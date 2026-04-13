package com.example.fit_lifegym;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.fit_lifegym.utils.SessionManager;
import com.example.fit_lifegym.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    
    private ImageView btnBack, ivProfilePicture;
    private TextView tvName, tvEmail, tvPhone, tvRole, tvMemberSince;
    private MaterialButton btnEditProfile, btnPaymentHistory, btnLogout;
    private MaterialSwitch swFingerprint;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    uploadProfileImage(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        
        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvRole = findViewById(R.id.tvRole);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnPaymentHistory = findViewById(R.id.btnPaymentHistory);
        btnLogout = findViewById(R.id.btnLogout);
        swFingerprint = findViewById(R.id.swFingerprint);
    }

    private void loadUserData() {
        tvName.setText(sessionManager.getName());
        tvEmail.setText(sessionManager.getEmail());
        
        String photoUrl = sessionManager.getProfileImage();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this).load(photoUrl).placeholder(R.drawable.btn_4).into(ivProfilePicture);
        }
        
        String phone = sessionManager.getPhone();
        tvPhone.setText(phone != null && !phone.isEmpty() ? phone : getString(R.string.label_not_set));
        
        loadSubscriptionStatus();
        
        tvMemberSince.setText(getString(R.string.member_since_placeholder, "Jan 2024"));
        
        swFingerprint.setChecked(sessionManager.isFingerprintEnabled());
    }

    private void loadSubscriptionStatus() {
        String userId = sessionManager.getUserId();
        FirebaseDatabase.getInstance().getReference("subscriptions")
            .child(userId).child("current")
            .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        com.example.fit_lifegym.models.Subscription sub = snapshot.getValue(com.example.fit_lifegym.models.Subscription.class);
                        if (sub != null && sub.isActive()) {
                            tvRole.setText(getString(R.string.label_member_suffix, sub.getPlanType()));
                            tvRole.setTextColor(getResources().getColor(R.color.gold_primary));
                            return;
                        }
                    }
                    tvRole.setText(R.string.label_free_member);
                    tvRole.setTextColor(getResources().getColor(R.color.text_secondary));
                }

                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {}
            });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        ivProfilePicture.setOnClickListener(v -> openGallery());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnPaymentHistory.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, PaymentHistoryActivity.class)));
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        
        swFingerprint.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setFingerprintEnabled(isChecked);
            String status = isChecked ? getString(R.string.fingerprint_enabled) : getString(R.string.fingerprint_disabled);
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        });
    }

    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        TextInputEditText etWeight = dialogView.findViewById(R.id.etWeight);
        TextInputEditText etHeight = dialogView.findViewById(R.id.etHeight);
        TextInputEditText etAge = dialogView.findViewById(R.id.etAge);
        
        etName.setText(sessionManager.getName());
        etPhone.setText(sessionManager.getPhone());
        
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_Fit_lifeGym)
            .setTitle(R.string.title_refine_profile)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(R.string.action_save_changes, null)
            .setNegativeButton(R.string.action_cancel, (d, which) -> d.dismiss())
            .create();

        dialog.show();

        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        saveButton.setTextColor(getResources().getColor(R.color.gold_primary));

        saveButton.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String weight = etWeight.getText().toString().trim();
            String height = etHeight.getText().toString().trim();
            String age = etAge.getText().toString().trim();

            // --- VALIDATION LOGIC ---
            if (!ValidationUtils.isValidName(name)) {
                etName.setError(getString(R.string.error_name_short));
                return;
            }

            if (!phone.isEmpty() && phone.length() < 10) {
                etPhone.setError(getString(R.string.error_invalid_phone));
                return;
            }

            if (!weight.isEmpty() && !ValidationUtils.isValidWeight(weight)) {
                etWeight.setError(getString(R.string.error_invalid_weight));
                return;
            }

            if (!height.isEmpty() && !ValidationUtils.isValidHeight(height)) {
                etHeight.setError(getString(R.string.error_invalid_height));
                return;
            }

            if (!age.isEmpty() && !ValidationUtils.isValidAge(age)) {
                etAge.setError(getString(R.string.error_invalid_age));
                return;
            }

            updateProfile(name, phone, weight, height, age);
            dialog.dismiss();
        });
    }

    private void updateProfile(String name, String phone, String weight, String height, String age) {
        String userId = sessionManager.getUserId();
        DatabaseReference userRef = databaseReference.child(userId);
        
        userRef.child("name").setValue(name);
        userRef.child("phone").setValue(phone);
        if (!weight.isEmpty()) userRef.child("weight").setValue(weight);
        if (!height.isEmpty()) userRef.child("height").setValue(height);
        if (!age.isEmpty()) userRef.child("age").setValue(age);

        sessionManager.updateUserInfo(name, phone);
        tvName.setText(name);
        tvPhone.setText(phone != null && !phone.isEmpty() ? phone : getString(R.string.label_not_set));
        
        Toast.makeText(this, R.string.msg_profile_synchronized, Toast.LENGTH_SHORT).show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri uri) {
        Toast.makeText(this, "Uploading profile picture...", Toast.LENGTH_SHORT).show();
        MediaManager.get().upload(uri)
                .unsigned("ml_default")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        updateProfilePhotoUrl(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void updateProfilePhotoUrl(String url) {
        String userId = sessionManager.getUserId();
        databaseReference.child(userId).child("profileImage").setValue(url)
                .addOnSuccessListener(aVoid -> {
                    sessionManager.setProfileImage(url);
                    Glide.with(ProfileActivity.this).load(url).into(ivProfilePicture);
                    Toast.makeText(ProfileActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this, R.style.Theme_Fit_lifeGym)
            .setTitle(R.string.title_sign_out)
            .setMessage(R.string.msg_sign_out_confirm)
            .setPositiveButton(R.string.title_sign_out, (dialog, which) -> {
                sessionManager.logout();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton(R.string.action_stay_connected, null)
            .show();
    }
}
