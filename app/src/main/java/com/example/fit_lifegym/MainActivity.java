package com.example.fit_lifegym;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.adapters.BookingAdapter;
import com.example.fit_lifegym.models.Booking;
import com.example.fit_lifegym.models.FitnessGoalSubmission;
import com.example.fit_lifegym.services.MainService;
import com.example.fit_lifegym.services.MainServiceRepository;
import com.example.fit_lifegym.utils.DataModel;
import com.example.fit_lifegym.utils.DataModelType;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
public class MainActivity extends AppCompatActivity implements MainService.IncomingCallListener {

    private static final String TAG = "MainActivity";
    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    private String dbUrl = "https://fit-life-gym-default-rtdb.firebaseio.com";
    @Inject MainServiceRepository serviceRepo;

    // UI Elements
    private TextView subtitle, tvWelcome;
    private View btnProfile;
    private LinearLayout memberDashboard, adminDashboard;
    private Button btnLogout;
    private View btnPersonalizedPlan;

    // Bookings RecyclerView
    private RecyclerView rvActiveBookings;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    private TextView tvActiveBookingsTitle;

    // Incoming Call UI
    private View incomingCallLayout;
    private TextView incomingCallTitleTv;
    private Button acceptButton, declineButton;

    // Quick Actions
    private CardView btnBookDoctor, btnBookTrainer, btnWorkoutTracker, btnWorkoutPlans, btnNutritionTracker, btnMealPlans, btnVideoLibrary, btnSocialFeed, btnMedicalReports;

    // Stats
    private TextView tvStatActiveValue, tvStatUpcomingValue, tvStatMealPlansValue, tvStatWorkoutPlansValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        databaseReference = FirebaseDatabase.getInstance(dbUrl).getReference();

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupRecyclerView();
        setupRoleBasedUI();
        setupListeners();
        updateWelcomeMessage();

        if (sessionManager.isMember()) {
            loadMemberStats();
            loadActiveBookings();
        }

        // Initialize Video Call Service
        checkPermissionsAndStartService();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        subtitle = findViewById(R.id.subtitle);
        btnProfile = findViewById(R.id.btnProfile);
        memberDashboard = findViewById(R.id.memberDashboard);
        adminDashboard = findViewById(R.id.adminDashboard);
        tvActiveBookingsTitle = findViewById(R.id.tvActiveBookingsTitle);
        rvActiveBookings = findViewById(R.id.rvActiveBookings);
        btnPersonalizedPlan = findViewById(R.id.btnPersonalizedPlan);

        // Stats
        setupStatCard(R.id.statActiveBookings, "Active", "Bookings", v -> tvStatActiveValue = v);
        setupStatCard(R.id.statUpcomingBookings, "Upcoming", "Pending", v -> tvStatUpcomingValue = v);
        setupStatCard(R.id.statActiveMealPlans, "Meal Plans", "Active", v -> tvStatMealPlansValue = v);
        setupStatCard(R.id.statActiveWorkoutPlans, "Workouts", "Plans", v -> tvStatWorkoutPlansValue = v);

        // Buttons
        btnBookDoctor = findViewById(R.id.btnBookDoctor);
        btnBookTrainer = findViewById(R.id.btnBookTrainer);
        btnWorkoutTracker = findViewById(R.id.btnWorkoutTracker);
        btnWorkoutPlans = findViewById(R.id.btnWorkoutPlans);
        btnNutritionTracker = findViewById(R.id.btnNutritionTracker);
        btnMealPlans = findViewById(R.id.btnMealPlans);
        btnVideoLibrary = findViewById(R.id.btnVideoLibrary);
        btnSocialFeed = findViewById(R.id.btnSocialFeed);
        btnMedicalReports = findViewById(R.id.btnMedicalReports);
        btnLogout = findViewById(R.id.btnLogout);

