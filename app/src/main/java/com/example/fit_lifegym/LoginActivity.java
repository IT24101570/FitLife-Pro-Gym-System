package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.fit_lifegym.models.User;
import com.example.fit_lifegym.services.MainServiceRepository;
import com.example.fit_lifegym.utils.FirebaseHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.example.fit_lifegym.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnFingerprint;
    private TextView tvRegister, tvForgotPassword;
    private CheckBox cbRememberMe;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private SessionManager sessionManager;

    @Inject MainServiceRepository mainServiceRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersRef();
        sessionManager = new SessionManager(this);

        if (mAuth.getCurrentUser() != null && sessionManager.isLoggedIn()) {
            // Ensure service is running for background signaling
            mainServiceRepository.startService(sessionManager.getUserId());
            navigateToHome();
            return;
        }

        initViews();
        setupListeners();
        checkFingerprintSupport();

        // Auto-show biometric prompt if enabled and credentials exist
        if (sessionManager.isFingerprintEnabled() && sessionManager.getEmail() != null && sessionManager.getPassword() != null) {
            new android.os.Handler().postDelayed(this::showBiometricPrompt, 500);
        }
    }

    private void checkFingerprintSupport() {
        if (sessionManager.isFingerprintEnabled() && sessionManager.getEmail() != null) {
            btnFingerprint.setVisibility(View.VISIBLE);
            showBiometricPrompt();
        } else {
            btnFingerprint.setVisibility(View.GONE);
        }
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON && errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_auth_error, errString), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                String email = sessionManager.getEmail();
                String password = sessionManager.getPassword();

                if (email != null && password != null) {
                    showLoading(true);
                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.msg_auth_success), Toast.LENGTH_SHORT).show();
                                    loadUserData(firebaseUser.getUid(), password);
                                }
                            } else {
                                showLoading(false);
                                Toast.makeText(getApplicationContext(), getString(R.string.msg_biometric_failed, 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown")), Toast.LENGTH_LONG).show();
                            }
                        });
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_session_expired), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), getString(R.string.msg_auth_failed), Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_title))
                .setSubtitle(getString(R.string.biometric_prompt_subtitle))
                .setNegativeButtonText(getString(R.string.biometric_negative_button))
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnFingerprint = findViewById(R.id.btnFingerprint);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        btnFingerprint.setOnClickListener(v -> showBiometricPrompt());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.error_password_empty));
            etPassword.requestFocus();
            return;
        }

        showLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        loadUserData(firebaseUser.getUid(), password);
                    }
                } else {
                    showLoading(false);
                    String error = task.getException() != null ? task.getException().getMessage() : getString(R.string.msg_auth_failed);
                    Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Login Failed: " + error);
                }
            });
    }

    private void loadUserData(String userId, String password) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        loginSuccess(userId, user.getName(), user.getEmail(), user.getPhone(), user.getRole(), password);
                    }
                } else {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        createUserData(userId, firebaseUser.getEmail(), firebaseUser.getDisplayName(), password);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "DB Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserData(String userId, String email, String displayName, String password) {
        String name = (displayName != null && !displayName.isEmpty()) ? displayName : email.split("@")[0];
        User user = new User(userId, name, email, "MEMBER");
        
        usersRef.child(userId).setValue(user)
            .addOnSuccessListener(aVoid -> loginSuccess(userId, name, email, null, "MEMBER", password))
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Failed to create record: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loginSuccess(String userId, String name, String email, String phone, String role, String password) {
        sessionManager.createLoginSession(userId, name, email, phone, role, password);
        
        // Critical Step: Initialize the signaling service immediately on login
        mainServiceRepository.startService(userId);

        showLoading(false);
        navigateToHome();
    }

    private void showForgotPasswordDialog() {
        // Reset password logic
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }
}
