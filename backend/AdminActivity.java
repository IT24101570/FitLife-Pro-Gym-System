package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";
    private Button btnViewMembers, btnViewPayments, btnManageSystem, btnSignOutTop;
    private Button btnApproveDoctorRequests, btnApproveTrainerRequests;
    private View btnBack, btnLogoutToolbar;
    private View statUsers, statMembers, statBookings, statRevenue;
    
    private DatabaseReference usersRef, bookingsRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        sessionManager = new SessionManager(this);
        initViews();
        initFirebase();
        setupListeners();
        loadStatistics();
    }

    private void initViews() {
        btnViewMembers = findViewById(R.id.btnViewMembers);
        btnViewPayments = findViewById(R.id.btnViewPayments);
        btnApproveDoctorRequests = findViewById(R.id.btnApproveDoctorRequests);
        btnApproveTrainerRequests = findViewById(R.id.btnApproveTrainerRequests);
        btnManageSystem = findViewById(R.id.btnManageSystem);
        btnSignOutTop = findViewById(R.id.btnSignOutTop);
        btnLogoutToolbar = findViewById(R.id.btnLogoutToolbar);
        btnBack = findViewById(R.id.btnBack);
        
        statUsers = findViewById(R.id.statUsers);
        statMembers = findViewById(R.id.statMembers);
        statBookings = findViewById(R.id.statBookings);
        statRevenue = findViewById(R.id.statRevenue);
    }

    private void initFirebase() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Sign Out Listeners
        View.OnClickListener logoutListener = v -> showLogoutDialog();
        if (btnSignOutTop != null) btnSignOutTop.setOnClickListener(logoutListener);
        if (btnLogoutToolbar != null) btnLogoutToolbar.setOnClickListener(logoutListener);

        if (btnManageSystem != null) {
            btnManageSystem.setOnClickListener(v -> {
                Log.d(TAG, "Navigating to System Management");
                Intent intent = new Intent(AdminActivity.this, SystemManagementActivity.class);
                startActivity(intent);
            });
        }

        if (btnApproveDoctorRequests != null) {
            btnApproveDoctorRequests.setOnClickListener(v -> {
                Log.d(TAG, "Opening Doctor Approvals");
                Intent intent = new Intent(AdminActivity.this, ApprovalListActivity.class);
                intent.putExtra("type", "DOCTOR");
                startActivity(intent);
            });
        }

        if (btnApproveTrainerRequests != null) {
            btnApproveTrainerRequests.setOnClickListener(v -> {
                Log.d(TAG, "Opening Trainer Approvals");
                Intent intent = new Intent(AdminActivity.this, ApprovalListActivity.class);
                intent.putExtra("type", "TRAINER");
                startActivity(intent);
            });
        }

        if (btnViewMembers != null) {
            btnViewMembers.setOnClickListener(v -> {
                Toast.makeText(this, "Opening User Management...", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out", (dialog, which) -> {
                FirebaseAuth.getInstance().signOut();
                sessionManager.logout();
                Intent intent = new Intent(AdminActivity.this, IntroActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void loadStatistics() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateStatValue(statUsers, String.valueOf(dataSnapshot.getChildrenCount()), "Total Users");
                long activeMembers = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if ("MEMBER".equals(ds.child("role").getValue(String.class))) activeMembers++;
                }
                updateStatValue(statMembers, String.valueOf(activeMembers), "Active Members");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        bookingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateStatValue(statBookings, String.valueOf(dataSnapshot.getChildrenCount()), "Bookings");
                double total = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Double p = ds.child("price").getValue(Double.class);
                    if (p != null) total += p;
                }
                updateStatValue(statRevenue, String.format("$%.0f", total), "Revenue");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void updateStatValue(View card, String value, String label) {
        if (card != null) {
            TextView tvValue = card.findViewById(R.id.tvStatValue);
            TextView tvLabel = card.findViewById(R.id.tvStatLabel);
            if (tvValue != null) tvValue.setText(value);
            if (tvLabel != null) tvLabel.setText(label);
        }
    }
}