        // Incoming Call UI
        incomingCallLayout = findViewById(R.id.incomingCallLayout);
        incomingCallTitleTv = findViewById(R.id.incomingCallTitleTv);
        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);
    }

    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(this, bookingList);
        rvActiveBookings.setLayoutManager(new LinearLayoutManager(this));
        rvActiveBookings.setAdapter(bookingAdapter);
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
                if (sessionManager.isLoggedIn()) {
                    serviceRepo.startService(sessionManager.getUserId());
                }
            });
    }

    private void setupStatCard(int id, String label, String unit, StatValueSetter setter) {
        View view = findViewById(id);
        if (view != null) {
            TextView tvLabel = view.findViewById(R.id.tvStatLabel);
            TextView tvUnit = view.findViewById(R.id.tvStatUnit);
            TextView tvValue = view.findViewById(R.id.tvStatValue);
            if (tvLabel != null) tvLabel.setText(label);
            if (tvUnit != null) tvUnit.setText(unit);
            if (tvValue != null) setter.set(tvValue);
        }
    }

    interface StatValueSetter { void set(TextView tv); }

    private void setupRoleBasedUI() {
        if (sessionManager.isAdmin()) {
            adminDashboard.setVisibility(View.VISIBLE);
            memberDashboard.setVisibility(View.GONE);
        } else if (sessionManager.isTrainer()) {
            Intent intent = new Intent(this, TrainerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (sessionManager.isDoctor()) {
            Intent intent = new Intent(this, DoctorActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            memberDashboard.setVisibility(View.VISIBLE);
            adminDashboard.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        View btnUpgrade = findViewById(R.id.btnUpgrade);
        if (btnUpgrade != null) btnUpgrade.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SubscriptionActivity.class)));

        setClickListener(btnBookDoctor, BookingEnhancedActivity.class, "DOCTOR");
        setClickListener(btnBookTrainer, BookingEnhancedActivity.class, "TRAINER");
        setClickListener(btnWorkoutTracker, WorkoutTrackerActivity.class, null);
        setClickListener(btnWorkoutPlans, ExerciseLibraryActivity.class, null);
        setClickListener(btnNutritionTracker, NutritionTrackerActivity.class, null);
        setClickListener(btnMealPlans, MealPlansActivity.class, null);
        
        View btnApproveProfiles = findViewById(R.id.btnApproveProfiles);
        if (btnApproveProfiles != null) {
            btnApproveProfiles.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminActivity.class)));
        }
        
        View btnAdminManage = findViewById(R.id.btnAdminManage);
        if (btnAdminManage != null) {
            btnAdminManage.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SystemManagementActivity.class)));
        }

        if (btnVideoLibrary != null) {
            btnVideoLibrary.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, VideoLibraryActivity.class)));
        }
        
        if (btnSocialFeed != null) {
            btnSocialFeed.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SocialFeedActivity.class)));
        }
        
        if (btnMedicalReports != null) {
            btnMedicalReports.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MedicalReportsListActivity.class)));
        }

        if (btnPersonalizedPlan != null) {
            btnPersonalizedPlan.setOnClickListener(v -> {
                Log.d(TAG, "Personalized Plan clicked");
                checkExistingSubmission();
            });
        }

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void checkExistingSubmission() {
        String userId = sessionManager.getUserId();
        databaseReference.child("fitness_submissions").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Already submitted, view results
                            startActivity(new Intent(MainActivity.this, PersonalizedPlanResultActivity.class));
                        } else {
                            // Not submitted yet, go to setup screen
                            startActivity(new Intent(MainActivity.this, PersonalizedPlanActivity.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setClickListener(View view, Class<?> activityClass, String typeExtra) {
        if (view != null) {
            view.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, activityClass);
                if (typeExtra != null) intent.putExtra("type", typeExtra);
                startActivity(intent);
            });
        }
    }

    private void updateWelcomeMessage() {
        String name = sessionManager.getName();
        if (name != null) {
            subtitle.setText(name);
            tvWelcome.setText("Welcome back,");
        }
    }

    private void loadMemberStats() {
        String userId = sessionManager.getUserId();
        databaseReference.child("bookings").orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int active = 0;
                        int pending = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Booking b = ds.getValue(Booking.class);
                            if (b != null) {
                                if (b.isConfirmed()) active++;
                                if (b.isPending()) pending++;
                            }
                        }
                        if (tvStatActiveValue != null) tvStatActiveValue.setText(String.valueOf(active));
                        if (tvStatUpcomingValue != null) tvStatUpcomingValue.setText(String.valueOf(pending));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadActiveBookings() {
        String userId = sessionManager.getUserId();
        databaseReference.child("bookings").orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookingList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Booking b = ds.getValue(Booking.class);
                            if (b != null && (b.isConfirmed() || b.isPending() || b.isCompleted())) {
                                b.setId(ds.getKey());
                                bookingList.add(b);
                            }
                        }
                        
                        if (!bookingList.isEmpty()) {
                            tvActiveBookingsTitle.setVisibility(View.VISIBLE);
                            bookingAdapter.notifyDataSetChanged();
                        } else {
                            tvActiveBookingsTitle.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
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
                    Intent intent = new Intent(MainActivity.this, CallActivity.class);
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
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onDestroy() {
        MainService.setIncomingCallListener(null);
        super.onDestroy();
    }
}
