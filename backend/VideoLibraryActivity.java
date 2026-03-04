package com.example.fit_lifegym;

import android.os.Bundle;
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
    private List<VideoClass> videoList;
    private List<VideoClass> filteredList;
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
        videoList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new VideoAdapter(filteredList);
        rvVideos.setLayoutManager(new LinearLayoutManager(this));
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
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private List<VideoClass> getSampleVideos() {
        List<VideoClass> samples = new ArrayList<>();
        samples.add(new VideoClass("Full Body HIIT Blast", "HIIT", "Coach Sarah", 20, "Intermediate", false));
        samples.add(new VideoClass("Morning Yoga Flow", "Yoga", "Emma Zen", 15, "Beginner", true));
        samples.add(new VideoClass("Heavy Lifting Basics", "Strength", "John Iron", 30, "Advanced", true));
        samples.add(new VideoClass("Core Power Session", "Strength", "Mike Ross", 25, "Intermediate", false));
        samples.add(new VideoClass("Sun Salutation", "Yoga", "Emma Zen", 10, "Beginner", false));
        return samples;
    }

    private void filterVideos(String category) {
        filteredList.clear();
        if (category.equals("All")) {
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
                holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_slideshow);
            }

            holder.itemView.setOnClickListener(v -> {
                if (vc.isPremium() && !sessionManager.isPremiumUser()) {
                    Toast.makeText(VideoLibraryActivity.this, "Premium Membership required!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VideoLibraryActivity.this, "Buffering: " + vc.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() { return videos.size(); }
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
