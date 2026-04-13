package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.adapters.WorkoutPlanAdapter;
import com.example.fit_lifegym.models.WorkoutPlan;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlansActivity extends AppCompatActivity implements WorkoutPlanAdapter.OnWorkoutPlanClickListener {

    private RecyclerView rvWorkoutPlans;
    private View tvEmpty;
    private ImageView btnBack;
    private WorkoutPlanAdapter adapter;
    private List<WorkoutPlan> workoutPlanList;
    private DatabaseReference workoutPlansRef, assignedRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_plans);

        sessionManager = new SessionManager(this);
        workoutPlansRef = FirebaseDatabase.getInstance().getReference("workoutPlans");
        assignedRef = FirebaseDatabase.getInstance().getReference("assigned_workout_plans").child(sessionManager.getUserId());
        workoutPlanList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadWorkoutPlans();
    }

    private void initViews() {
        rvWorkoutPlans = findViewById(R.id.rvWorkoutPlans);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new WorkoutPlanAdapter(workoutPlanList, this);
        rvWorkoutPlans.setLayoutManager(new LinearLayoutManager(this));
        rvWorkoutPlans.setAdapter(adapter);
    }

    private void loadWorkoutPlans() {
        // Load trainer assigned plans first, then system templates
        assignedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot assignedSnapshot) {
                workoutPlanList.clear();
                for (DataSnapshot ds : assignedSnapshot.getChildren()) {
                    WorkoutPlan plan = ds.getValue(WorkoutPlan.class);
                    if (plan != null) {
                        plan.setId(ds.getKey());
                        workoutPlanList.add(plan);
                    }
                }

                workoutPlansRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot systemSnapshot) {
                        if (systemSnapshot.exists()) {
                            for (DataSnapshot ds : systemSnapshot.getChildren()) {
                                WorkoutPlan plan = ds.getValue(WorkoutPlan.class);
                                if (plan != null) {
                                    plan.setId(ds.getKey());
                                    workoutPlanList.add(plan);
                                }
                            }
                        } else if (workoutPlanList.isEmpty()) {
                            addSampleWorkoutPlans();
                        }
                        adapter.notifyDataSetChanged();
                        if (tvEmpty != null) tvEmpty.setVisibility(workoutPlanList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WorkoutPlansActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSampleWorkoutPlans() {
        WorkoutPlan p1 = new WorkoutPlan("1", "Strength Foundation", "Muscle Gain", "Beginner");
        p1.setDescription("Perfect for beginners looking to build a solid strength base.");
        p1.setDurationWeeks(4); p1.setWorkoutsPerWeek(3);
        workoutPlanList.add(p1);

        WorkoutPlan p2 = new WorkoutPlan("2", "Fat Burn Blitz", "Weight Loss", "Intermediate");
        p2.setDescription("High intensity interval training to maximize calorie burn.");
        p2.setDurationWeeks(6); p2.setWorkoutsPerWeek(5);
        workoutPlanList.add(p2);
    }

    @Override
    public void onActivateClick(WorkoutPlan plan) {
        String userId = sessionManager.getUserId();
        FirebaseDatabase.getInstance().getReference("users").child(userId).child("activeWorkoutPlanId").setValue(plan.getId())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, plan.getName() + " Activated!", Toast.LENGTH_SHORT).show();
                    // Redirect to Tracker as requested
                    startActivity(new Intent(this, WorkoutTrackerActivity.class));
                    finish();
                });
    }
}
