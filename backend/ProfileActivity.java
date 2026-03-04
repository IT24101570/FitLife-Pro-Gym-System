package com.example.fit_lifegym;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.utils.SessionManager;
import com.example.fit_lifegym.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    
    private ImageView btnBack, ivProfilePicture;
    private TextView tvName, tvEmail, tvPhone, tvRole, tvMemberSince;
    private MaterialButton btnEditProfile, btnPaymentHistory, btnLogout;

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
    }

    private void loadUserData() {
        tvName.setText(sessionManager.getName());
        tvEmail.setText(sessionManager.getEmail());
        
        String phone = sessionManager.getPhone();
        tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "Not set");
        
        String role = sessionManager.getRole();
        tvRole.setText(role != null ? role.toUpperCase() : "MEMBER");
        
        tvMemberSince.setText("Member since Jan 2024");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnPaymentHistory.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, PaymentHistoryActivity.class)));
        btnLogout.setOnClickListener(v -> showLogoutDialog());
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
            .setTitle("Refine Profile")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Save Changes", null)
            .setNegativeButton("Cancel", (d, which) -> d.dismiss())
            .create();

        dialog.show();

        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        saveButton.setTextColor(getResources().getColor(R.color.accent));

        saveButton.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String weight = etWeight.getText().toString().trim();
            String height = etHeight.getText().toString().trim();
            String age = etAge.getText().toString().trim();

            // --- VALIDATION LOGIC ---
            if (!ValidationUtils.isValidName(name)) {
                etName.setError("Name is too short");
                return;
            }

            if (!phone.isEmpty() && phone.length() < 10) {
                etPhone.setError("Invalid phone number");
                return;
            }

            if (!weight.isEmpty() && !ValidationUtils.isValidWeight(weight)) {
                etWeight.setError("Weight must be between 30-300 kg");
                return;
            }

            if (!height.isEmpty() && !ValidationUtils.isValidHeight(height)) {
                etHeight.setError("Height must be between 100-250 cm");
                return;
            }

            if (!age.isEmpty() && !ValidationUtils.isValidAge(age)) {
                etAge.setError("Age must be between 12-100");
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
        tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "Not set");
        
        Toast.makeText(this, "Profile Synchronized!", Toast.LENGTH_SHORT).show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this, R.style.Theme_Fit_lifeGym)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to end your session?")
            .setPositiveButton("Sign Out", (dialog, which) -> {
                sessionManager.logout();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Stay Connected", null)
            .show();
    }
}
