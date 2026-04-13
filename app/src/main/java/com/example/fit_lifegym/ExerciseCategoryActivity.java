package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fit_lifegym.adapters.ExerciseSubCategoryAdapter;
import com.example.fit_lifegym.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class ExerciseCategoryActivity extends AppCompatActivity implements ExerciseSubCategoryAdapter.OnSubCategoryClickListener {

    private String categoryName;
    private String folder;
    private SessionManager sessionManager;
    private RecyclerView rvSubCategories;
    private ExerciseSubCategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_category);

        sessionManager = new SessionManager(this);
        categoryName = getIntent().getStringExtra("category_name");
        folder = getIntent().getStringExtra("category_folder");

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(categoryName);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvSubCategories = findViewById(R.id.rvSubCategories);
        rvSubCategories.setLayoutManager(new LinearLayoutManager(this));

        loadSubCategories();
    }

    private void loadSubCategories() {
        List<ExerciseSubCategoryAdapter.SubCategory> list = new ArrayList<>();
        
        if ("advance".equals(folder)) {
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Abs Advanced", "exerciseImage/advance", "abs_advanced_men.webp", 15, "abs"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Chest Advanced", "exerciseImage/advance", "chest_advanced_men.webp", 15, "chest"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Arm Advanced", "exerciseImage/advance", "arm_advanced_men.webp", 15, "arm"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Leg Advanced", "exerciseImage/advance", "leg_advanced_men.webp", 15, "leg"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Shoulder & Back Advanced", "exerciseImage/advance", "shoulder_back_advanced_men.webp", 15, "shoulder_back"));
        } else if ("beginner".equals(folder)) {
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Abs Beginner", "exerciseImage/beginner", "abs_beginner_men.webp", 8, "abs"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Chest Beginner", "exerciseImage/beginner", "chest_beginner_men.webp", 8, "chest"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Arm Beginner", "exerciseImage/beginner", "arm_beginner_men.webp", 8, "arm"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Leg Beginner", "exerciseImage/beginner", "leg_beginner_men.webp", 8, "leg"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Shoulder & Back Beginner", "exerciseImage/beginner", "shoulder_back_beginner_men.webp", 8, "shoulder_back"));
        } else if ("intermediate".equals(folder)) {
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Abs Intermediate", "exerciseImage/intermediate", "abs_intermediate_men.webp", 12, "abs"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Chest Intermediate", "exerciseImage/intermediate", "chest_intermediate_men.webp", 12, "chest"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Arm Intermediate", "exerciseImage/intermediate", "arm_intermediate_men.webp", 12, "arm"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Leg Intermediate", "exerciseImage/intermediate", "leg_intermediate_men.webp", 12, "leg"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Shoulder & Back Intermediate", "exerciseImage/intermediate", "shoulder_back_intermediate_men.webp", 12, "shoulder_back"));
        } else if ("discover".equals(folder)) {
            list.add(new ExerciseSubCategoryAdapter.SubCategory("7 Min Lose Arm Fat", "exerciseImage/discover/subPlan", "ic_7_min_lose_arm_fat.webp", 7, "arm"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Killer Core HIIT", "exerciseImage/discover/subPlan", "ic_killer_core_hiit.webp", 10, "abs"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Legs & Butt Workout", "exerciseImage/discover/subPlan", "ic_legs_butt_workout.webp", 12, "leg"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Plank Challenge", "exerciseImage/discover/subPlan", "ic_plank_challenge.webp", 5, "abs"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Butt Blaster", "exerciseImage/discover/subPlan", "ic_butt_blaster.webp", 15, "leg"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Intense Inner Thigh", "exerciseImage/discover/subPlan", "ic_intense_inner.webp", 10, "leg"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Build Wider Shoulder", "exerciseImage/discover/subPlan", "ic_build_wider_shoulder.webp", 12, "shoulder_back"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Killer Chest Workout", "exerciseImage/discover/subPlan", "ic_killer_chest_workout.webp", 15, "chest"));
        } else if ("other".equals(folder)) {
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Best Quarantine Workout", "exerciseImage/other", "best_quarantine_men.webp", 10, "abs"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Full Body Workout", "exerciseImage/other", "full_body_men.webp", 20, "chest"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Lower Body Workout", "exerciseImage/other", "lower_body_men.webp", 15, "leg"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Six Pack Abs", "exerciseImage/other", "ic_six_pack.webp", 10, "abs"));
            list.add(new ExerciseSubCategoryAdapter.SubCategory("Chest Focus", "exerciseImage/other", "ic_chest.webp", 12, "chest"));
        }

        adapter = new ExerciseSubCategoryAdapter(this, list, this);
        rvSubCategories.setAdapter(adapter);
    }

    @Override
    public void onSubCategoryClick(ExerciseSubCategoryAdapter.SubCategory subCategory) {
        // Intermediate and Advance categories require Premium/Elite membership
        boolean isRestricted = "intermediate".equals(folder) || "advance".equals(folder);
        
        if (isRestricted && !sessionManager.isPremiumUser()) {
            showPremiumDialog();
            return;
        }
        Intent intent = new Intent(this, ExerciseListActivity.class);
        intent.putExtra("sub_category_name", subCategory.getName());
        intent.putExtra("type", subCategory.getType());
        intent.putExtra("difficulty", folder);
        intent.putExtra("exercise_count", subCategory.getExerciseCount());
        startActivity(intent);
    }

    private void showPremiumDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.elite_feature)
                .setMessage(R.string.premium_only_message)
                .setPositiveButton(R.string.upgrade, (dialog, which) -> {
                    startActivity(new Intent(this, SubscriptionActivity.class));
                })
                .setNegativeButton(R.string.maybe_later, null)
                .show();
    }
}
