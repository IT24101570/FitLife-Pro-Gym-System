package com.example.fit_lifegym;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.models.WorkoutHistory;
import com.example.fit_lifegym.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressChartsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LineChart caloriesChart, durationChart;
    private BarChart workoutsPerWeekChart;
    
    private SessionManager sessionManager;
    private DatabaseReference workoutsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_charts);

        sessionManager = new SessionManager(this);
        workoutsRef = FirebaseDatabase.getInstance().getReference("workoutHistory");

        initializeViews();
        loadChartData();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        caloriesChart = findViewById(R.id.caloriesChart);
        durationChart = findViewById(R.id.durationChart);
        workoutsPerWeekChart = findViewById(R.id.workoutsPerWeekChart);

        btnBack.setOnClickListener(v -> finish());

        setupCharts();
    }

    private void setupCharts() {
        // Setup Calories Chart
        caloriesChart.getDescription().setEnabled(false);
        caloriesChart.setTouchEnabled(true);
        caloriesChart.setDragEnabled(true);
        caloriesChart.setScaleEnabled(true);
        caloriesChart.setPinchZoom(true);
        caloriesChart.setDrawGridBackground(false);

        // Setup Duration Chart
        durationChart.getDescription().setEnabled(false);
        durationChart.setTouchEnabled(true);
        durationChart.setDragEnabled(true);
        durationChart.setScaleEnabled(true);
        durationChart.setPinchZoom(true);
        durationChart.setDrawGridBackground(false);

        // Setup Workouts Per Week Chart
        workoutsPerWeekChart.getDescription().setEnabled(false);
        workoutsPerWeekChart.setTouchEnabled(true);
        workoutsPerWeekChart.setDragEnabled(true);
        workoutsPerWeekChart.setScaleEnabled(true);
        workoutsPerWeekChart.setPinchZoom(true);
        workoutsPerWeekChart.setDrawGridBackground(false);
        
        XAxis xAxis = workoutsPerWeekChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
    }

    private void loadChartData() {
        String userId = sessionManager.getUserId();
        
        workoutsRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<WorkoutHistory> workouts = new ArrayList<>();
                    
                    for (DataSnapshot data : snapshot.getChildren()) {
                        WorkoutHistory workout = data.getValue(WorkoutHistory.class);
                        if (workout != null) {
                            workouts.add(workout);
                        }
                    }
                    
                    updateCaloriesChart(workouts);
                    updateDurationChart(workouts);
                    updateWorkoutsPerWeekChart(workouts);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle error
                }
            });
    }

    private void updateCaloriesChart(List<WorkoutHistory> workouts) {
        List<Entry> entries = new ArrayList<>();
        
        for (int i = 0; i < workouts.size(); i++) {
            entries.add(new Entry(i, workouts.get(i).getCaloriesBurned()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Calories Burned");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#4CAF50"));
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        caloriesChart.setData(lineData);
        caloriesChart.invalidate();
    }

    private void updateDurationChart(List<WorkoutHistory> workouts) {
        List<Entry> entries = new ArrayList<>();
        
        for (int i = 0; i < workouts.size(); i++) {
            entries.add(new Entry(i, workouts.get(i).getDuration()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Duration (minutes)");
        dataSet.setColor(Color.parseColor("#FF9800"));
        dataSet.setCircleColor(Color.parseColor("#FF9800"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#FF9800"));
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        durationChart.setData(lineData);
        durationChart.invalidate();
    }

    private void updateWorkoutsPerWeekChart(List<WorkoutHistory> workouts) {
        Map<Integer, Integer> weeklyWorkouts = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        
        for (WorkoutHistory workout : workouts) {
            calendar.setTime(workout.getDate());
            int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
            weeklyWorkouts.put(weekOfYear, weeklyWorkouts.getOrDefault(weekOfYear, 0) + 1);
        }

        List<BarEntry> entries = new ArrayList<>();
        int index = 0;
        for (Map.Entry<Integer, Integer> entry : weeklyWorkouts.entrySet()) {
            entries.add(new BarEntry(index++, entry.getValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Workouts Per Week");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);
        
        workoutsPerWeekChart.setData(barData);
        workoutsPerWeekChart.invalidate();
    }
}
