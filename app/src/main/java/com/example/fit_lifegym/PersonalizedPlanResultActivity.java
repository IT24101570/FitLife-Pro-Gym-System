package com.example.fit_lifegym;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.adapters.PhotoAdapter;
import com.example.fit_lifegym.models.DoctorPersonalizedMealPlan;
import com.example.fit_lifegym.models.FitnessGoalSubmission;
import com.example.fit_lifegym.models.TrainerGuidance;
import com.example.fit_lifegym.utils.FirebaseHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PersonalizedPlanResultActivity extends AppCompatActivity {

    private TextView tvSelectedGoal, tvSubmissionDate, tvMemberNotes, tvWaitingMessage, tvPhotosLabel;
    private RecyclerView rvPhotos;
    private MaterialCardView cardTrainerGuidance, cardDoctorPlan;
    private TextView tvTrainerInfo, tvWeeklyTarget, tvTrainerAdvice, tvVideoLinks;
    private TextView tvDoctorInfo, tvBreakfast, tvLunch, tvDinner, tvSnacks, tvNutritionNotes;

    private DatabaseReference mDatabase;
    private SessionManager sessionManager;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalized_plan_result);

        initViews();
        setupToolbar();

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        mDatabase = FirebaseHelper.getDbRef();

        loadSubmissionData();
        loadTrainerGuidance();
        loadDoctorMealPlan();
    }

    private void initViews() {
        tvSelectedGoal = findViewById(R.id.tvSelectedGoal);
        tvSubmissionDate = findViewById(R.id.tvSubmissionDate);
        tvMemberNotes = findViewById(R.id.tvMemberNotes);
        tvWaitingMessage = findViewById(R.id.tvWaitingMessage);
        tvPhotosLabel = findViewById(R.id.tvPhotosLabel);
        rvPhotos = findViewById(R.id.rvPhotos);
        cardTrainerGuidance = findViewById(R.id.cardTrainerGuidance);
        cardDoctorPlan = findViewById(R.id.cardDoctorPlan);

        tvTrainerInfo = findViewById(R.id.tvTrainerInfo);
        tvWeeklyTarget = findViewById(R.id.tvWeeklyTarget);
        tvTrainerAdvice = findViewById(R.id.tvTrainerAdvice);
        tvVideoLinks = findViewById(R.id.tvVideoLinks);

        tvDoctorInfo = findViewById(R.id.tvDoctorInfo);
        tvBreakfast = findViewById(R.id.tvBreakfast);
        tvLunch = findViewById(R.id.tvLunch);
        tvDinner = findViewById(R.id.tvDinner);
        tvSnacks = findViewById(R.id.tvSnacks);
        tvNutritionNotes = findViewById(R.id.tvNutritionNotes);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void loadSubmissionData() {
        mDatabase.child("personalized_plans").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            try {
                                FitnessGoalSubmission submission = snapshot.getValue(FitnessGoalSubmission.class);
                                if (submission != null) {
                                    tvSelectedGoal.setText("Goal: " + submission.getSelectedGoal());

                                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                                    String dateStr = sdf.format(new Date(submission.getTimestamp()));
                                    tvSubmissionDate.setText("Submitted on " + dateStr);

                                    String notes = submission.getNotes();
                                    tvMemberNotes.setText(notes != null && !notes.isEmpty() ? notes : "No additional notes.");

                                    if (submission.getPhotoUrls() != null && !submission.getPhotoUrls().isEmpty()) {
                                        tvPhotosLabel.setVisibility(View.VISIBLE);
                                        rvPhotos.setVisibility(View.VISIBLE);
                                        rvPhotos.setAdapter(new PhotoAdapter(submission.getPhotoUrls()));
                                    }
                                }
                            } catch (Exception e) {
                                android.util.Log.e("FirebaseError", "Failed to parse submission", e);
                                Toast.makeText(PersonalizedPlanResultActivity.this, "Invalid data format found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PersonalizedPlanResultActivity.this, "Error loading submission", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTrainerGuidance() {
        mDatabase.child("trainer_guidance").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            TrainerGuidance guidance = snapshot.getValue(TrainerGuidance.class);
                            if (guidance != null) {
                                cardTrainerGuidance.setVisibility(View.VISIBLE);
                                tvTrainerInfo.setText("Assigned by Trainer " + guidance.getTrainerName());
                                tvWeeklyTarget.setText(guidance.getWeeklyTarget());
                                tvTrainerAdvice.setText(guidance.getAdvice());
                                
                                String links = guidance.getVideoLinks();
                                tvVideoLinks.setText(links != null && !links.isEmpty() ? links : "No links provided.");
                                checkWaitingMessage();
                            }
                        } else {
                            cardTrainerGuidance.setVisibility(View.GONE);
                            checkWaitingMessage();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadDoctorMealPlan() {
        mDatabase.child("doctor_personalized_meal_plans").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            DoctorPersonalizedMealPlan plan = snapshot.getValue(DoctorPersonalizedMealPlan.class);
                            if (plan != null) {
                                cardDoctorPlan.setVisibility(View.VISIBLE);
                                tvDoctorInfo.setText("Assigned by Dr. " + plan.getDoctorName());
                                tvBreakfast.setText(plan.getBreakfastPlan());
                                tvLunch.setText(plan.getLunchPlan());
                                tvDinner.setText(plan.getDinnerPlan());
                                tvSnacks.setText(plan.getSnacksPlan());
                                tvNutritionNotes.setText(plan.getNotes());
                                checkWaitingMessage();
                            }
                        } else {
                            cardDoctorPlan.setVisibility(View.GONE);
                            checkWaitingMessage();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void checkWaitingMessage() {
        if (cardTrainerGuidance.getVisibility() == View.GONE && cardDoctorPlan.getVisibility() == View.GONE) {
            tvWaitingMessage.setVisibility(View.VISIBLE);
        } else {
            tvWaitingMessage.setVisibility(View.GONE);
        }
    }
}
