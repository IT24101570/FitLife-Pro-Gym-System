package com.example.fit_lifegym;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.models.ExerciseLibrary;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ExerciseDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvExerciseName, tvCategory, tvDescription;
    private TextView tvStatDifficultyValue, tvStatMuscleValue, tvStatEquipmentValue;
    private MaterialButton btnAddToWorkout, btnFavorite;
    
    private DatabaseReference exercisesRef;
    private SessionManager sessionManager;
    private String exerciseId;
    private ExerciseLibrary exercise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        sessionManager = new SessionManager(this);
        exercisesRef = FirebaseDatabase.getInstance().getReference("exercises");
        
        exerciseId = getIntent().getStringExtra("exerciseId");
        if (exerciseId == null) exerciseId = "ex1"; // Fallback for testing
        
        initViews();
        setupListeners();
        loadExerciseDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvExerciseName = findViewById(R.id.tvExerciseName);
        tvCategory = findViewById(R.id.tvCategory);
        tvDescription = findViewById(R.id.tvDescription);
        
        View statDifficulty = findViewById(R.id.statDifficulty);
        ((TextView)statDifficulty.findViewById(R.id.tvStatLabel)).setText("Level");
        tvStatDifficultyValue = statDifficulty.findViewById(R.id.tvStatValue);
        ((TextView)statDifficulty.findViewById(R.id.tvStatUnit)).setText("Difficulty");

        View statMuscle = findViewById(R.id.statMuscle);
        ((TextView)statMuscle.findViewById(R.id.tvStatLabel)).setText("Target");
        tvStatMuscleValue = statMuscle.findViewById(R.id.tvStatValue);
        ((TextView)statMuscle.findViewById(R.id.tvStatUnit)).setText("Muscle Group");

        View statEquipment = findViewById(R.id.statEquipment);
        ((TextView)statEquipment.findViewById(R.id.tvStatLabel)).setText("Using");
        tvStatEquipmentValue = statEquipment.findViewById(R.id.tvStatValue);
        ((TextView)statEquipment.findViewById(R.id.tvStatUnit)).setText("Equipment");

        btnAddToWorkout = findViewById(R.id.btnAddToWorkout);
        btnFavorite = findViewById(R.id.btnFavorite);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnAddToWorkout.setOnClickListener(v -> {
            Toast.makeText(this, "Added to Routine!", Toast.LENGTH_SHORT).show();
            finish();
        });
        
        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void loadExerciseDetails() {
        exercisesRef.child(exerciseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                exercise = snapshot.getValue(ExerciseLibrary.class);
                if (exercise != null) {
                    displayExerciseDetails();
                } else {
                    // Sample data for demo if not in DB
                    exercise = new ExerciseLibrary(exerciseId, "Bench Press", "Classic strength move.", "Chest", "Barbell", "Intermediate", "", "Lower bar to chest, press up.", "", 10);
                    displayExerciseDetails();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void displayExerciseDetails() {
        tvExerciseName.setText(exercise.getName());
        tvCategory.setText(exercise.getMuscleGroup() + " Training");
        tvDescription.setText(exercise.getInstructions());
        
        tvStatDifficultyValue.setText(exercise.getDifficulty());
        tvStatMuscleValue.setText(exercise.getMuscleGroup());
        tvStatEquipmentValue.setText(exercise.getEquipment());
        
        updateFavoriteButton();
    }

    private void toggleFavorite() {
        if (exercise != null) {
            exercise.setFavorite(!exercise.isFavorite());
            updateFavoriteButton();
            Toast.makeText(this, exercise.isFavorite() ? "Saved!" : "Removed", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFavoriteButton() {
        if (exercise != null && exercise.isFavorite()) {
            btnFavorite.setText("Saved to Favorites");
        } else {
            btnFavorite.setText("Save to Favorites");
        }
    }
}
