package com.example.fit_lifegym;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.models.DoctorPersonalizedMealPlan;
import com.example.fit_lifegym.models.FitnessGoalSubmission;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DoctorPersonalizedPlanActivity extends AppCompatActivity {

    private RecyclerView rvSubmissions;
    private TextView tvEmpty;
    private SubmissionAdapter adapter;
    private List<FitnessGoalSubmission> submissionList;
    private DatabaseReference submissionsRef, mealPlanRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_personalized_plan);

        sessionManager = new SessionManager(this);
        submissionsRef = FirebaseDatabase.getInstance().getReference("fitness_submissions");
        mealPlanRef = FirebaseDatabase.getInstance().getReference("doctor_personalized_meal_plans");
        submissionList = new ArrayList<>();

        initViews();
        loadSubmissions();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvSubmissions = findViewById(R.id.rvSubmissions);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvSubmissions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubmissionAdapter(submissionList);
        rvSubmissions.setAdapter(adapter);
    }

    private void loadSubmissions() {
        submissionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                submissionList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    FitnessGoalSubmission sub = ds.getValue(FitnessGoalSubmission.class);
                    if (sub != null) {
                        submissionList.add(sub);
                    }
                }
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(submissionList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showMealPlanDialog(FitnessGoalSubmission submission) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_doctor_meal_plan, null);
        TextInputEditText etBreakfast = view.findViewById(R.id.etBreakfast);
        TextInputEditText etLunch = view.findViewById(R.id.etLunch);
        TextInputEditText etDinner = view.findViewById(R.id.etDinner);
        TextInputEditText etSnacks = view.findViewById(R.id.etSnacks);
        TextInputEditText etNotes = view.findViewById(R.id.etNotes);

        new AlertDialog.Builder(this)
                .setTitle("Meal Plan for " + submission.getMemberName())
                .setView(view)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String b = etBreakfast.getText().toString().trim();
                    String l = etLunch.getText().toString().trim();
                    String d = etDinner.getText().toString().trim();
                    String s = etSnacks.getText().toString().trim();
                    String notes = etNotes.getText().toString().trim();

                    if (TextUtils.isEmpty(b) || TextUtils.isEmpty(l) || TextUtils.isEmpty(d)) {
                        Toast.makeText(this, "Please fill main meals", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DoctorPersonalizedMealPlan plan = new DoctorPersonalizedMealPlan();
                    plan.setId(UUID.randomUUID().toString());
                    plan.setSubmissionId(submission.getMemberId());
                    plan.setDoctorId(sessionManager.getUserId());
                    plan.setDoctorName(sessionManager.getName());
                    plan.setBreakfastPlan(b);
                    plan.setLunchPlan(l);
                    plan.setDinnerPlan(d);
                    plan.setSnacksPlan(s);
                    plan.setNotes(notes);
                    plan.setUpdatedDate(System.currentTimeMillis());

                    mealPlanRef.child(submission.getMemberId()).setValue(plan)
                            .addOnSuccessListener(aVoid -> {
                                submissionsRef.child(submission.getMemberId()).child("status").setValue("DOCTOR_UPDATED");
                                Toast.makeText(this, "Meal plan submitted!", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.ViewHolder> {
        private final List<FitnessGoalSubmission> submissions;
        public SubmissionAdapter(List<FitnessGoalSubmission> submissions) { this.submissions = submissions; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal_submission, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FitnessGoalSubmission sub = submissions.get(position);
            holder.tvName.setText(sub.getMemberName());
            holder.tvGoal.setText("Goal: " + sub.getSelectedGoal());
            holder.tvNotes.setText("Notes: " + sub.getNotes());
            holder.tvStatus.setText(sub.getStatus());
            holder.btnAction.setText("Provide Meal Plan");
            holder.btnAction.setOnClickListener(v -> showMealPlanDialog(sub));
        }

        @Override
        public int getItemCount() { return submissions.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvGoal, tvNotes, tvStatus;
            MaterialButton btnAction;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvMemberName);
                tvGoal = v.findViewById(R.id.tvGoal);
                tvNotes = v.findViewById(R.id.tvNotes);
                tvStatus = v.findViewById(R.id.tvStatus);
                btnAction = v.findViewById(R.id.btnProvideGuidance);
            }
        }
    }
}
