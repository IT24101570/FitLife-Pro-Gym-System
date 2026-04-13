package com.example.fit_lifegym;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.models.Professional;
import com.example.fit_lifegym.models.VideoClass;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageVideosActivity extends AppCompatActivity {

    private RecyclerView rvMyVideos;
    private FloatingActionButton fabAddVideo;
    private DatabaseReference videosRef;
    private SessionManager sessionManager;
    private List<VideoClass> myVideos = new ArrayList<>();
    private MyVideoAdapter adapter;
    private String trainerName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_videos);

        sessionManager = new SessionManager(this);
        videosRef = FirebaseDatabase.getInstance().getReference("videoClasses");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        rvMyVideos = findViewById(R.id.rvMyVideos);
        fabAddVideo = findViewById(R.id.fabAddVideo);

        setupRecyclerView();
        fetchTrainerInfo();
        loadMyVideos();

        fabAddVideo.setOnClickListener(v -> showAddVideoDialog());
    }

    private void setupRecyclerView() {
        rvMyVideos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyVideoAdapter(myVideos);
        rvMyVideos.setAdapter(adapter);
    }

    private void fetchTrainerInfo() {
        String uid = sessionManager.getUserId();
        FirebaseDatabase.getInstance().getReference("professionals").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Professional p = snapshot.getValue(Professional.class);
                        if (p != null) {
                            trainerName = p.getName();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadMyVideos() {
        String uid = sessionManager.getUserId();
        videosRef.orderByChild("instructorProfileId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myVideos.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            VideoClass vc = ds.getValue(VideoClass.class);
                            if (vc != null) {
                                vc.setId(ds.getKey());
                                myVideos.add(vc);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void showAddVideoDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_video, null);
        EditText etTitle = view.findViewById(R.id.etVideoTitle);
        EditText etLink = view.findViewById(R.id.etVideoLink);
        EditText etOwner = view.findViewById(R.id.etOwnerName);
        Spinner spPlan = view.findViewById(R.id.spMembershipPlan);
        Spinner spCategory = view.findViewById(R.id.spCategory);

        etOwner.setText(trainerName);
        etOwner.setEnabled(false);

        String[] plans = {"Free", "Premium (Elite)"};
        spPlan.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, plans));

        String[] categories = {"Yoga", "HIIT", "Strength", "Cardio"};
        spCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        new AlertDialog.Builder(this)
                .setTitle("Add New Video")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String link = etLink.getText().toString().trim();
                    boolean isPremium = spPlan.getSelectedItemPosition() == 1;
                    String category = spCategory.getSelectedItem().toString();

                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(link)) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    saveVideo(title, link, category, isPremium);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveVideo(String title, String link, String category, boolean isPremium) {
        String id = videosRef.push().getKey();
        VideoClass vc = new VideoClass();
        vc.setId(id);
        vc.setTitle(title);
        vc.setVideoUrl(link);
        vc.setCategory(category);
        vc.setInstructorName(trainerName);
        vc.setInstructorProfileId(sessionManager.getUserId());
        vc.setPremium(isPremium);
        vc.setDurationMinutes(15); // Default duration
        vc.setDifficulty("Intermediate"); // Default difficulty

        if (id != null) {
            videosRef.child(id).setValue(vc).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Video added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add video", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private class MyVideoAdapter extends RecyclerView.Adapter<MyVideoViewHolder> {
        private List<VideoClass> list;
        MyVideoAdapter(List<VideoClass> list) { this.list = list; }

        @NonNull
        @Override
        public MyVideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new MyVideoViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyVideoViewHolder holder, int position) {
            VideoClass vc = list.get(position);
            holder.text1.setText(vc.getTitle());
            holder.text2.setText(vc.getCategory() + " | " + (vc.isPremium() ? "Premium" : "Free"));
            
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(ManageVideosActivity.this)
                        .setTitle("Delete Video")
                        .setMessage("Are you sure you want to delete this video?")
                        .setPositiveButton("Delete", (d, w) -> {
                            videosRef.child(vc.getId()).removeValue();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() { return list.size(); }
    }

    private static class MyVideoViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        MyVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
