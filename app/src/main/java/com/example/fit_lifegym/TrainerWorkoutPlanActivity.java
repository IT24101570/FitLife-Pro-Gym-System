package com.example.fit_lifegym;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.models.Booking;
import com.example.fit_lifegym.models.WorkoutPlan;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TrainerWorkoutPlanActivity extends AppCompatActivity {

    private RecyclerView rvMembers;
    private View tvEmpty;
    private ImageView btnBack;
    private MemberAdapter adapter;
    private List<Booking> confirmedBookings;
    private DatabaseReference bookingsRef, assignedWorkoutPlansRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_workout_plan);

        sessionManager = new SessionManager(this);
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        assignedWorkoutPlansRef = FirebaseDatabase.getInstance().getReference("assigned_workout_plans");
        confirmedBookings = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadConfirmedMembers();
    }

    private void initViews() {
        rvMembers = findViewById(R.id.rvMembers);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new MemberAdapter(confirmedBookings);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(adapter);
    }

    private void loadConfirmedMembers() {
        String trainerId = sessionManager.getUserId();
        // Showing both CONFIRMED and COMPLETED bookings to allow plan management
        bookingsRef.orderByChild("professionalId").equalTo(trainerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        confirmedBookings.clear();
                        Set<String> uniqueUserIds = new HashSet<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Booking b = ds.getValue(Booking.class);
                            if (b != null && (b.isConfirmed() || b.isCompleted())) {
                                if (!uniqueUserIds.contains(b.getUserId())) {
                                    confirmedBookings.add(b);
                                    uniqueUserIds.add(b.getUserId());
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(confirmedBookings.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void showWorkoutPlansDialog(Booking booking) {
        // Reuse workout plans list layout if available or create a simple one
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_workout_plans, null);
        RecyclerView rvDialogPlans = dialogView.findViewById(R.id.rvWorkoutPlans);
        View tvDialogEmpty = dialogView.findViewById(R.id.tvEmpty);
        ImageView btnDialogBack = dialogView.findViewById(R.id.btnBack);
        if (btnDialogBack != null) btnDialogBack.setVisibility(View.GONE);

        List<WorkoutPlan> assignedPlans = new ArrayList<>();
        WorkoutPlanDialogAdapter dialogAdapter = new WorkoutPlanDialogAdapter(assignedPlans, booking.getUserId());
        rvDialogPlans.setLayoutManager(new LinearLayoutManager(this));
        rvDialogPlans.setAdapter(dialogAdapter);

        ValueEventListener plansListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                assignedPlans.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    WorkoutPlan plan = ds.getValue(WorkoutPlan.class);
                    if (plan != null) {
                        plan.setId(ds.getKey());
                        assignedPlans.add(plan);
                    }
                }
                dialogAdapter.notifyDataSetChanged();
                tvDialogEmpty.setVisibility(assignedPlans.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        assignedWorkoutPlansRef.child(booking.getUserId()).addValueEventListener(plansListener);

        new AlertDialog.Builder(this)
                .setTitle("Plans for " + booking.getUserName())
                .setView(dialogView)
                .setPositiveButton("Assign New", (d, which) -> {
                    assignedWorkoutPlansRef.child(booking.getUserId()).removeEventListener(plansListener);
                    showCreateWorkoutPlanDialog(booking);
                })
                .setNegativeButton("Close", (d, which) -> {
                    assignedWorkoutPlansRef.child(booking.getUserId()).removeEventListener(plansListener);
                })
                .setOnDismissListener(d -> assignedWorkoutPlansRef.child(booking.getUserId()).removeEventListener(plansListener))
                .show();
    }

    private void showCreateWorkoutPlanDialog(Booking booking) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_workout_plan, null);
        TextInputEditText etPlanName = dialogView.findViewById(R.id.etPlanName);
        TextInputEditText etGoal = dialogView.findViewById(R.id.etGoal);
        TextInputEditText etDifficulty = dialogView.findViewById(R.id.etDifficulty);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etDuration = dialogView.findViewById(R.id.etDuration);
        TextInputEditText etFrequency = dialogView.findViewById(R.id.etFrequency);

        new AlertDialog.Builder(this)
                .setTitle("Create Workout Plan for " + booking.getUserName())
                .setView(dialogView)
                .setPositiveButton("Assign", (dialog, which) -> {
                    String name = etPlanName.getText().toString().trim();
                    String goal = etGoal.getText().toString().trim();
                    String diff = etDifficulty.getText().toString().trim();
                    String desc = etDescription.getText().toString().trim();
                    String durStr = etDuration.getText().toString().trim();
                    String freqStr = etFrequency.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(durStr)) {
                        Toast.makeText(this, "Please enter plan name and duration", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    WorkoutPlan plan = new WorkoutPlan();
                    plan.setId(UUID.randomUUID().toString());
                    plan.setName(name);
                    plan.setGoal(goal);
                    plan.setDifficulty(diff);
                    plan.setDescription(desc);
                    try {
                        plan.setDurationWeeks(durStr.isEmpty() ? 0 : Integer.parseInt(durStr));
                        plan.setWorkoutsPerWeek(freqStr.isEmpty() ? 0 : Integer.parseInt(freqStr));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    plan.setCreatorId(sessionManager.getUserId());
                    plan.setCreatorName(sessionManager.getName());

                    assignedWorkoutPlansRef.child(booking.getUserId()).child(plan.getId()).setValue(plan)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Workout plan assigned to " + booking.getUserName(), Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to assign plan", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
        private final List<Booking> members;
        public MemberAdapter(List<Booking> members) { this.members = members; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_confirmed_member, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking b = members.get(position);
            holder.tvName.setText(b.getUserName());
            holder.tvDetail.setText(b.isCompleted() ? "Past Member" : "Confirmed Member");
            
            holder.btnCreatePlan.setOnClickListener(v -> showCreateWorkoutPlanDialog(b));
            holder.btnManagePlans.setOnClickListener(v -> showWorkoutPlansDialog(b));
        }

        @Override
        public int getItemCount() { return members.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDetail;
            MaterialButton btnCreatePlan, btnManagePlans;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvMemberName);
                tvDetail = itemView.findViewById(R.id.tvMemberDetail);
                btnCreatePlan = itemView.findViewById(R.id.btnCreatePlan);
                btnManagePlans = itemView.findViewById(R.id.btnManagePlans);
            }
        }
    }

    private class WorkoutPlanDialogAdapter extends RecyclerView.Adapter<WorkoutPlanDialogAdapter.ViewHolder> {
        private final List<WorkoutPlan> plans;
        private final String userId;

        public WorkoutPlanDialogAdapter(List<WorkoutPlan> plans, String userId) {
            this.plans = plans;
            this.userId = userId;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_plan, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WorkoutPlan plan = plans.get(position);
            holder.tvName.setText(plan.getName());
            holder.tvGoal.setText(plan.getGoal());
            holder.tvDifficulty.setText(plan.getDifficulty());
            holder.tvDuration.setText(plan.getDurationWeeks() + " Weeks");
            holder.tvWorkouts.setText(plan.getWorkoutsPerWeek() + "/Week");
            holder.tvDescription.setText(plan.getDescription());
            
            if (plan.getCreatorName() != null) {
                holder.tvTrainerName.setText("Trainer: " + plan.getCreatorName());
            } else {
                holder.tvTrainerName.setText("Trainer: System");
            }

            // For management, use Activate button as Edit and add a Delete option
            holder.btnActivate.setText("Edit");
            holder.btnActivate.setOnClickListener(v -> showEditWorkoutPlanDialog(userId, plan));
            
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(TrainerWorkoutPlanActivity.this)
                        .setTitle("Delete Plan")
                        .setMessage("Are you sure you want to delete this workout plan?")
                        .setPositiveButton("Delete", (d, w) -> {
                            assignedWorkoutPlansRef.child(userId).child(plan.getId()).removeValue()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(TrainerWorkoutPlanActivity.this, "Plan deleted", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() { return plans.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvGoal, tvDifficulty, tvDuration, tvWorkouts, tvDescription, tvTrainerName;
            MaterialButton btnActivate;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvPlanName);
                tvGoal = itemView.findViewById(R.id.tvGoal);
                tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
                tvDuration = itemView.findViewById(R.id.tvDuration);
                tvWorkouts = itemView.findViewById(R.id.tvWorkouts);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvTrainerName = itemView.findViewById(R.id.tvTrainerName);
                btnActivate = itemView.findViewById(R.id.btnActivate);
            }
        }
    }

    private void showEditWorkoutPlanDialog(String userId, WorkoutPlan plan) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_workout_plan, null);
        TextInputEditText etPlanName = dialogView.findViewById(R.id.etPlanName);
        TextInputEditText etGoal = dialogView.findViewById(R.id.etGoal);
        TextInputEditText etDifficulty = dialogView.findViewById(R.id.etDifficulty);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etDuration = dialogView.findViewById(R.id.etDuration);
        TextInputEditText etFrequency = dialogView.findViewById(R.id.etFrequency);

        etPlanName.setText(plan.getName());
        etGoal.setText(plan.getGoal());
        etDifficulty.setText(plan.getDifficulty());
        etDescription.setText(plan.getDescription());
        etDuration.setText(String.valueOf(plan.getDurationWeeks()));
        etFrequency.setText(String.valueOf(plan.getWorkoutsPerWeek()));

        new AlertDialog.Builder(this)
                .setTitle("Edit Workout Plan")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    plan.setName(etPlanName.getText().toString().trim());
                    plan.setGoal(etGoal.getText().toString().trim());
                    plan.setDifficulty(etDifficulty.getText().toString().trim());
                    plan.setDescription(etDescription.getText().toString().trim());
                    try {
                        plan.setDurationWeeks(Integer.parseInt(etDuration.getText().toString().trim()));
                        plan.setWorkoutsPerWeek(Integer.parseInt(etFrequency.getText().toString().trim()));
                    } catch (NumberFormatException ignored) {}

                    assignedWorkoutPlansRef.child(userId).child(plan.getId()).setValue(plan)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Plan updated", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
