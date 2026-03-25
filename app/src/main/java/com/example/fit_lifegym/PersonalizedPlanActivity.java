package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fit_lifegym.models.FitnessGoalSubmission;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class PersonalizedPlanActivity extends AppCompatActivity {

    private RadioGroup rgGoal;
    private TextInputEditText etNotes;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalized_plan);

        sessionManager = new SessionManager(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("fitness_submissions");

        initViews();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rgGoal = findViewById(R.id.rgGoal);
        etNotes = findViewById(R.id.etNotes);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        btnSubmit.setOnClickListener(v -> submitPlan());
    }

    private void submitPlan() {
        int selectedId = rgGoal.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a goal", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton rb = findViewById(selectedId);
        String goal = rb.getText().toString();
        String notes = etNotes.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        List<String> photoUrls = new ArrayList<>();
        // Photo upload skipped as requested

        String userId = sessionManager.getUserId();
        String userName = sessionManager.getName();
        FitnessGoalSubmission submission = new FitnessGoalSubmission(
                userId, userId, userName, goal, notes, photoUrls, System.currentTimeMillis()
        );

        databaseReference.child(userId).setValue(submission)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Plan request submitted successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
