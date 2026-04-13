package com.example.fit_lifegym;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.models.Exercise;
import com.example.fit_lifegym.models.WorkoutSession;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class WorkoutDetailsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvWorkoutType, tvDate;
    private TextView tvDuration, tvCalories, tvSteps;
    private TextView tvSets, tvReps, tvWeight, tvNotes;
    
    private TextView tvDurationLabel, tvCaloriesLabel, tvStepsLabel;
    private TextView tvSetsLabel, tvRepsLabel, tvWeightLabel;
    
    private TextView tvDurationUnit, tvCaloriesUnit, tvStepsUnit;
    private TextView tvSetsUnit, tvRepsUnit, tvWeightUnit;
    
    private DatabaseReference workoutsRef;
    private SessionManager sessionManager;
    private String workoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_details);

        sessionManager = new SessionManager(this);
        workoutId = getIntent().getStringExtra("workoutId");
        workoutsRef = FirebaseDatabase.getInstance().getReference("workoutHistory")
                .child(sessionManager.getUserId()).child(workoutId);

        initializeViews();
        loadWorkoutDetails();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvWorkoutType = findViewById(R.id.tvWorkoutType);
        tvDate = findViewById(R.id.tvDate);
        
        // Duration Stat Card
        android.view.View statDuration = findViewById(R.id.statDuration);
        tvDuration = statDuration.findViewById(R.id.tvStatValue);
        tvDurationLabel = statDuration.findViewById(R.id.tvStatLabel);
        tvDurationUnit = statDuration.findViewById(R.id.tvStatUnit);
        tvDurationLabel.setText("DURATION");
        tvDurationUnit.setText("MINUTES");

        // Calories Stat Card
        android.view.View statCalories = findViewById(R.id.statCalories);
        tvCalories = statCalories.findViewById(R.id.tvStatValue);
        tvCaloriesLabel = statCalories.findViewById(R.id.tvStatLabel);
        tvCaloriesUnit = statCalories.findViewById(R.id.tvStatUnit);
        tvCaloriesLabel.setText("BURNED");
        tvCaloriesUnit.setText("KCAL");

        // Sets Stat Card
        android.view.View statSets = findViewById(R.id.statSets);
        tvSets = statSets.findViewById(R.id.tvStatValue);
        tvSetsLabel = statSets.findViewById(R.id.tvStatLabel);
        tvSetsUnit = statSets.findViewById(R.id.tvStatUnit);
        tvSetsLabel.setText("SETS");
        tvSetsUnit.setText("COMPLETED");

        // Reps Stat Card
        android.view.View statReps = findViewById(R.id.statReps);
        tvReps = statReps.findViewById(R.id.tvStatValue);
        tvRepsLabel = statReps.findViewById(R.id.tvStatLabel);
        tvRepsUnit = statReps.findViewById(R.id.tvStatUnit);
        tvRepsLabel.setText("REPS");
        tvRepsUnit.setText("TOTAL");

        // Weight Stat Card
        android.view.View statWeight = findViewById(R.id.statWeight);
        tvWeight = statWeight.findViewById(R.id.tvStatValue);
        tvWeightLabel = statWeight.findViewById(R.id.tvStatLabel);
        tvWeightUnit = statWeight.findViewById(R.id.tvStatUnit);
        tvWeightLabel.setText("VOLUME");
        tvWeightUnit.setText("KG");

        // Steps/Distance Stat Card
        android.view.View statSteps = findViewById(R.id.statSteps);
        tvSteps = statSteps.findViewById(R.id.tvStatValue);
        tvStepsLabel = statSteps.findViewById(R.id.tvStatLabel);
        tvStepsUnit = statSteps.findViewById(R.id.tvStatUnit);
        tvStepsLabel.setText("ACTIVITY");
        tvStepsUnit.setText("STEPS");

        tvNotes = findViewById(R.id.tvNotes);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadWorkoutDetails() {
        workoutsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                WorkoutSession session = snapshot.getValue(WorkoutSession.class);
                if (session != null) {
                    displayWorkoutDetails(session);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WorkoutDetailsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayWorkoutDetails(WorkoutSession session) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        
        tvWorkoutType.setText(session.getWorkoutName() != null ? session.getWorkoutName() : session.getWorkoutType());
        if (session.getStartTime() != null) {
            tvDate.setText(dateFormat.format(session.getStartTime()));
        }

        if (session.getEndTime() != null && session.getStartTime() != null) {
            long durationMillis = session.getEndTime().getTime() - session.getStartTime().getTime();
            long minutes = durationMillis / (1000 * 60);
            tvDuration.setText(String.valueOf(minutes));
        } else {
            tvDuration.setText(String.valueOf(session.getDurationMinutes()));
        }

        tvCalories.setText(String.valueOf(session.getTotalCaloriesBurned()));
        
        if (session.getTotalSteps() > 0) {
            tvSteps.setText(String.valueOf(session.getTotalSteps()));
        } else {
            tvSteps.setText("0");
        }
        
        // Calculate totals from exercise list
        int totalSets = 0;
        int totalReps = 0;
        double totalWeight = 0;
        
        if (session.getExercises() != null) {
            for (Exercise.ExerciseLog log : session.getExercises()) {
                totalSets += log.getSets();
                totalReps += (log.getSets() * log.getReps());
                totalWeight += (log.getSets() * log.getReps() * log.getWeight());
            }
        }

        tvSets.setText(String.valueOf(totalSets));
        tvReps.setText(String.valueOf(totalReps));
        tvWeight.setText(String.format("%.1f", totalWeight));
        
        if (session.getNotes() != null && !session.getNotes().isEmpty()) {
            tvNotes.setText(session.getNotes());
        } else {
            tvNotes.setText("No notes");
        }
    }
}
