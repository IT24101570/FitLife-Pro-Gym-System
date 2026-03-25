package com.example.fit_lifegym;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        bookingsRef.orderByChild("professionalId").equalTo(trainerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        confirmedBookings.clear();
                        Set<String> uniqueUserIds = new HashSet<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Booking b = ds.getValue(Booking.class);
                            if (b != null && "CONFIRMED".equals(b.getStatus())) {
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
                    plan.setDurationWeeks(durStr.isEmpty() ? 0 : Integer.parseInt(durStr));
                    plan.setWorkoutsPerWeek(freqStr.isEmpty() ? 0 : Integer.parseInt(freqStr));
                    plan.setCreatorId(sessionManager.getUserId());

                    assignedWorkoutPlansRef.child(booking.getUserId()).child(plan.getId()).setValue(plan)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Workout plan assigned to " + booking.getUserName(), Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to assign plan", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
        private final List<Booking> members;

        public MemberAdapter(List<Booking> members) {
            this.members = members;
        }

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
            holder.tvDetail.setText("Confirmed Member");
            holder.btnAction.setOnClickListener(v -> showCreateWorkoutPlanDialog(b));
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDetail;
            View btnAction;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvMemberName);
                tvDetail = itemView.findViewById(R.id.tvMemberDetail);
                btnAction = itemView.findViewById(R.id.btnAssignPlan);
            }
        }
    }
}
