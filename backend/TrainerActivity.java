package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.models.Professional;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrainerActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    
    private TextView tvApprovalStatus;
    private MaterialCardView cardManageProfile, cardSessions, cardAvailability, cardWorkoutPlans;
    private MaterialButton btnRatings, btnLogout;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);

        sessionManager = new SessionManager(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupListeners();
        checkProfileStatus();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvApprovalStatus = findViewById(R.id.tvApprovalStatus);
        cardManageProfile = findViewById(R.id.cardManageProfile);
        cardSessions = findViewById(R.id.cardSessions);
        cardAvailability = findViewById(R.id.cardAvailability);
        cardWorkoutPlans = findViewById(R.id.cardWorkoutPlans);
        btnRatings = findViewById(R.id.btnRatings);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        cardManageProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfessionalProfileActivity.class));
        });

        cardSessions.setOnClickListener(v -> {
            startActivity(new Intent(this, BookingHistoryActivity.class));
        });

        cardAvailability.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        cardWorkoutPlans.setOnClickListener(v -> {
            startActivity(new Intent(this, ExerciseLibraryActivity.class));
        });

        btnRatings.setOnClickListener(v -> {
            startActivity(new Intent(this, WorkoutHistoryActivity.class));
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> {
                sessionManager.logout();
                navigateToLogin();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkProfileStatus() {
        String trainerId = sessionManager.getUserId();
        databaseReference.child("professionals").child(trainerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Professional p = snapshot.getValue(Professional.class);
                            if (p != null) {
                                String status = p.getApprovalStatus();
                                tvApprovalStatus.setText(status != null ? status : "PENDING");
                                
                                if ("APPROVED".equals(status)) {
                                    tvApprovalStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                    enableButtons(true);
                                } else if ("REJECTED".equals(status)) {
                                    tvApprovalStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                    enableButtons(false);
                                } else {
                                    tvApprovalStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                                    enableButtons(false);
                                }
                            }
                        } else {
                            tvApprovalStatus.setText("PROFILE NOT CREATED");
                            enableButtons(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void enableButtons(boolean enabled) {
        cardSessions.setEnabled(enabled);
        cardAvailability.setEnabled(enabled);
        cardWorkoutPlans.setEnabled(enabled);
        btnRatings.setEnabled(enabled);
        
        float alpha = enabled ? 1.0f : 0.5f;
        cardSessions.setAlpha(alpha);
        cardAvailability.setAlpha(alpha);
        cardWorkoutPlans.setAlpha(alpha);
        btnRatings.setAlpha(alpha);
    }
}
