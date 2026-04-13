package com.example.fit_lifegym;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.adapters.ExerciseAdapter;
import com.example.fit_lifegym.models.Exercise;
import com.example.fit_lifegym.models.Post;
import com.example.fit_lifegym.models.WorkoutPlan;
import com.example.fit_lifegym.models.WorkoutSession;
import com.example.fit_lifegym.utils.FirebaseHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.example.fit_lifegym.utils.ValidationUtils;
import com.example.fit_lifegym.utils.WearableManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class WorkoutTrackerActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private WearableManager wearableManager;
    private WorkoutSession currentWorkout;
    private List<Exercise.ExerciseLog> exerciseList;
    private ExerciseAdapter exerciseAdapter;
    private DatabaseReference workoutsRef, postsRef, userRef, workoutPlansRef, assignedRef;
    
    private TextView tvTimer, tvExerciseCount, tvCalories, tvHeartRate, tvSteps, tvEmptyState;
    private TextView tvStatWeeklyValue, tvStatTotalValue;
    private RecyclerView rvExercises;
    private TextView btnAddExercise, btnHistory;
    private Button btnFinishWorkout;
    private ImageView btnBack;
    private BarChart workoutChart;
    
    // Active Plan UI
    private View cardActivePlan;
    private TextView tvActivePlanName, tvActivePlanGoal;
    private Button btnDeactivate;
    
    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_tracker);

        sessionManager = new SessionManager(this);
        wearableManager = new WearableManager(this);
        workoutsRef = FirebaseHelper.getWorkoutsRef().child(sessionManager.getUserId());
        postsRef = FirebaseHelper.getPostsRef();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(sessionManager.getUserId());
        workoutPlansRef = FirebaseDatabase.getInstance().getReference("workoutPlans");
        assignedRef = FirebaseDatabase.getInstance().getReference("assigned_workout_plans").child(sessionManager.getUserId());
        
        initViews();
        initWorkout();
        setupRecyclerView();
        setupListeners();
        setupChart();
        startTimer();
        loadWorkoutStats();
        loadActiveWorkoutPlan();
        
        if (wearableManager.hasPermissions()) {
            startWearableTracking();
        } else {
            wearableManager.requestPermissions(this);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnHistory = findViewById(R.id.btnHistory);
        tvTimer = findViewById(R.id.tvTimer);
        tvExerciseCount = findViewById(R.id.tvExerciseCount);
        tvCalories = findViewById(R.id.tvCalories);
        tvHeartRate = findViewById(R.id.tvHeartRate);
        tvSteps = findViewById(R.id.tvSteps);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvExercises = findViewById(R.id.rvExercises);
        btnAddExercise = findViewById(R.id.btnAddExercise);
        btnFinishWorkout = findViewById(R.id.btnFinishWorkout);
        workoutChart = findViewById(R.id.workoutChart);

        cardActivePlan = findViewById(R.id.cardActivePlan);
        tvActivePlanName = findViewById(R.id.tvActivePlanName);
        tvActivePlanGoal = findViewById(R.id.tvActivePlanGoal);
        btnDeactivate = findViewById(R.id.btnDeactivate);

        View statWeekly = findViewById(R.id.statWeekly);
        if (statWeekly != null) {
            ((TextView)statWeekly.findViewById(R.id.tvStatLabel)).setText(R.string.label_this_week);
            ((TextView)statWeekly.findViewById(R.id.tvStatUnit)).setText(R.string.label_workouts);
            tvStatWeeklyValue = statWeekly.findViewById(R.id.tvStatValue);
        }

        View statTotal = findViewById(R.id.statTotal);
        if (statTotal != null) {
            ((TextView)statTotal.findViewById(R.id.tvStatLabel)).setText(R.string.label_all_time);
            ((TextView)statTotal.findViewById(R.id.tvStatUnit)).setText(R.string.label_sessions);
            tvStatTotalValue = statTotal.findViewById(R.id.tvStatValue);
        }
    }

    private void initWorkout() {
        currentWorkout = new WorkoutSession();
        currentWorkout.setId(UUID.randomUUID().toString());
        currentWorkout.setUserId(sessionManager.getUserId());
        currentWorkout.setStartTime(new Date());
        currentWorkout.setWorkoutType(getString(R.string.workout_type_general));
        
        exerciseList = new ArrayList<>();
        startTime = System.currentTimeMillis();
    }

    private void loadActiveWorkoutPlan() {
        userRef.child("activeWorkoutPlanId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String planId = snapshot.getValue(String.class);
                if (planId != null) {
                    fetchPlanDetails(planId);
                } else {
                    cardActivePlan.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchPlanDetails(String planId) {
        assignedRef.child(planId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    applyWorkoutPlan(snapshot.getValue(WorkoutPlan.class));
                } else {
                    workoutPlansRef.child(planId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot systemSnap) {
                            if (systemSnap.exists()) {
                                applyWorkoutPlan(systemSnap.getValue(WorkoutPlan.class));
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void applyWorkoutPlan(WorkoutPlan plan) {
        if (plan == null) return;
        cardActivePlan.setVisibility(View.VISIBLE);
        tvActivePlanName.setText(plan.getName());
        StringBuilder info = new StringBuilder(getString(R.string.label_goal_prefix)).append(plan.getGoal()).append(" • ").append(plan.getDifficulty());
        if (plan.getDurationWeeks() > 0) {
            info.append(" • ").append(plan.getDurationWeeks()).append(getString(R.string.label_weeks_suffix));
        }
        if (plan.getCreatorName() != null) {
            info.append(" • ").append(getString(R.string.label_trainer_prefix)).append(plan.getCreatorName());
        }
        tvActivePlanGoal.setText(info.toString());
    }

    private void deactivatePlan() {
        userRef.child("activeWorkoutPlanId").removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, R.string.msg_plan_deactivated, Toast.LENGTH_SHORT).show());
    }

    private void startWearableTracking() {
        wearableManager.startSensorTracking(new WearableManager.OnHealthDataListener() {
            @Override
            public void onHeartRateUpdate(int bpm) {
                runOnUiThread(() -> {
                    if (tvHeartRate != null) tvHeartRate.setText(String.valueOf(bpm));
                });
            }

            @Override
            public void onStepCountUpdate(int steps) {
                runOnUiThread(() -> {
                    if (tvSteps != null) tvSteps.setText(String.valueOf(steps));
                });
            }
        });
    }

    private void setupRecyclerView() {
        exerciseAdapter = new ExerciseAdapter(this, exerciseList, 
            new ExerciseAdapter.OnExerciseActionListener() {
                @Override
                public void onEdit(int position) {
                    showEditExerciseDialog(position);
                }
                @Override
                public void onDelete(int position) {
                    exerciseList.remove(position);
                    exerciseAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    updateLiveStats();
                }
            });
        
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        rvExercises.setAdapter(exerciseAdapter);
        updateEmptyState();
    }

    private void showEditExerciseDialog(int position) {
        Exercise.ExerciseLog exerciseToEdit = exerciseList.get(position);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exercise, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etExerciseName);
        TextInputEditText etSets = dialogView.findViewById(R.id.etSets);
        TextInputEditText etReps = dialogView.findViewById(R.id.etReps);
        TextInputEditText etWeight = dialogView.findViewById(R.id.etWeight);

        etName.setText(exerciseToEdit.getExerciseName());
        etSets.setText(String.valueOf(exerciseToEdit.getSets()));
        etReps.setText(String.valueOf(exerciseToEdit.getReps()));
        etWeight.setText(exerciseToEdit.getWeight() > 0 ? String.valueOf(exerciseToEdit.getWeight()) : "");

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_Fit_lifeGym)
            .setTitle(R.string.title_update_activity)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(R.string.action_update_exercise, null)
            .setNegativeButton(R.string.dialog_cancel, (d, w) -> d.dismiss())
            .create();

        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.gold_primary));

        positiveButton.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String setsStr = etSets.getText().toString().trim();
            String repsStr = etReps.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();

            if (!ValidationUtils.isValidExerciseName(name)) {
                etName.setError(getString(R.string.error_invalid_name));
                return;
            }

            if (!ValidationUtils.isValidSets(setsStr)) {
                etSets.setError(getString(R.string.error_invalid_sets));
                return;
            }

            if (!ValidationUtils.isValidReps(repsStr)) {
                etReps.setError(getString(R.string.error_invalid_reps));
                return;
            }

            if (!weightStr.isEmpty() && !ValidationUtils.isValidWeight(weightStr)) {
                etWeight.setError(getString(R.string.error_invalid_weight));
                return;
            }

            exerciseToEdit.setExerciseName(name);
            exerciseToEdit.setSets(Integer.parseInt(setsStr));
            exerciseToEdit.setReps(Integer.parseInt(repsStr));
            if (!weightStr.isEmpty()) {
                exerciseToEdit.setWeight(Double.parseDouble(weightStr));
            } else {
                exerciseToEdit.setWeight(0);
            }

            exerciseAdapter.notifyItemChanged(position);
            updateLiveStats();
            dialog.dismiss();
            Toast.makeText(this, "Activity updated", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnHistory != null) btnHistory.setOnClickListener(v -> startActivity(new Intent(this, WorkoutHistoryActivity.class)));
        if (btnAddExercise != null) btnAddExercise.setOnClickListener(v -> showAddExerciseDialog());
        if (btnFinishWorkout != null) btnFinishWorkout.setOnClickListener(v -> showFinishWorkoutDialog());
        if (btnDeactivate != null) btnDeactivate.setOnClickListener(v -> deactivatePlan());
    }

    private void setupChart() {
        if (workoutChart == null) return;
        workoutChart.getDescription().setEnabled(false);
        workoutChart.setDrawGridBackground(false);
        workoutChart.getLegend().setEnabled(false);
        workoutChart.setTouchEnabled(false);
        
        XAxis xAxis = workoutChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.GRAY);
        
        workoutChart.getAxisLeft().setTextColor(Color.GRAY);
        workoutChart.getAxisRight().setEnabled(false);
    }

    private void loadWorkoutStats() {
        workoutsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = (int) snapshot.getChildrenCount();
                if (tvStatTotalValue != null) tvStatTotalValue.setText(String.valueOf(total));
                
                List<BarEntry> entries = new ArrayList<>();
                for (int i = 0; i < 7; i++) entries.add(new BarEntry(i, (float) (Math.random() * 60 + 20)));

                BarDataSet dataSet = new BarDataSet(entries, "Minutes");
                dataSet.setColor(getResources().getColor(R.color.accent));
                dataSet.setDrawValues(false);

                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.6f);
                
                if (workoutChart != null) {
                    workoutChart.setData(barData);
                    String[] days = {"M", "T", "W", "T", "F", "S", "S"};
                    workoutChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days));
                    workoutChart.invalidate();
                }
                
                if (tvStatWeeklyValue != null) tvStatWeeklyValue.setText("4"); 
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void startTimer() {
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                
                if (tvTimer != null) tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void showAddExerciseDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exercise, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etExerciseName);
        TextInputEditText etSets = dialogView.findViewById(R.id.etSets);
        TextInputEditText etReps = dialogView.findViewById(R.id.etReps);
        TextInputEditText etWeight = dialogView.findViewById(R.id.etWeight);
        
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_Fit_lifeGym)
            .setTitle(R.string.title_add_activity)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(R.string.action_add_to_session, null)
            .setNegativeButton(R.string.dialog_cancel, (d, w) -> d.dismiss())
            .create();

        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.gold_primary));

        positiveButton.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String setsStr = etSets.getText().toString().trim();
            String repsStr = etReps.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();

            if (!ValidationUtils.isValidExerciseName(name)) {
                etName.setError(getString(R.string.error_invalid_name));
                return;
            }

            if (!ValidationUtils.isValidSets(setsStr)) {
                etSets.setError(getString(R.string.error_invalid_sets));
                return;
            }

            if (!ValidationUtils.isValidReps(repsStr)) {
                etReps.setError(getString(R.string.error_invalid_reps));
                return;
            }

            if (!weightStr.isEmpty() && !ValidationUtils.isValidWeight(weightStr)) {
                etWeight.setError(getString(R.string.error_invalid_weight));
                return;
            }

            Exercise.ExerciseLog log = new Exercise.ExerciseLog();
            log.setExerciseName(name);
            log.setSets(Integer.parseInt(setsStr));
            log.setReps(Integer.parseInt(repsStr));
            if (!weightStr.isEmpty()) log.setWeight(Double.parseDouble(weightStr));
            
            exerciseList.add(log);
            exerciseAdapter.notifyDataSetChanged();
            updateEmptyState();
            updateLiveStats();
            dialog.dismiss();
            Toast.makeText(this, getString(R.string.msg_activity_added, name), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLiveStats() {
        if (tvExerciseCount != null) tvExerciseCount.setText(String.valueOf(exerciseList.size()));
        int cals = 0;
        for(Exercise.ExerciseLog e : exerciseList) cals += (e.getSets() * e.getReps() * 2);
        if (tvCalories != null) tvCalories.setText(String.valueOf(cals));
    }

    private void showFinishWorkoutDialog() {
        if (exerciseList.isEmpty()) {
            Toast.makeText(this, R.string.msg_add_exercise_first, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this, R.style.Theme_Fit_lifeGym)
            .setTitle(R.string.title_end_session)
            .setMessage(R.string.msg_end_session_confirm)
            .setPositiveButton(R.string.action_finish_share, (d, w) -> {
                saveWorkout(true);
            })
            .setNeutralButton(R.string.action_finish_only, (d, w) -> {
                saveWorkout(false);
            })
            .setNegativeButton(R.string.action_keep_grinding, null)
            .show();
    }

    private void saveWorkout(boolean share) {
        currentWorkout.setExercises(exerciseList);
        currentWorkout.setCompleted(true);
        currentWorkout.setEndTime(new Date());
        try {
            currentWorkout.setTotalCaloriesBurned(Integer.parseInt(tvCalories.getText().toString()));
        } catch (NumberFormatException e) {
            currentWorkout.setTotalCaloriesBurned(0);
        }
        
        workoutsRef.child(currentWorkout.getId()).setValue(currentWorkout)
            .addOnSuccessListener(aVoid -> {
                if (share) {
                    shareToCommunity();
                } else {
                    Toast.makeText(this, R.string.msg_session_logged, Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void shareToCommunity() {
        String content = getString(R.string.share_workout_template, 
                tvTimer.getText().toString(), 
                tvCalories.getText().toString(), 
                exerciseList.size());
        
        Post post = new Post(sessionManager.getUserId(), sessionManager.getName(), content, "WORKOUT");
        post.setWorkoutSessionId(currentWorkout.getId());
        
        String postId = postsRef.push().getKey();
        if (postId != null) {
            postsRef.child(postId).setValue(post)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, R.string.msg_session_shared, Toast.LENGTH_SHORT).show();
                    finish();
                });
        }
    }

    private void updateEmptyState() {
        if (exerciseList.isEmpty()) {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
            if (rvExercises != null) rvExercises.setVisibility(View.GONE);
        } else {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
            if (rvExercises != null) rvExercises.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WearableManager.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) startWearableTracking();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null) timerHandler.removeCallbacks(timerRunnable);
        if (wearableManager != null) wearableManager.stopSensorTracking();
    }
}
