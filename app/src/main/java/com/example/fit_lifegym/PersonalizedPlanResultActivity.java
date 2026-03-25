package com.example.fit_lifegym;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fit_lifegym.models.DoctorPersonalizedMealPlan;
import com.example.fit_lifegym.models.FitnessGoalSubmission;
import com.example.fit_lifegym.models.TrainerGuidance;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PersonalizedPlanResultActivity extends AppCompatActivity {

    private TextView tvSelectedGoal, tvSubmissionDate, tvTrainerInfo, tvWeeklyTarget, tvTrainerAdvice;
    private TextView tvDoctorInfo, tvBreakfast, tvLunch, tvDinner, tvSnacks, tvNutritionNotes, tvWaitingMessage;
    private View cardTrainerGuidance, cardDoctorPlan;

    private DatabaseReference dbRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalized_plan_result);

        sessionManager = new SessionManager(this);
        dbRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        loadData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvSelectedGoal = findViewById(R.id.tvSelectedGoal);
        tvSubmissionDate = findViewById(R.id.tvSubmissionDate);
        tvTrainerInfo = findViewById(R.id.tvTrainerInfo);
        tvWeeklyTarget = findViewById(R.id.tvWeeklyTarget);
        tvTrainerAdvice = findViewById(R.id.tvTrainerAdvice);
        tvDoctorInfo = findViewById(R.id.tvDoctorInfo);
        tvBreakfast = findViewById(R.id.tvBreakfast);
        tvLunch = findViewById(R.id.tvLunch);
        tvDinner = findViewById(R.id.tvDinner);
        tvSnacks = findViewById(R.id.tvSnacks);
        tvNutritionNotes = findViewById(R.id.tvNutritionNotes);
        tvWaitingMessage = findViewById(R.id.tvWaitingMessage);

        cardTrainerGuidance = findViewById(R.id.cardTrainerGuidance);
        cardDoctorPlan = findViewById(R.id.cardDoctorPlan);
    }

    private void loadData() {
        String userId = sessionManager.getUserId();

        // 1. Load Submission
        dbRef.child("fitness_submissions").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FitnessGoalSubmission sub = snapshot.getValue(FitnessGoalSubmission.class);
                if (sub != null) {
                    tvSelectedGoal.setText(sub.getSelectedGoal());
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    tvSubmissionDate.setText("Submitted on " + sdf.format(new Date(sub.getSubmittedDate())));
                    
                    if ("PENDING".equals(sub.getStatus())) {
                        tvWaitingMessage.setVisibility(View.VISIBLE);
                        cardTrainerGuidance.setVisibility(View.GONE);
                        cardDoctorPlan.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 2. Load Trainer Guidance
        dbRef.child("trainer_guidance").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TrainerGuidance g = snapshot.getValue(TrainerGuidance.class);
                if (g != null) {
                    cardTrainerGuidance.setVisibility(View.VISIBLE);
                    tvTrainerInfo.setText("Assigned by Trainer: " + g.getTrainerName());
                    tvWeeklyTarget.setText(g.getWeeklyTarget());
                    tvTrainerAdvice.setText(g.getAdvice());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 3. Load Doctor Plan
        dbRef.child("doctor_personalized_meal_plans").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DoctorPersonalizedMealPlan p = snapshot.getValue(DoctorPersonalizedMealPlan.class);
                if (p != null) {
                    cardDoctorPlan.setVisibility(View.VISIBLE);
                    tvDoctorInfo.setText("Assigned by Doctor: " + p.getDoctorName());
                    tvBreakfast.setText(p.getBreakfastPlan());
                    tvLunch.setText(p.getLunchPlan());
                    tvDinner.setText(p.getDinnerPlan());
                    tvSnacks.setText(p.getSnacksPlan());
                    tvNutritionNotes.setText(p.getNotes());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
