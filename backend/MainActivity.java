package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fit_lifegym.models.Booking;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    private String dbUrl = "https://fit-life-gym-default-rtdb.firebaseio.com";

    // UI Elements
    private TextView subtitle, tvWelcome;
    private View btnProfile;
    private LinearLayout memberDashboard, adminDashboard;
    private View cardActiveBooking, reviewSection;
    private TextView tvBookingProName, tvBookingStatus;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmitReview, btnLogout;
    private FloatingActionButton fabChatbot;

    // Quick Actions
    private CardView btnBookDoctor, btnBookTrainer, btnWorkoutTracker, btnWorkoutPlans, btnNutritionTracker, btnMealPlans, btnVideoLibrary, btnSocialFeed;

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
        setupRoleBasedUI();
        setupListeners();
        updateWelcomeMessage();

        if (sessionManager.isMember()) {
            loadMemberStats();
            loadActiveBooking();
        }
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        subtitle = findViewById(R.id.subtitle);
        btnProfile = findViewById(R.id.btnProfile);
        memberDashboard = findViewById(R.id.memberDashboard);
        adminDashboard = findViewById(R.id.adminDashboard);

        // Stats - Safe ID matching with activity_main.xml
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
        fabChatbot = findViewById(R.id.fabChatbot);

        cardActiveBooking = findViewById(R.id.cardActiveBooking);
        tvBookingProName = findViewById(R.id.tvBookingProName);
        tvBookingStatus = findViewById(R.id.tvBookingStatus);
        reviewSection = findViewById(R.id.reviewSection);
        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        btnLogout = findViewById(R.id.btnLogout);
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
        String role = sessionManager.getRole();
        if (memberDashboard != null) memberDashboard.setVisibility(View.GONE);
        if (adminDashboard != null) adminDashboard.setVisibility(View.GONE);

        if (sessionManager.isAdmin()) {
            if (adminDashboard != null) adminDashboard.setVisibility(View.VISIBLE);
        } else if (sessionManager.isTrainer()) {
            startActivity(new Intent(this, TrainerActivity.class));
            finish();
        } else if (sessionManager.isDoctor()) {
            startActivity(new Intent(this, DoctorActivity.class));
            finish();
        } else {
            if (memberDashboard != null) memberDashboard.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        if (btnProfile != null) btnProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        View btnUpgrade = findViewById(R.id.btnUpgrade);
        if (btnUpgrade != null) btnUpgrade.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SubscriptionActivity.class)));

        setClickListener(btnBookDoctor, BookingEnhancedActivity.class, "DOCTOR");
        setClickListener(btnBookTrainer, BookingEnhancedActivity.class, "TRAINER");
        setClickListener(btnWorkoutTracker, WorkoutTrackerActivity.class, null);
        setClickListener(btnWorkoutPlans, ExerciseLibraryActivity.class, null);
        setClickListener(btnNutritionTracker, NutritionTrackerActivity.class, null);
        setClickListener(btnMealPlans, MealPlansActivity.class, null);
        
        // Admin Button Listeners
        View btnApprove = findViewById(R.id.btnApproveProfiles);
        if (btnApprove != null) {
            btnApprove.setOnClickListener(v -> {
                Log.d("MainActivity", "Approvals button clicked - Redirecting to Admin Center");
                Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                startActivity(intent);
            });
        }
        
        View btnManage = findViewById(R.id.btnAdminManage);
        if (btnManage != null) {
            btnManage.setOnClickListener(v -> {
                Log.d("MainActivity", "Manage System button clicked - Redirecting to System Management");
                startActivity(new Intent(MainActivity.this, SystemManagementActivity.class));
            });
        }

        // Stat Card Listeners
        View statActive = findViewById(R.id.statActiveBookings);
        if (statActive != null) {
            statActive.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BookingHistoryActivity.class)));
        }

        View statUpcoming = findViewById(R.id.statUpcomingBookings);
        if (statUpcoming != null) {
            statUpcoming.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BookingHistoryActivity.class)));
        }

        View statMeals = findViewById(R.id.statActiveMealPlans);
        if (statMeals != null) {
            statMeals.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MealPlansActivity.class)));
        }

        View statWorkouts = findViewById(R.id.statActiveWorkoutPlans);
        if (statWorkouts != null) {
            statWorkouts.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WorkoutPlansActivity.class)));
        }

        if (btnVideoLibrary != null) {
            btnVideoLibrary.setOnClickListener(v -> {
                Log.d("MainActivity", "Opening VideoLibraryActivity");
                startActivity(new Intent(MainActivity.this, VideoLibraryActivity.class));
            });
        }
        
        if (btnSocialFeed != null) {
            btnSocialFeed.setOnClickListener(v -> {
                Log.d("MainActivity", "Opening SocialFeedActivity");
                startActivity(new Intent(MainActivity.this, SocialFeedActivity.class));
            });
        }

        if (fabChatbot != null) {
            fabChatbot.setOnClickListener(v -> {
                AIChatbotSheet chatbotSheet = new AIChatbotSheet();
                chatbotSheet.show(getSupportFragmentManager(), "AIChatbotSheet");
            });
        }

        if (btnLogout != null) btnLogout.setOnClickListener(v -> showLogoutDialog());
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
        if (name != null && subtitle != null) {
            subtitle.setText(name);
            if (tvWelcome != null) tvWelcome.setText("Welcome back,");
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

        // Fetch counts for active plans from user data
        databaseReference.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("activeMealPlanId")) {
                    if (tvStatMealPlansValue != null) tvStatMealPlansValue.setText("1");
                } else {
                    if (tvStatMealPlansValue != null) tvStatMealPlansValue.setText("0");
                }
                
                if (snapshot.hasChild("activeWorkoutPlanId")) {
                    if (tvStatWorkoutPlansValue != null) tvStatWorkoutPlansValue.setText("1");
                } else {
                    if (tvStatWorkoutPlansValue != null) tvStatWorkoutPlansValue.setText("0");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadActiveBooking() {
        String userId = sessionManager.getUserId();
        databaseReference.child("bookings").orderByChild("userId").equalTo(userId).limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren() && cardActiveBooking != null) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                Booking b = ds.getValue(Booking.class);
                                if (b != null) {
                                    cardActiveBooking.setVisibility(View.VISIBLE);
                                    if (tvBookingProName != null) tvBookingProName.setText(b.getProfessionalName());
                                    if (tvBookingStatus != null) tvBookingStatus.setText("Status: " + b.getStatus());

                                    if (b.isCompleted() && reviewSection != null) {
                                        reviewSection.setVisibility(View.VISIBLE);
                                        if (btnSubmitReview != null) btnSubmitReview.setOnClickListener(v -> submitReview(ds.getKey(), b));
                                    } else if (reviewSection != null) {
                                        reviewSection.setVisibility(View.GONE);
                                    }
                                }
                            }
                        } else if (cardActiveBooking != null) {
                            cardActiveBooking.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void submitReview(String bookingId, Booking b) {
        if (ratingBar == null || etComment == null) return;
        int rating = (int) ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("bookings").child(bookingId).child("rating").setValue(rating);
        databaseReference.child("bookings").child(bookingId).child("review").setValue(comment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    if (reviewSection != null) reviewSection.setVisibility(View.GONE);
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
}
