package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fit_lifegym.models.VideoClass;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VideoLibraryActivity extends AppCompatActivity {

    private RecyclerView rvVideos;
    private VideoAdapter adapter;
    private List<VideoClass> videoList = new ArrayList<>();
    private List<VideoClass> filteredList = new ArrayList<>();
    private DatabaseReference videosRef;
    private TabLayout tabLayout;
    private ImageView btnBack;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_library);

        sessionManager = new SessionManager(this);
        videosRef = FirebaseDatabase.getInstance().getReference("videoClasses");

        initViews();
        setupRecyclerView();
        setupListeners();
        loadVideos();
    }

    private void initViews() {
        rvVideos = findViewById(R.id.rvVideos);
        tabLayout = findViewById(R.id.tabLayout);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        rvVideos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter(filteredList);
        rvVideos.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterVideos(tab.getText().toString());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadVideos() {
        videosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    VideoClass vc = ds.getValue(VideoClass.class);
                    if (vc != null) {
                        vc.setId(ds.getKey());
                        videoList.add(vc);
                    }
                }
                
                if (videoList.isEmpty()) {
                    videoList.addAll(getSampleVideos());
                }
                
                filterVideos(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("VideoLibrary", "Database Error: " + error.getMessage());
                if (videoList.isEmpty()) {
                    videoList.addAll(getSampleVideos());
                    filterVideos(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString());
                }
            }
        });
    }

    private List<VideoClass> getSampleVideos() {
        List<VideoClass> samples = new ArrayList<>();
        
        VideoClass v1 = new VideoClass("Morning Yoga Flow", "Yoga", "Emma Zen", 15, "Beginner", true);
        v1.setVideoUrl("https://youtu.be/Vr3h5X9kmUo?si=MkcsK62RX6pCU0Lp");
        samples.add(v1);

        VideoClass v2 = new VideoClass("Full Body HIIT Blast", "HIIT", "Coach Sarah", 20, "Intermediate", false);
        v2.setVideoUrl("https://youtu.be/hLVh5IBsCxk?si=lh7EToqbKK4PBknM");
        samples.add(v2);

        VideoClass v3 = new VideoClass("Heavy Lifting Basics", "Strength", "John Iron", 30, "Advanced", true);
        v3.setVideoUrl("https://youtu.be/GY1JhB9BEkk?si=nvjspt_1SIZTKXB2");
        samples.add(v3);

        VideoClass v4 = new VideoClass("Core Power Session", "Strength", "Mike Ross", 25, "Intermediate", false);
        v4.setVideoUrl("https://youtu.be/k2Aw9vZm_0E?si=aJTfT_4H-jdV0eg3");
        samples.add(v4);

        VideoClass v5 = new VideoClass("Sun Salutation", "Yoga", "Emma Zen", 10, "Beginner", false);
        v5.setVideoUrl("https://youtu.be/I9_wJmIAckA?si=NIkQod1F0BgajwlS");
        samples.add(v5);

        return samples;
    }

    private void filterVideos(String category) {
        filteredList.clear();
        if (category == null || category.equalsIgnoreCase("All")) {
            filteredList.addAll(videoList);
        } else {
            for (VideoClass vc : videoList) {
                if (vc.getCategory() != null && vc.getCategory().equalsIgnoreCase(category)) {
                    filteredList.add(vc);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private class VideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {
        private List<VideoClass> videos;
        VideoAdapter(List<VideoClass> videos) { this.videos = videos; }

        @NonNull
        @Override
        public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_class, parent, false);
            return new VideoViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
            VideoClass vc = videos.get(position);
            holder.tvTitle.setText(vc.getTitle());
            holder.tvInstructor.setText("by " + vc.getInstructorName());
            holder.tvDuration.setText(vc.getDurationMinutes() + " min");
            holder.tvDifficulty.setText(vc.getDifficulty().toUpperCase());
            holder.tvPremiumTag.setVisibility(vc.isPremium() ? View.VISIBLE : View.GONE);

            if (vc.getThumbnailUrl() != null && !vc.getThumbnailUrl().isEmpty()) {
                Glide.with(VideoLibraryActivity.this).load(vc.getThumbnailUrl()).into(holder.ivThumbnail);
            } else {
                String videoId = extractVideoId(vc.getVideoUrl());
                if (videoId != null) {
                    String thumbUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
                    Glide.with(VideoLibraryActivity.this)
                         .load(thumbUrl)
                         .placeholder(R.drawable.logo_background)
                         .into(holder.ivThumbnail);
                } else {
                    holder.ivThumbnail.setImageResource(R.drawable.logo_background);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                if (vc.isPremium() && !sessionManager.isPremiumUser()) {
                    showPremiumDialog();
                } else {
                    if (vc.getVideoUrl() != null && !vc.getVideoUrl().isEmpty()) {
                        Intent intent = new Intent(VideoLibraryActivity.this, VideoPlayerActivity.class);
                        intent.putExtra("VIDEO_URL", vc.getVideoUrl());
                        intent.putExtra("VIDEO_TITLE", vc.getTitle());
                        startActivity(intent);
                    } else {
                        Toast.makeText(VideoLibraryActivity.this, "Video link not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() { return videos.size(); }

        private String extractVideoId(String url) {
            if (url == null || url.trim().isEmpty()) return null;
            try {
                if (url.contains("v=")) {
                    String[] parts = url.split("v=");
                    String id = parts[1];
                    int ampersandIndex = id.indexOf("&");
                    if (ampersandIndex != -1) id = id.substring(0, ampersandIndex);
                    return id;
                } else if (url.contains("youtu.be/")) {
                    String id = url.substring(url.lastIndexOf("/") + 1);
                    int questionMarkIndex = id.indexOf("?");
                    if (questionMarkIndex != -1) id = id.substring(0, questionMarkIndex);
                    return id;
                }
            } catch (Exception e) {
                return null;
            }
            return null;
        }

        private void showPremiumDialog() {
            new androidx.appcompat.app.AlertDialog.Builder(VideoLibraryActivity.this)
                .setTitle("Elite Access Required")
                .setMessage("This video is part of our Elite library. Upgrade your plan to unlock all professional training videos.")
                .setPositiveButton("View Plans", (dialog, which) -> {
                    try {
                        startActivity(new Intent(VideoLibraryActivity.this, Class.forName("com.example.fit_lifegym.SubscriptionActivity")));
                    } catch (ClassNotFoundException e) {
                        Toast.makeText(VideoLibraryActivity.this, "Subscription plans coming soon!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Maybe Later", null)
                .show();
        }
    }

    private static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInstructor, tvDuration, tvDifficulty, tvPremiumTag;
        ImageView ivThumbnail;
        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvInstructor = itemView.findViewById(R.id.tvInstructor);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvPremiumTag = itemView.findViewById(R.id.tvPremiumTag);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
        }
    }
}
