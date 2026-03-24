package com.example.fit_lifegym;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SystemManagementActivity extends AppCompatActivity {

    private MaterialSwitch switchMaintenance, switchRegistration;
    private MaterialButton btnClearCache, btnResetStats, btnAppUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_management);

        initViews();
        setupListeners();
    }

    private void initViews() {
        switchMaintenance = findViewById(R.id.switchMaintenance);
        switchRegistration = findViewById(R.id.switchRegistration);
        btnClearCache = findViewById(R.id.btnClearCache);
        btnResetStats = findViewById(R.id.btnResetStats);
        btnAppUpdate = findViewById(R.id.btnAppUpdate);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        switchMaintenance.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Maintenance Mode ON" : "Maintenance Mode OFF";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        switchRegistration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Registrations Enabled" : "Registrations Disabled";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        btnClearCache.setOnClickListener(v -> {
            Toast.makeText(this, "App cache cleared successfully!", Toast.LENGTH_SHORT).show();
        });

        btnResetStats.setOnClickListener(v -> {
            Toast.makeText(this, "Analytics data has been reset.", Toast.LENGTH_SHORT).show();
        });

        btnAppUpdate.setOnClickListener(v -> {
            Toast.makeText(this, "Checking for latest updates...", Toast.LENGTH_SHORT).show();
        });
    }
}
