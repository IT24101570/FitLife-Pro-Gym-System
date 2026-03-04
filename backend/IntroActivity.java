package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.utils.SessionManager;

public class IntroActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToHome();
            return;
        }

        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToOnboarding();
            }
        });

        // Auto-navigate after 3 seconds if no interaction
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && !sessionManager.isLoggedIn()) {
                    navigateToOnboarding();
                }
            }
        }, 3000);
    }

    private void navigateToOnboarding() {
        startActivity(new Intent(IntroActivity.this, OnboardingActivity.class));
        finish();
    }

    private void navigateToHome() {
        startActivity(new Intent(IntroActivity.this, MainActivity.class));
        finish();
    }
}
