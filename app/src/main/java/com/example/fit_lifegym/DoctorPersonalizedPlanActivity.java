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

import com.example.fit_lifegym.adapters.PhotoAdapter;
import com.example.fit_lifegym.models.DoctorPersonalizedMealPlan;
import com.example.fit_lifegym.models.FitnessGoalSubmission;
import com.example.fit_lifegym.utils.FirebaseHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DoctorPersonalizedPlanActivity extends AppCompatActivity {

    private RecyclerView rvSubmissions;
    private TextView tvEmpty;
    private DatabaseReference mDatabase;
    private SessionManager sessionManager;
    private List<FitnessGoalSubmission> submissionsList;
    private SubmissionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_personalized_plan);

        setupToolbar();
        rvSubmissions = findViewById(R.id.rvSubmissions);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvSubmissions.setLayoutManager(new LinearLayoutManager(this));

        sessionManager = new SessionManager(this);
        mDatabase = FirebaseHelper.getDbRef();
        submissionsList = new ArrayList<>();
        adapter = new SubmissionsAdapter(submissionsList);
        rvSubmissions.setAdapter(adapter);

        loadSubmissions();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void loadSubmissions() {
        mDatabase.child("personalized_plans").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                submissionsList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    try {
                        FitnessGoalSubmission submission = postSnapshot.getValue(FitnessGoalSubmission.class);
                        if (submission != null) {
                            submissionsList.add(submission);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("FirebaseError", "Data mismatch at " + postSnapshot.getKey(), e);
                    }
                }
                if (submissionsList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvSubmissions.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvSubmissions.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DoctorPersonalizedPlanActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMealPlanDialog(FitnessGoalSubmission submission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_doctor_meal_plan, null);
        builder.setView(dialogView);

        TextInputEditText etBreakfast = dialogView.findViewById(R.id.etBreakfast);
        TextInputEditText etLunch = dialogView.findViewById(R.id.etLunch);
        TextInputEditText etDinner = dialogView.findViewById(R.id.etDinner);
        TextInputEditText etSnacks = dialogView.findViewById(R.id.etSnacks);
        TextInputEditText etNotes = dialogView.findViewById(R.id.etNotes);

        builder.setTitle("Meal Plan for " + submission.getMemberName())
                .setPositiveButton("Submit", (dialog, which) -> {
                    String breakfast = etBreakfast.getText().toString().trim();
                    String lunch = etLunch.getText().toString().trim();
                    String dinner = etDinner.getText().toString().trim();
                    String snacks = etSnacks.getText().toString().trim();
                    String notes = etNotes.getText().toString().trim();

                    if (TextUtils.isEmpty(breakfast) || TextUtils.isEmpty(lunch) || TextUtils.isEmpty(dinner)) {
                        Toast.makeText(this, "Please fill main meals", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    submitMealPlan(submission, breakfast, lunch, dinner, snacks, notes);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitMealPlan(FitnessGoalSubmission submission, String breakfast, String lunch, String dinner, String snacks, String notes) {
        String memberId = submission.getUserId();
        DoctorPersonalizedMealPlan plan = new DoctorPersonalizedMealPlan(
                sessionManager.getUserId(),
                sessionManager.getName(),
                memberId,
                breakfast,
                lunch,
                dinner,
                snacks,
                notes
        );

        mDatabase.child("doctor_personalized_meal_plans").child(memberId).setValue(plan)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Meal Plan sent!", Toast.LENGTH_SHORT).show();
                    mDatabase.child("personalized_plans").child(memberId).child("doctorStatus").setValue("COMPLETED");
                    checkOverallStatus(memberId);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void checkOverallStatus(String memberId) {
        mDatabase.child("personalized_plans").child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    FitnessGoalSubmission submission = snapshot.getValue(FitnessGoalSubmission.class);
                    if (submission != null && "COMPLETED".equals(submission.getDoctorStatus()) && "COMPLETED".equals(submission.getTrainerStatus())) {
                        mDatabase.child("personalized_plans").child(memberId).child("status").setValue("COMPLETED");
                    }
                } catch (Exception e) {
                    android.util.Log.e("FirebaseError", "Failed to parse submission in checkOverallStatus", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private class SubmissionsAdapter extends RecyclerView.Adapter<SubmissionsAdapter.ViewHolder> {
        private List<FitnessGoalSubmission> list;

        SubmissionsAdapter(List<FitnessGoalSubmission> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal_submission, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FitnessGoalSubmission submission = list.get(position);
            holder.tvMemberName.setText(submission.getMemberName());
            holder.tvGoal.setText("Goal: " + submission.getSelectedGoal());
            holder.tvNotes.setText("Notes: " + submission.getNotes());
            holder.tvStatus.setText("Status: " + submission.getDoctorStatus());

            if (submission.getPhotoUrls() != null && !submission.getPhotoUrls().isEmpty()) {
                holder.tvPhotosLabel.setVisibility(View.VISIBLE);
                holder.rvPhotos.setVisibility(View.VISIBLE);
                holder.rvPhotos.setAdapter(new PhotoAdapter(submission.getPhotoUrls()));
            } else {
                holder.tvPhotosLabel.setVisibility(View.GONE);
                holder.rvPhotos.setVisibility(View.GONE);
            }

            holder.btnProvideGuidance.setText("Provide Meal Plan");
            holder.btnProvideGuidance.setOnClickListener(v -> showMealPlanDialog(submission));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMemberName, tvGoal, tvNotes, tvStatus, tvPhotosLabel;
            RecyclerView rvPhotos;
            MaterialButton btnProvideGuidance;

            ViewHolder(View itemView) {
                super(itemView);
                tvMemberName = itemView.findViewById(R.id.tvMemberName);
                tvGoal = itemView.findViewById(R.id.tvGoal);
                tvNotes = itemView.findViewById(R.id.tvNotes);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvPhotosLabel = itemView.findViewById(R.id.tvPhotosLabel);
                rvPhotos = itemView.findViewById(R.id.rvPhotos);
                btnProvideGuidance = itemView.findViewById(R.id.btnProvideGuidance);
            }
        }
    }
}
