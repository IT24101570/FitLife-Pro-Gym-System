package com.example.fit_lifegym;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fit_lifegym.adapters.OnboardingAdapter;
import com.example.fit_lifegym.models.OnboardingItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private MaterialButton btnNext, btnSkip;
    private OnboardingAdapter adapter;
    private List<OnboardingItem> onboardingItems;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (isOnboardingCompleted()) {
            navigateToConversationalOnboarding();
            return;
        }
        
        setContentView(R.layout.activity_onboarding);

        initViews();
        setupOnboardingItems();
        setupViewPager();
        setupListeners();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
    }

    private void setupOnboardingItems() {
        onboardingItems = new ArrayList<>();
        
        onboardingItems.add(new OnboardingItem(
            "Sync Your Gear",
            "Connect your smartwatch to track real-time heart rate and steps during every session. ⌚",
            R.drawable.first
        ));
        
        onboardingItems.add(new OnboardingItem(
            "Set Your Goals",
            "Choose between Muscle Gain, Weight Loss, or Endurance. We'll tailor everything for you. 🎯",
            R.drawable.second
        ));
        
        onboardingItems.add(new OnboardingItem(
            "Expert Coaching",
            "Access specialized workout and meal plans created by top trainers and nutritionists. 🥗",
            R.drawable.third
        ));
    }

    private void setupViewPager() {
        adapter = new OnboardingAdapter(onboardingItems);
        viewPager.setAdapter(adapter);
        
        setupDots();
        setCurrentDot(0);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                setCurrentDot(position);
                
                if (position == onboardingItems.size() - 1) {
                    btnNext.setText("Finish Setup");
                } else {
                    btnNext.setText("Next");
                }
            }
        });
    }

    private void setupDots() {
        dotsLayout.removeAllViews();
        for (int i = 0; i < onboardingItems.size(); i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(android.R.drawable.presence_invisible);
            dot.setBackgroundResource(R.drawable.button_secondary); 
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(12, 12);
            params.setMargins(8, 0, 8, 0);
            dotsLayout.addView(dot, params);
        }
    }

    private void setCurrentDot(int position) {
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            View dot = dotsLayout.getChildAt(i);
            if (dot != null) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
                if (i == position) {
                    params.width = 32;
                    dot.setAlpha(1.0f);
                } else {
                    params.width = 12;
                    dot.setAlpha(0.5f);
                }
                dot.setLayoutParams(params);
            }
        }
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            if (currentPage == onboardingItems.size() - 1) {
                completeOnboarding();
            } else {
                viewPager.setCurrentItem(currentPage + 1);
            }
        });
        
        btnSkip.setOnClickListener(v -> completeOnboarding());
    }

    private void completeOnboarding() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("onboarding_completed", true).apply();
        Toast.makeText(this, "Welcome to FitLife Gym!", Toast.LENGTH_SHORT).show();
        navigateToConversationalOnboarding();
    }

    private boolean isOnboardingCompleted() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        return prefs.getBoolean("onboarding_completed", false);
    }

    private void navigateToConversationalOnboarding() {
        Intent intent = new Intent(OnboardingActivity.this, ConversationalOnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
