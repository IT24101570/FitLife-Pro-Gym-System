package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ExerciseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        setupCategory(R.id.cardBeginner, "Beginner", "Start your journey", "beginner", "exerciseImage/other/img_beginner.webp");
        setupCategory(R.id.cardIntermediate, "Intermediate", "Push your limits", "intermediate", "exerciseImage/other/img_intermediate.webp");
        setupCategory(R.id.cardAdvance, "Advance", "Elite performance", "advance", "exerciseImage/other/img_advanced.webp");
        setupCategory(R.id.cardDiscover, "Discover", "Explore new routines", "discover", "exerciseImage/other/img_dart_board.webp");
        setupCategory(R.id.cardOthers, "Others", "Specialized workouts", "other", "exerciseImage/other/img_keep_fit_round.webp");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupCategory(int id, String name, String desc, String folder, String imagePath) {
        android.view.View view = findViewById(id);
        TextView tvName = view.findViewById(R.id.tvCategoryName);
        TextView tvDesc = view.findViewById(R.id.tvCategoryDescription);
        ImageView ivBackground = view.findViewById(R.id.ivCategoryBackground);
        
        tvName.setText(name);
        tvDesc.setText(desc);

        try {
            java.io.InputStream is = getAssets().open(imagePath);
            android.graphics.drawable.Drawable d = android.graphics.drawable.Drawable.createFromStream(is, null);
            ivBackground.setImageDrawable(d);
            is.close();
        } catch (java.io.IOException e) {
            // Fallback to high-quality assets if asset loading fails
            if (name.equals("Beginner")) ivBackground.setImageResource(R.drawable.first);
            else if (name.equals("Intermediate")) ivBackground.setImageResource(R.drawable.second);
            else if (name.equals("Advance")) ivBackground.setImageResource(R.drawable.third);
            else if (name.equals("Discover")) ivBackground.setImageResource(R.drawable.fourth);
            else ivBackground.setImageResource(R.drawable.fifth);
        }

        view.setOnClickListener(v -> {
            Intent intent = new Intent(ExerciseActivity.this, ExerciseCategoryActivity.class);
            intent.putExtra("category_name", name);
            intent.putExtra("category_folder", folder);
            startActivity(intent);
        });
    }
}
