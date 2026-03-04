package com.example.fit_lifegym;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.models.User;
import com.example.fit_lifegym.models.WorkoutSession;
import com.example.fit_lifegym.utils.FirebaseHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private ImageView btnBack;
    private LeaderboardAdapter adapter;
    private List<UserScore> userList;
    private DatabaseReference usersRef, workoutsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        usersRef = FirebaseHelper.getUsersRef();
        workoutsRef = FirebaseDatabase.getInstance().getReference("workouts");
        userList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupListeners();
        loadLeaderboard("Workouts");
    }

    private void initViews() {
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        tabLayout = findViewById(R.id.tabLayout);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter(userList);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadLeaderboard(tab.getText().toString());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadLeaderboard(String category) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        workoutsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot workoutSnapshot) {
                Map<String, Integer> userScores = new HashMap<>();
                
                // Aggregate data based on category
                for (DataSnapshot userWorkouts : workoutSnapshot.getChildren()) {
                    String userId = userWorkouts.getKey();
                    int score = 0;
                    
                    for (DataSnapshot sessionDs : userWorkouts.getChildren()) {
                        WorkoutSession session = sessionDs.getValue(WorkoutSession.class);
                        if (session != null) {
                            if (category.equals("Workouts")) score++;
                            else if (category.equals("Calories")) score += session.getTotalCaloriesBurned();
                            else if (category.equals("Streak")) score += 1; // Simplified streak logic
                        }
                    }
                    userScores.put(userId, score);
                }

                fetchUserNames(userScores, category);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchUserNames(Map<String, Integer> scores, String category) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                userList.clear();
                for (DataSnapshot ds : userSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (user != null && scores.containsKey(user.getId())) {
                        userList.add(new UserScore(user.getName(), scores.get(user.getId()), getUnit(category)));
                    }
                }
                
                Collections.sort(userList, (u1, u2) -> Integer.compare(u2.score, u1.score));
                adapter.notifyDataSetChanged();
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private String getUnit(String category) {
        switch (category) {
            case "Workouts": return "sessions";
            case "Calories": return "kcal";
            case "Streak": return "days";
            default: return "pts";
        }
    }

    private static class UserScore {
        String name;
        int score;
        String unit;

        UserScore(String name, int score, String unit) {
            this.name = name;
            this.score = score;
            this.unit = unit;
        }
    }

    private class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
        private List<UserScore> list;
        LeaderboardAdapter(List<UserScore> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserScore us = list.get(position);
            holder.tvRank.setText(String.valueOf(position + 1));
            holder.tvUserName.setText(us.name);
            holder.tvStatValue.setText(String.valueOf(us.score));
            holder.tvStatUnit.setText(us.unit);
            
            // Premium highlight for rank 1
            if (position == 0) {
                holder.tvRank.setTextColor(getResources().getColor(R.color.accent));
                holder.tvStatValue.setTextColor(getResources().getColor(R.color.accent));
            } else {
                holder.tvRank.setTextColor(getResources().getColor(R.color.text_secondary));
                holder.tvStatValue.setTextColor(getResources().getColor(R.color.text_primary));
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRank, tvUserName, tvStatValue, tvStatUnit;
            ViewHolder(View v) {
                super(v);
                tvRank = v.findViewById(R.id.tvRank);
                tvUserName = v.findViewById(R.id.tvUserName);
                tvStatValue = v.findViewById(R.id.tvStatValue);
                tvStatUnit = v.findViewById(R.id.tvStatUnit);
            }
        }
    }
}
