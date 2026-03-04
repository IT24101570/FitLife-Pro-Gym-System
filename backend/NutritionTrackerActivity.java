package com.example.fit_lifegym;

import android.content.DialogInterface;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.adapters.MealAdapter;
import com.example.fit_lifegym.models.FoodItem;
import com.example.fit_lifegym.models.Meal;
import com.example.fit_lifegym.models.NutritionLog;
import com.example.fit_lifegym.utils.SessionManager;
import com.example.fit_lifegym.utils.ValidationUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;
import java.util.UUID;

public class NutritionTrackerActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvRemainingCalories, tvEaten, tvBurned;
    private TextView tvProteinValue, tvCarbsValue, tvFatsValue;
    private CircularProgressIndicator calorieProgress;
    private LinearProgressIndicator proteinProgress, carbsProgress, fatsProgress;
    private RecyclerView rvMeals;
    private View btnAddMeal;

    private MealAdapter mealAdapter;
    private SessionManager sessionManager;
    private NutritionLog nutritionLog;

    private final String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack", "Pre-Workout", "Post-Workout"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_tracker);

        sessionManager = new SessionManager(this);
        initializeViews();
        setupNutritionLog();
        setupRecyclerView();
        setupListeners();
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
    }

    private void setupNutritionLog() {
        nutritionLog = new NutritionLog(sessionManager.getUserId(), new Date());
        nutritionLog.setTargetCalories(2400);
        nutritionLog.setTargetProtein(180);
        nutritionLog.setTargetCarbs(220);
        nutritionLog.setTargetFats(70);
    }

    private void setupRecyclerView() {
        rvMeals.setLayoutManager(new LinearLayoutManager(this));
        mealAdapter = new MealAdapter(nutritionLog.getMeals(), null);
        rvMeals.setAdapter(mealAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddMeal.setOnClickListener(v -> showAddMealDialog());
    }

    private void updateUI() {
        int consumed = nutritionLog.getTotalCalories();
        int target = nutritionLog.getTargetCalories();
        int remaining = target - consumed;

        tvRemainingCalories.setText(String.valueOf(remaining));
        tvEaten.setText(String.valueOf(consumed));
        tvBurned.setText("450");

        calorieProgress.setProgress(Math.min((consumed * 100) / target, 100), true);

        tvProteinValue.setText((int)nutritionLog.getTotalProtein() + "g");
        proteinProgress.setProgress(Math.min((int)(nutritionLog.getTotalProtein() * 100 / nutritionLog.getTargetProtein()), 100), true);

        tvCarbsValue.setText((int)nutritionLog.getTotalCarbs() + "g");
        carbsProgress.setProgress(Math.min((int)(nutritionLog.getTotalCarbs() * 100 / nutritionLog.getTargetCarbs()), 100), true);

        tvFatsValue.setText((int)nutritionLog.getTotalFats() + "g");
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
            .setCancelable(false) // Fix: Prevent dialog from disappearing when touching outside
            .setPositiveButton("Log Meal", null) // Set null listener here to handle validation manually
            .setNegativeButton("Cancel", (d, which) -> d.dismiss())
            .create();

        dialog.show();

        // Fix: Manually handle the Positive Button to prevent auto-closing on error and style it
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        
        // Premium Styling for buttons
        positiveButton.setTextColor(getResources().getColor(R.color.accent));
        negativeButton.setTextColor(getResources().getColor(R.color.text_secondary));
        positiveButton.setTransformationMethod(null); // Disable all caps
        negativeButton.setTransformationMethod(null);

        positiveButton.setOnClickListener(v -> {
            String type = etMealType.getText().toString().trim();
            String mealName = etMealName.getText().toString().trim();
            String caloriesStr = etCalories.getText().toString().trim();
            String proteinStr = etProtein.getText().toString().trim();
            String carbsStr = etCarbs.getText().toString().trim();
            String fatsStr = etFats.getText().toString().trim();
            
            if (type.isEmpty()) {
                Toast.makeText(this, "Please select or type a meal type", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mealName.isEmpty()) {
                etMealName.setError("Enter meal name");
                return;
            }
            
            if (!ValidationUtils.isValidCalorie(caloriesStr)) {
                etCalories.setError("Enter valid calories (0-5000)");
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
                
                Toast.makeText(this, "Meal logged successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // Only dismiss when successful
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid numbers entered", Toast.LENGTH_SHORT).show();
            }
        });

        // Add TextWatcher to highlight button when form is valid (Optional "Highlighting")
        TextWatcher formWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isValid = !etMealName.getText().toString().trim().isEmpty() && 
                                 !etCalories.getText().toString().trim().isEmpty();
                positiveButton.setAlpha(isValid ? 1.0f : 0.5f);
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etMealName.addTextChangedListener(formWatcher);
        etCalories.addTextChangedListener(formWatcher);
        positiveButton.setAlpha(0.5f); // Start slightly dimmed
    }
}
