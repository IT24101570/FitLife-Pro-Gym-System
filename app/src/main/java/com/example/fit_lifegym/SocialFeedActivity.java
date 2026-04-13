package com.example.fit_lifegym;

import android.os.Bundle;
import android.text.format.DateUtils;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.fit_lifegym.models.Post;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SocialFeedActivity extends AppCompatActivity {

    private RecyclerView rvPosts;
    private PostAdapter adapter;
    private List<Post> postList;
    private DatabaseReference postsRef;
    private SessionManager sessionManager;
    private SwipeRefreshLayout swipeRefresh;
    private ExtendedFloatingActionButton fabPost;
    private ImageView btnBack, btnLeaderboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_feed);

        sessionManager = new SessionManager(this);
        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        initViews();
        setupRecyclerView();
        setupListeners();
        loadPosts();
    }

    private void initViews() {
        rvPosts = findViewById(R.id.rvPosts);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        fabPost = findViewById(R.id.fabPost);
        btnBack = findViewById(R.id.btnBack);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
    }

    private void setupRecyclerView() {
        postList = new ArrayList<>();
        adapter = new PostAdapter(postList);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        if (btnLeaderboard != null) {
            btnLeaderboard.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Hall of Fame...", Toast.LENGTH_SHORT).show();
            });
        }

        if (swipeRefresh != null) swipeRefresh.setOnRefreshListener(this::loadPosts);
        
        if (fabPost != null) fabPost.setOnClickListener(v -> showCreatePostDialog());
    }

    private void loadPosts() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(true);
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    if (post != null) {
                        post.setId(ds.getKey());
                        postList.add(post);
                    }
                }
                
                if (postList.isEmpty()) addSamplePosts();
                
                Collections.sort(postList, (p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                adapter.notifyDataSetChanged();
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void addSamplePosts() {
        postList.add(new Post("system", "Coach John", "New workout programs are live! Check out the Strength section.", "GENERAL"));
        postList.add(new Post("user1", "Alex Fit", "Just hit my personal best on Deadlift! 140kg! 💪", "ACHIEVEMENT"));
        postList.add(new Post("user2", "Sarah Miles", "Early morning yoga session felt amazing. Namaste.", "WORKOUT"));
    }

    private void showCreatePostDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_post, null);
        TextInputEditText etContent = dialogView.findViewById(R.id.etContent);

        new AlertDialog.Builder(this, R.style.Theme_Fit_lifeGym)
            .setTitle("Share with Community")
            .setView(dialogView)
            .setPositiveButton("Post", (dialog, which) -> {
                String content = etContent.getText().toString().trim();
                if (!content.isEmpty()) {
                    createNewPost(content);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void createNewPost(String content) {
        String postId = postsRef.push().getKey();
        Post post = new Post(sessionManager.getUserId(), sessionManager.getName(), content, "GENERAL");
        
        if (postId != null) {
            postsRef.child(postId).setValue(post)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Moment shared!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to share", Toast.LENGTH_SHORT).show());
        }
    }

    private class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {
        private List<Post> posts;
        PostAdapter(List<Post> posts) { this.posts = posts; }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
            return new PostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
            Post post = posts.get(position);
            holder.tvUserName.setText(post.getUserName());
            holder.tvContent.setText(post.getContent());
            holder.tvPostType.setText(post.getType().toUpperCase());
            holder.tvLikeCount.setText(String.valueOf(post.getLikeCount()));
            holder.tvTime.setText(DateUtils.getRelativeTimeSpanString(post.getTimestamp()));
            
            if (post.getUserProfileImage() != null && !post.getUserProfileImage().isEmpty()) {
                Glide.with(SocialFeedActivity.this).load(post.getUserProfileImage()).into(holder.ivUserProfile);
            }

            holder.btnLike.setOnClickListener(v -> toggleLike(post));
        }

        @Override
        public int getItemCount() { return posts.size(); }

        private void toggleLike(Post post) {
            String myId = sessionManager.getUserId();
            DatabaseReference likeRef = postsRef.child(post.getId()).child("likes").child(myId);
            if (post.getLikes() != null && post.getLikes().containsKey(myId)) {
                likeRef.removeValue();
            } else {
                likeRef.setValue(true);
            }
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvContent, tvPostType, tvLikeCount, tvTime;
        ImageView ivUserProfile;
        View btnLike;
        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvPostType = itemView.findViewById(R.id.tvPostType);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }
}
