package com.example.fit_lifegym;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.models.Professional;
import com.example.fit_lifegym.services.MainService;
import com.example.fit_lifegym.services.MainServiceRepository;
import com.example.fit_lifegym.utils.DataModel;
import com.example.fit_lifegym.utils.DataModelType;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.permissionx.guolindev.PermissionX;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TrainerActivity extends AppCompatActivity implements MainService.IncomingCallListener {

    private static final String TAG = "TrainerActivity";
    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    @Inject MainServiceRepository serviceRepo;
    
    private TextView tvApprovalStatus;
    private MaterialCardView cardManageProfile, cardSessions, cardAvailability, cardWorkoutPlans, cardGoalSubmissions;
    private MaterialButton btnRatings, btnLogout;
    private ImageButton btnBack;

    // Incoming Call UI
    private View incomingCallLayout;
    private TextView incomingCallTitleTv;
    private Button acceptButton, declineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);

        sessionManager = new SessionManager(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupListeners();
        checkProfileStatus();

        // Initialize Call Receiving
        checkPermissionsAndStartService();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvApprovalStatus = findViewById(R.id.tvApprovalStatus);
        cardManageProfile = findViewById(R.id.cardManageProfile);
        cardSessions = findViewById(R.id.cardSessions);
        cardAvailability = findViewById(R.id.cardAvailability);
        cardWorkoutPlans = findViewById(R.id.cardWorkoutPlans);
        cardGoalSubmissions = findViewById(R.id.cardGoalSubmissions);
        btnRatings = findViewById(R.id.btnRatings);
        btnLogout = findViewById(R.id.btnLogout);

        // Incoming Call UI
        incomingCallLayout = findViewById(R.id.incomingCallLayout);
        incomingCallTitleTv = findViewById(R.id.incomingCallTitleTv);
        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);
    }

    private void checkPermissionsAndStartService() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        PermissionX.init(this)
            .permissions(permissions)
            .request((allGranted, grantedList, deniedList) -> {
                serviceRepo.startService(sessionManager.getUserId());
            });
    }

    @Override
    public void onIncomingCall(DataModel model) {
        if (model == null || model.getType() == null) return;
        
        Log.d(TAG, "onIncomingCall: " + model.getType());
        runOnUiThread(() -> {
            if (model.getType() == DataModelType.StartVideoCall || 
                model.getType() == DataModelType.StartAudioCall ||
                model.getType() == DataModelType.Offer) {
                
                if (incomingCallLayout.getVisibility() == View.VISIBLE) return;

                String displayName = (model.getSenderName() != null && !model.getSenderName().isEmpty()) 
                                    ? model.getSenderName() : model.getSender();
                
                incomingCallTitleTv.setText(displayName + " is calling...");
                incomingCallLayout.setVisibility(View.VISIBLE);
                
                acceptButton.setOnClickListener(v -> {
                    incomingCallLayout.setVisibility(View.GONE);
                    Intent intent = new Intent(TrainerActivity.this, CallActivity.class);
                    intent.putExtra("target", model.getSender());
                    intent.putExtra("targetName", displayName);
                    intent.putExtra("isVideoCall", model.getType() != DataModelType.StartAudioCall);
                    intent.putExtra("isCaller", false);
                    startActivity(intent);
                });
                
                declineButton.setOnClickListener(v -> {
                    incomingCallLayout.setVisibility(View.GONE);
                    serviceRepo.sendEndCall();
                });
            } else if (model.getType() == DataModelType.EndCall) {
                incomingCallLayout.setVisibility(View.GONE);
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        cardManageProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfessionalProfileActivity.class)));
        cardSessions.setOnClickListener(v -> startActivity(new Intent(this, BookingHistoryActivity.class)));
        cardAvailability.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        cardWorkoutPlans.setOnClickListener(v -> startActivity(new Intent(this, TrainerWorkoutPlanActivity.class)));
        
        if (cardGoalSubmissions != null) {
            cardGoalSubmissions.setOnClickListener(v -> startActivity(new Intent(this, TrainerGuidanceActivity.class)));
        }

        btnRatings.setOnClickListener(v -> startActivity(new Intent(this, WorkoutHistoryActivity.class)));
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
        if (trainerId == null) return;

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
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void enableButtons(boolean enabled) {
        cardSessions.setEnabled(enabled);
        cardAvailability.setEnabled(enabled);
        cardWorkoutPlans.setEnabled(enabled);
        if (cardGoalSubmissions != null) cardGoalSubmissions.setEnabled(enabled);
        btnRatings.setEnabled(enabled);
        float alpha = enabled ? 1.0f : 0.5f;
        cardSessions.setAlpha(alpha);
        cardAvailability.setAlpha(alpha);
        cardWorkoutPlans.setAlpha(alpha);
        if (cardGoalSubmissions != null) cardGoalSubmissions.setAlpha(alpha);
        btnRatings.setAlpha(alpha);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainService.setIncomingCallListener(this);
    }

    @Override
    protected void onPause() {
        MainService.setIncomingCallListener(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        MainService.setIncomingCallListener(null);
        super.onDestroy();
    }
}
