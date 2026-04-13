package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.fit_lifegym.adapters.WorkoutHistoryAdapter;
import com.example.fit_lifegym.models.WorkoutSession;
import com.example.fit_lifegym.utils.FirebaseHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class WorkoutHistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CalendarView calendarView;
    private RecyclerView rvHistory;
    private TextView tvNoData;
    private WorkoutHistoryAdapter adapter;
    
    private SessionManager sessionManager;
    private DatabaseReference workoutsRef;
    private List<WorkoutSession> workoutList;
    private List<EventDay> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        sessionManager = new SessionManager(this);
        workoutsRef = FirebaseHelper.getWorkoutsRef(sessionManager.getUserId());
        workoutList = new ArrayList<>();
        events = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        loadWorkoutHistory();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        calendarView = findViewById(R.id.calendarView);
        rvHistory = findViewById(R.id.rvHistory);
        tvNoData = findViewById(R.id.tvNoData);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new WorkoutHistoryAdapter(this, workoutList, session -> {
            Intent intent = new Intent(this, WorkoutDetailsActivity.class);
            intent.putExtra("workoutId", session.getId());
            startActivity(intent);
        });
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);
    }

    private void loadWorkoutHistory() {
        workoutsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                workoutList.clear();
                events.clear();
                
                for (DataSnapshot data : snapshot.getChildren()) {
                    WorkoutSession session = data.getValue(WorkoutSession.class);
                    if (session != null) {
                        workoutList.add(session);
                        
                        // Add calendar event for each workout
                        if (session.getStartTime() != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(session.getStartTime());
                            events.add(new EventDay(cal, android.R.drawable.presence_online));
                        }
                    }
                }
                
                Collections.sort(workoutList, (w1, w2) -> w2.getStartTime().compareTo(w1.getStartTime()));
                adapter.notifyDataSetChanged();
                calendarView.setEvents(events);
                
                if (tvNoData != null) {
                    tvNoData.setVisibility(workoutList.isEmpty() ? View.VISIBLE : View.GONE);
                }
                rvHistory.setVisibility(workoutList.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
