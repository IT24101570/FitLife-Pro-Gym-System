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
import com.example.fit_lifegym.models.FitnessGoalSubmission;
import com.example.fit_lifegym.models.TrainerGuidance;
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

public class TrainerGuidanceActivity extends AppCompatActivity {

    private RecyclerView rvSubmissions;
    private TextView tvEmpty;
    private DatabaseReference mDatabase;
    private SessionManager sessionManager;
    private List<FitnessGoalSubmission> submissionsList;
    private SubmissionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_guidance);

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
                Toast.makeText(TrainerGuidanceActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGuidanceDialog(FitnessGoalSubmission submission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_trainer_guidance, null);
        builder.setView(dialogView);

        TextInputEditText etWeeklyTarget = dialogView.findViewById(R.id.etWeeklyTarget);
        TextInputEditText etVideoLinks = dialogView.findViewById(R.id.etVideoLinks);
        TextInputEditText etAdvice = dialogView.findViewById(R.id.etAdvice);

        builder.setTitle("Guidance for " + submission.getMemberName())
                .setPositiveButton("Submit", (dialog, which) -> {
                    String weeklyTarget = etWeeklyTarget.getText().toString().trim();
                    String advice = etAdvice.getText().toString().trim();
                    String videos = etVideoLinks.getText().toString().trim();

                    if (TextUtils.isEmpty(weeklyTarget) || TextUtils.isEmpty(advice)) {
                        Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    submitGuidance(submission, weeklyTarget, advice, videos);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitGuidance(FitnessGoalSubmission submission, String target, String advice, String videos) {
        String memberId = submission.getUserId();
        TrainerGuidance guidance = new TrainerGuidance(
                sessionManager.getUserId(),
                sessionManager.getName(),
                memberId,
                target,
                advice,
                videos
        );

        mDatabase.child("trainer_guidance").child(memberId).setValue(guidance)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Guidance sent!", Toast.LENGTH_SHORT).show();
                    mDatabase.child("personalized_plans").child(memberId).child("trainerStatus").setValue("COMPLETED");
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
            holder.tvStatus.setText("Status: " + submission.getTrainerStatus());

            if (submission.getPhotoUrls() != null && !submission.getPhotoUrls().isEmpty()) {
                holder.tvPhotosLabel.setVisibility(View.VISIBLE);
                holder.rvPhotos.setVisibility(View.VISIBLE);
                holder.rvPhotos.setAdapter(new PhotoAdapter(submission.getPhotoUrls()));
            } else {
                holder.tvPhotosLabel.setVisibility(View.GONE);
                holder.rvPhotos.setVisibility(View.GONE);
            }

            holder.btnProvideGuidance.setOnClickListener(v -> showGuidanceDialog(submission));
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
