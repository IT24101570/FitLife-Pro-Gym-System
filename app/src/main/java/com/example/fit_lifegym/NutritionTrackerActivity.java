package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.adapters.MealAdapter;
import com.example.fit_lifegym.models.FoodItem;
import com.example.fit_lifegym.models.Meal;
import com.example.fit_lifegym.models.MealPlan;
import com.example.fit_lifegym.models.NutritionLog;
import com.example.fit_lifegym.utils.SessionManager;
import com.example.fit_lifegym.utils.ValidationUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.UUID;

public class NutritionTrackerActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvRemainingCalories, tvEaten, tvBurned;
    private TextView tvProteinValue, tvCarbsValue, tvFatsValue;
    private CircularProgressIndicator calorieProgress;
    private LinearProgressIndicator proteinProgress, carbsProgress, fatsProgress;
    private RecyclerView rvMeals;
    private View btnAddMeal, btnMealPlans;
    
    // Active Plan UI
    private View cardActivePlan;
    private TextView tvActivePlanName, tvActivePlanGoal;
    private Button btnDeactivate;

    private MealAdapter mealAdapter;
    private SessionManager sessionManager;
    private NutritionLog nutritionLog;
    private DatabaseReference userRef, mealPlansRef, assignedRef;

    private final String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack", "Pre-Workout", "Post-Workout"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_tracker);

        sessionManager = new SessionManager(this);
        userRef = FirebaseDatabase.getInstance().getReference("users").child(sessionManager.getUserId());
        mealPlansRef = FirebaseDatabase.getInstance().getReference("mealPlans");
        assignedRef = FirebaseDatabase.getInstance().getReference("assigned_meal_plans").child(sessionManager.getUserId());
        
        initializeViews();
        setupNutritionLog();
        setupRecyclerView();
        setupListeners();
        loadActiveMealPlan();
        updateUI();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvRemainingCalories = findViewById(R.id.tvRemainingCalories);
        tvEaten = findViewById(R.id.tvEaten);
        tvBurned = findViewById(R.id.tvBurned);
        calorieProgress = findViewById(R.id.calorieProgress);
        
        View macroProtein = findViewById(R.id.macroProtein);
        tvProteinValue = macroProtein.findViewById(R.id.tvMacroValue);
        proteinProgress = macroProtein.findViewById(R.id.macroProgress);
        ((TextView)macroProtein.findViewById(R.id.tvMacroLabel)).setText("PROTEIN");

        View macroCarbs = findViewById(R.id.macroCarbs);
        tvCarbsValue = macroCarbs.findViewById(R.id.tvMacroValue);
        carbsProgress = macroCarbs.findViewById(R.id.macroProgress);
        ((TextView)macroCarbs.findViewById(R.id.tvMacroLabel)).setText("CARBS");

        View macroFats = findViewById(R.id.macroFats);
        tvFatsValue = macroFats.findViewById(R.id.tvMacroValue);
        fatsProgress = macroFats.findViewById(R.id.macroProgress);
        ((TextView)macroFats.findViewById(R.id.tvMacroLabel)).setText("FATS");

        rvMeals = findViewById(R.id.rvMeals);
        btnAddMeal = findViewById(R.id.btnAddMeal);
        btnMealPlans = findViewById(R.id.btnMealPlans);
        
        cardActivePlan = findViewById(R.id.cardActivePlan);
        tvActivePlanName = findViewById(R.id.tvActivePlanName);
        tvActivePlanGoal = findViewById(R.id.tvActivePlanGoal);
        btnDeactivate = findViewById(R.id.btnDeactivate);
    }

    private void setupNutritionLog() {
        nutritionLog = new NutritionLog(sessionManager.getUserId(), new Date());
        // Default values if no plan is active
        nutritionLog.setTargetCalories(2000);
        nutritionLog.setTargetProtein(150);
        nutritionLog.setTargetCarbs(200);
        nutritionLog.setTargetFats(60);
    }

    private void setupRecyclerView() {
        rvMeals.setLayoutManager(new LinearLayoutManager(this));
        mealAdapter = new MealAdapter(nutritionLog.getMeals(), null);
        rvMeals.setAdapter(mealAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddMeal.setOnClickListener(v -> showAddMealDialog());
        btnMealPlans.setOnClickListener(v -> startActivity(new Intent(this, MealPlansActivity.class)));
        btnDeactivate.setOnClickListener(v -> deactivatePlan());
    }

    private void loadActiveMealPlan() {
        userRef.child("activeMealPlanId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String planId = snapshot.getValue(String.class);
                if (planId != null) {
                    fetchPlanDetails(planId);
                } else {
                    cardActivePlan.setVisibility(View.GONE);
                    setupNutritionLog(); // Reset to defaults
                    updateUI();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchPlanDetails(String planId) {
        // Check assigned plans first
        assignedRef.child(planId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    applyMealPlan(snapshot.getValue(MealPlan.class));
                } else {
                    // Check system templates
                    mealPlansRef.child(planId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot systemSnap) {
                            if (systemSnap.exists()) {
                                applyMealPlan(systemSnap.getValue(MealPlan.class));
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void applyMealPlan(MealPlan plan) {
        if (plan == null) return;
        
        nutritionLog.setTargetCalories(plan.getTotalCalories());
        nutritionLog.setTargetProtein((int) plan.getProtein());
        nutritionLog.setTargetCarbs((int) plan.getCarbs());
        nutritionLog.setTargetFats((int) plan.getFats());

        cardActivePlan.setVisibility(View.VISIBLE);
        tvActivePlanName.setText(plan.getName());
        tvActivePlanGoal.setText("Goal: " + plan.getGoal());
        
        updateUI();
    }

    private void deactivatePlan() {
        userRef.child("activeMealPlanId").removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Meal plan deactivated", Toast.LENGTH_SHORT).show());
    }

    private void updateUI() {
        int consumed = nutritionLog.getTotalCalories();
        int target = nutritionLog.getTargetCalories();
        int remaining = Math.max(0, target - consumed);

        tvRemainingCalories.setText(String.valueOf(remaining));
        tvEaten.setText(String.valueOf(consumed));
        tvBurned.setText("0"); // Placeholder or fetch from activity tracker

        calorieProgress.setProgress(Math.min((consumed * 100) / target, 100), true);

        tvProteinValue.setText((int)nutritionLog.getTotalProtein() + " / " + (int)nutritionLog.getTargetProtein() + "g");
        proteinProgress.setProgress(Math.min((int)(nutritionLog.getTotalProtein() * 100 / nutritionLog.getTargetProtein()), 100), true);

        tvCarbsValue.setText((int)nutritionLog.getTotalCarbs() + " / " + (int)nutritionLog.getTargetCarbs() + "g");
        carbsProgress.setProgress(Math.min((int)(nutritionLog.getTotalCarbs() * 100 / nutritionLog.getTargetCarbs()), 100), true);

        tvFatsValue.setText((int)nutritionLog.getTotalFats() + " / " + (int)nutritionLog.getTargetFats() + "g");
        fatsProgress.setProgress(Math.min((int)(nutritionLog.getTotalFats() * 100 / nutritionLog.getTargetFats()), 100), true);
    }

    private void showAddMealDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_meal, null);
        
        AutoCompleteTextView etMealType = dialogView.findViewById(R.id.etMealType);
        TextInputEditText etMealName = dialogView.findViewById(R.id.etMealName);
        TextInputEditText etCalories = dialogView.findViewById(R.id.etCalories);
        TextInputEditText etProtein = dialogView.findViewById(R.id.etProtein);
        TextInputEditText etCarbs = dialogView.findViewById(R.id.etCarbs);
        TextInputEditText etFats = dialogView.findViewById(R.id.etFats);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown, mealTypes);
        etMealType.setAdapter(adapter);
        etMealType.setText(mealTypes[0], false);
        etMealType.setOnClickListener(v -> etMealType.showDropDown());
        
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_Fit_lifeGym)
            .setTitle("Log New Meal")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Log Meal", null)
            .setNegativeButton("Cancel", (d, which) -> d.dismiss())
            .create();

        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            String type = etMealType.getText().toString().trim();
            String mealName = etMealName.getText().toString().trim();
            String caloriesStr = etCalories.getText().toString().trim();
            String proteinStr = etProtein.getText().toString().trim();
            String carbsStr = etCarbs.getText().toString().trim();
            String fatsStr = etFats.getText().toString().trim();
            
            if (mealName.isEmpty() || caloriesStr.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                int calories = Integer.parseInt(caloriesStr);
                double protein = proteinStr.isEmpty() ? 0 : Double.parseDouble(proteinStr);
                double carbs = carbsStr.isEmpty() ? 0 : Double.parseDouble(carbsStr);
                double fats = fatsStr.isEmpty() ? 0 : Double.parseDouble(fatsStr);
                
                FoodItem foodItem = new FoodItem(UUID.randomUUID().toString(), mealName, calories);
                foodItem.setProtein(protein);
                foodItem.setCarbs(carbs);
                foodItem.setFats(fats);
                
                Meal meal = new Meal(sessionManager.getUserId(), type.toUpperCase(), mealName);
                meal.setId(UUID.randomUUID().toString());
                meal.addFoodItem(foodItem);
                
                nutritionLog.addMeal(meal);
                mealAdapter.notifyDataSetChanged();
                updateUI();
                
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid numbers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
