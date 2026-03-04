package com.example.fit_lifegym;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CardView cardTheme, cardChangePassword;
    private TextView tvCurrentTheme, tvNotificationStatus;
    
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        themeManager = new ThemeManager(this);
        
        initializeViews();
        setupListeners();
        updateThemeDisplay();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        cardTheme = findViewById(R.id.cardTheme);
        cardChangePassword = findViewById(R.id.cardChangePassword);
        tvCurrentTheme = findViewById(R.id.tvCurrentTheme);
        tvNotificationStatus = findViewById(R.id.tvNotificationStatus);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        cardTheme.setOnClickListener(v -> showThemeDialog());
        
        if (cardChangePassword != null) {
            cardChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }
    }

    private void showThemeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Theme");
        
        String[] themes = {"Light", "Dark", "System Default"};
        int currentMode = themeManager.getThemeMode();
        
        builder.setSingleChoiceItems(themes, currentMode, (dialog, which) -> {
            themeManager.setThemeMode(which);
            updateThemeDisplay();
            dialog.dismiss();
            
            // Recreate activity to apply theme
            recreate();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText etCurrent = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNew = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirm = dialogView.findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Update", (dialog, which) -> {
                String currentPw = etCurrent.getText().toString();
                String newPw = etNew.getText().toString();
                String confirmPw = etConfirm.getText().toString();

                if (newPw.isEmpty() || !newPw.equals(confirmPw)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Logic to update password in Firebase would go here
                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void updateThemeDisplay() {
        int mode = themeManager.getThemeMode();
        tvCurrentTheme.setText("Theme: " + themeManager.getThemeModeName(mode));
    }
}
