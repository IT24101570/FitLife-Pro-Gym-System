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

import com.example.fit_lifegym.models.FitnessGoalSubmission;
import com.example.fit_lifegym.models.TrainerGuidance;
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

public class TrainerGuidanceActivity extends AppCompatActivity {

    private RecyclerView rvSubmissions;
    private TextView tvEmpty;
    private SubmissionAdapter adapter;
    private List<FitnessGoalSubmission> submissionList;
    private DatabaseReference submissionsRef, guidanceRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_guidance);

        sessionManager = new SessionManager(this);
        submissionsRef = FirebaseDatabase.getInstance().getReference("fitness_submissions");
        guidanceRef = FirebaseDatabase.getInstance().getReference("trainer_guidance");
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

    private void showGuidanceDialog(FitnessGoalSubmission submission) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_trainer_guidance, null);
        TextInputEditText etWeeklyTarget = view.findViewById(R.id.etWeeklyTarget);
        TextInputEditText etVideoLinks = view.findViewById(R.id.etVideoLinks);
        TextInputEditText etAdvice = view.findViewById(R.id.etAdvice);

        new AlertDialog.Builder(this)
                .setTitle("Guidance for " + submission.getMemberName())
                .setView(view)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String target = etWeeklyTarget.getText().toString().trim();
                    String links = etVideoLinks.getText().toString().trim();
                    String advice = etAdvice.getText().toString().trim();

                    if (TextUtils.isEmpty(target) || TextUtils.isEmpty(advice)) {
                        Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    TrainerGuidance guidance = new TrainerGuidance();
                    guidance.setId(UUID.randomUUID().toString());
                    guidance.setSubmissionId(submission.getMemberId());
                    guidance.setTrainerId(sessionManager.getUserId());
                    guidance.setTrainerName(sessionManager.getName());
                    guidance.setWeeklyTarget(target);
                    guidance.setVideoLinks(links);
                    guidance.setAdvice(advice);
                    guidance.setUpdatedDate(System.currentTimeMillis());

                    guidanceRef.child(submission.getMemberId()).setValue(guidance)
                            .addOnSuccessListener(aVoid -> {
                                submissionsRef.child(submission.getMemberId()).child("status").setValue("TRAINER_UPDATED");
                                Toast.makeText(this, "Guidance submitted!", Toast.LENGTH_SHORT).show();
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
            holder.btnAction.setOnClickListener(v -> showGuidanceDialog(sub));
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
