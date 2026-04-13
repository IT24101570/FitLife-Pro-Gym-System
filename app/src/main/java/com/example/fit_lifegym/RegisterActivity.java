package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.utils.FirebaseHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.example.fit_lifegym.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword, etPhone;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private android.widget.RadioGroup rgRole;
    
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private SessionManager sessionManager;

    // SMS Verification variables
    private String mVerificationId;
    private com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken mResendToken;
    private com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersRef();
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
        rgRole = findViewById(R.id.rgRole);

        initPhoneCallbacks();
    }

    private void initPhoneCallbacks() {
        mCallbacks = new com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull com.google.firebase.auth.PhoneAuthCredential credential) {
                showLoading(false);
                // Auto-verification or instant validation
                String code = credential.getSmsCode();
                if (code != null) {
                    verifyCredential(credential);
                }
            }

            @Override
            public void onVerificationFailed(@NonNull com.google.firebase.FirebaseException e) {
                showLoading(false);
                Log.e(TAG, "onVerificationFailed", e);
                Toast.makeText(RegisterActivity.this, getString(R.string.msg_verification_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken token) {
                showLoading(false);
                mVerificationId = verificationId;
                mResendToken = token;
                showOtpDialog();
            }
        };
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        final String name = etName.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String phone = etPhone.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // --- VALIDATION START ---
        if (!ValidationUtils.isValidName(name)) {
            etName.setError(getString(R.string.error_name_invalid));
            etName.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }

        if (phone.isEmpty() || !phone.startsWith("+")) {
            etPhone.setError(getString(R.string.error_phone_invalid));
            etPhone.requestFocus();
            return;
        }

        if (!ValidationUtils.isStrongPassword(password)) {
            etPassword.setError(getString(R.string.error_password_weak));
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_passwords_mismatch));
            etConfirmPassword.requestFocus();
            return;
        }

        if (rgRole.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, getString(R.string.error_role_required), Toast.LENGTH_SHORT).show();
            return;
        }
        // --- VALIDATION END ---
        
        showLoading(true);
        startPhoneNumberVerification(phone);
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        com.google.firebase.auth.PhoneAuthOptions options =
                com.google.firebase.auth.PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        com.google.firebase.auth.PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void showOtpDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_otp, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        TextInputEditText etOtp = view.findViewById(R.id.etOtp);
        MaterialButton btnVerifyOtp = view.findViewById(R.id.btnVerifyOtp);
        TextView tvResendOtp = view.findViewById(R.id.tvResendOtp);

        btnVerifyOtp.setOnClickListener(v -> {
            String code = etOtp.getText().toString().trim();
            if (code.length() == 6) {
                dialog.dismiss();
                showLoading(true);
                com.google.firebase.auth.PhoneAuthCredential credential = 
                    com.google.firebase.auth.PhoneAuthProvider.getCredential(mVerificationId, code);
                verifyCredential(credential);
            } else {
                etOtp.setError(getString(R.string.error_otp_invalid));
            }
        });

        tvResendOtp.setOnClickListener(v -> {
            dialog.dismiss();
            registerUser(); // Re-trigger verification
        });

        dialog.show();
    }

    private void verifyCredential(com.google.firebase.auth.PhoneAuthCredential credential) {
        // Here we just verify the phone, but the primary auth is still Email/Password
        // We'll proceed to create the Email account now
        createEmailAccount();
    }

    private void createEmailAccount() {
        final String name = etName.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        final String selectedRole = getSelectedRole();

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        // Link the phone number to the account
                        firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build());
                        
                        saveUserToDatabase(firebaseUser.getUid(), name, email, etPhone.getText().toString().trim(), selectedRole, password);
                    }
                } else {
                    showLoading(false);
                    String error = task.getException() != null ? task.getException().getMessage() : getString(R.string.msg_auth_failed);
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
    }

    private void saveUserToDatabase(String userId, String name, String email, String phone, String role, String password) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userId);
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("phone", phone);
        userMap.put("role", role);
        userMap.put("createdAt", System.currentTimeMillis());
        userMap.put("isActive", true);
        userMap.put("isPremium", false);

        usersRef.child(userId).setValue(userMap)
            .addOnSuccessListener(aVoid -> {
                showLoading(false);
                registrationSuccess(userId, name, email, phone, role, password);
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(RegisterActivity.this, "DB Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private String getSelectedRole() {
        int selectedId = rgRole.getCheckedRadioButtonId();
        if (selectedId == R.id.rbTrainer) return "TRAINER";
        if (selectedId == R.id.rbDoctor) return "DOCTOR";
        return "MEMBER";
    }

    private void registrationSuccess(String userId, String name, String email, String phone, String role, String password) {
        sessionManager.createLoginSession(userId, name, email, phone, role, password);
        // If SessionManager has a setPhone method, we should call it here too
        // or ensure createLoginSession handles it.
        Toast.makeText(this, getString(R.string.msg_welcome_elite, name), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
}
