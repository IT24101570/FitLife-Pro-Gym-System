package com.example.fit_lifegym;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.models.Recipe;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView btnBack, ivRecipeImage;
    private TextView tvRecipeName, tvCategory, tvIngredients, tvInstructions;
    private MaterialButton btnAddToGrocery;
    
    private String recipeId;
    private DatabaseReference recipeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId == null) recipeId = "sample_recipe";
        
        recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);

        initViews();
        loadRecipeData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        tvRecipeName = findViewById(R.id.tvRecipeName);
        tvCategory = findViewById(R.id.tvCategory);
        tvIngredients = findViewById(R.id.tvIngredients);
        tvInstructions = findViewById(R.id.tvInstructions);
        btnAddToGrocery = findViewById(R.id.btnAddToGrocery);

        btnBack.setOnClickListener(v -> finish());
        btnAddToGrocery.setOnClickListener(v -> {
            Toast.makeText(this, "Added to Grocery List!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadRecipeData() {
        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe != null) {
                    displayRecipe(recipe);
                } else {
                    displaySampleRecipe();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                displaySampleRecipe();
            }
        });
    }

    private void displayRecipe(Recipe recipe) {
        tvRecipeName.setText(recipe.getName());
        tvCategory.setText(recipe.getCategory().toUpperCase() + " • " + recipe.getCalories() + " kcal");
        
        StringBuilder ingredients = new StringBuilder();
        for (String ing : recipe.getIngredients()) {
            ingredients.append("• ").append(ing).append("\n");
        }
        tvIngredients.setText(ingredients.toString());

        StringBuilder instructions = new StringBuilder();
        for (int i = 0; i < recipe.getInstructions().size(); i++) {
            instructions.append(i + 1).append(". ").append(recipe.getInstructions().get(i)).append("\n");
        }
        tvInstructions.setText(instructions.toString());
    }

    private void displaySampleRecipe() {
        tvRecipeName.setText("Quinoa Protein Bowl");
        tvCategory.setText("LUNCH • 450 kcal");
        tvIngredients.setText("• 1 cup Quinoa\n• 200g Chicken Breast\n• 1 Avocado\n• Handful of Spinach");
        tvInstructions.setText("1. Cook quinoa according to package.\n2. Grill chicken until cooked through.\n3. Slice avocado and toss everything together.");
    }
}
