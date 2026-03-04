package com.example.fit_lifegym;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.adapters.MealPlanAdapter;
import com.example.fit_lifegym.models.MealPlan;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MealPlansActivity extends AppCompatActivity implements MealPlanAdapter.OnMealPlanClickListener {

    private RecyclerView rvMealPlans;
    private View tvEmpty;
    private ImageView btnBack;
    private MealPlanAdapter adapter;
    private List<MealPlan> mealPlanList;
    private DatabaseReference mealPlansRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plans);

        sessionManager = new SessionManager(this);
        mealPlansRef = FirebaseDatabase.getInstance().getReference("mealPlans");
        mealPlanList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadMealPlans();
    }

    private void initViews() {
        rvMealPlans = findViewById(R.id.rvMealPlans);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new MealPlanAdapter(mealPlanList, this);
        rvMealPlans.setLayoutManager(new LinearLayoutManager(this));
        rvMealPlans.setAdapter(adapter);
    }

    private void loadMealPlans() {
        mealPlansRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mealPlanList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        MealPlan plan = ds.getValue(MealPlan.class);
                        if (plan != null) {
                            plan.setId(ds.getKey());
                            mealPlanList.add(plan);
                        }
                    }
                } else {
                    addSampleMealPlans();
                }
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(mealPlanList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MealPlansActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSampleMealPlans() {
        MealPlan p1 = new MealPlan("1", "Weight Loss Starter", "Weight Loss", 1500);
        p1.setDescription("A balanced plan focused on calorie deficit while maintaining high protein intake.");
        p1.setProtein(120); p1.setCarbs(150); p1.setFats(50);
        mealPlanList.add(p1);

        MealPlan p2 = new MealPlan("2", "Muscle Building Elite", "Muscle Gain", 2800);
        p2.setDescription("High calorie and high protein plan designed for optimal muscle growth and recovery.");
        p2.setProtein(200); p2.setCarbs(350); p2.setFats(80);
        mealPlanList.add(p2);
    }

    @Override
    public void onActivateClick(MealPlan plan) {
        String userId = sessionManager.getUserId();
        FirebaseDatabase.getInstance().getReference("users").child(userId).child("activeMealPlanId").setValue(plan.getId())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, plan.getName() + " Activated!", Toast.LENGTH_SHORT).show());
    }
}
