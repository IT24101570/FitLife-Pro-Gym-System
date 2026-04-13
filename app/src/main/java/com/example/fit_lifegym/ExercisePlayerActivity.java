package com.example.fit_lifegym;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fit_lifegym.models.ExerciseAction;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExercisePlayerActivity extends AppCompatActivity {

    private List<ExerciseAction> exerciseList;
    private int currentIndex = 0;
    private CountDownTimer timer;
    private Handler animationHandler = new Handler();
    private Runnable animationRunnable;

    private ImageView ivExerciseAction;
    private TextView tvExerciseName, tvTimer, tvCounter;
    private CircularProgressIndicator progressTimer;
    private MaterialButton btnPauseResume;
    private boolean isPaused = false;
    private long millisRemaining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_player);

        exerciseList = (ArrayList<ExerciseAction>) getIntent().getSerializableExtra("exercise_list");
        if (exerciseList == null || exerciseList.isEmpty()) {
            finish();
            return;
        }

        ivExerciseAction = findViewById(R.id.ivExerciseAction);
        tvExerciseName = findViewById(R.id.tvExerciseName);
        tvTimer = findViewById(R.id.tvTimer);
        tvCounter = findViewById(R.id.tvCounter);
        progressTimer = findViewById(R.id.progressTimer);
        btnPauseResume = findViewById(R.id.btnPauseResume);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnNext).setOnClickListener(v -> nextExercise());
        findViewById(R.id.btnQuit).setOnClickListener(v -> finish());
        
        btnPauseResume.setOnClickListener(v -> {
            if (isPaused) {
                resumeExercise();
            } else {
                pauseExercise();
            }
        });

        startExercise();
    }

    private void startExercise() {
        if (currentIndex >= exerciseList.size()) {
            Toast.makeText(this, R.string.workout_completed, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ExerciseAction current = exerciseList.get(currentIndex);
        tvExerciseName.setText(current.getName());
        String counterText = (currentIndex + 1) + "/" + exerciseList.size();
        tvCounter.setText(counterText);
        
        isPaused = false;
        btnPauseResume.setText(R.string.pause);

        if (current.isRest()) {
            try {
                InputStream is = getAssets().open("images/arm_beginner.webp");
                Drawable d = Drawable.createFromStream(is, null);
                ivExerciseAction.setImageDrawable(d);
            } catch (IOException e) {
                e.printStackTrace();
            }
            tvExerciseName.setText(R.string.rest);
            startTimer(current.getDurationSeconds());
            if (animationRunnable != null) animationHandler.removeCallbacks(animationRunnable);
        } else {
            startAnimation(current.getFolderName());
            if (current.isTimed()) {
                startTimer(current.getDurationSeconds());
            } else {
                if (timer != null) timer.cancel();
                tvTimer.setText(getString(R.string.reps_format, current.getRepetitions()));
                progressTimer.setProgress(0);
                btnPauseResume.setText(R.string.done); // Change Pause to Done for rep exercises
            }
        }
    }

    private void pauseExercise() {
        isPaused = true;
        btnPauseResume.setText(R.string.resume);
        if (timer != null) timer.cancel();
        if (animationRunnable != null) animationHandler.removeCallbacks(animationRunnable);
    }

    private void resumeExercise() {
        ExerciseAction current = exerciseList.get(currentIndex);
        if (!current.isTimed() && !current.isRest()) {
            nextExercise(); // For rep-based exercises, "Resume" (Done) button goes to next
            return;
        }
        isPaused = false;
        btnPauseResume.setText(R.string.pause);
        startTimer((int) (millisRemaining / 1000));
        animationHandler.post(animationRunnable);
    }

    private int frameCount = 2;
    private int currentFrame = 1;

    private void startAnimation(String folderName) {
        if (animationRunnable != null) animationHandler.removeCallbacks(animationRunnable);

        // Detect number of frames in the folder
        try {
            String[] files = getAssets().list("img_exercise/" + folderName);
            if (files != null) {
                frameCount = 0;
                for (String file : files) {
                    if (file.endsWith(".webp")) frameCount++;
                }
            }
        } catch (IOException e) {
            frameCount = 2;
        }
        currentFrame = 1;

        animationRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String fileName = currentFrame + ".webp";
                    InputStream is = getAssets().open("img_exercise/" + folderName + "/" + fileName);
                    Drawable d = Drawable.createFromStream(is, null);
                    ivExerciseAction.setImageDrawable(d);
                    is.close();

                    currentFrame++;
                    if (currentFrame > frameCount) currentFrame = 1;
                } catch (IOException e) {
                    ivExerciseAction.setImageResource(R.drawable.first);
                }
                animationHandler.postDelayed(this, 1000); // Toggle every second
            }
        };
        animationHandler.post(animationRunnable);
    }

    private void startTimer(int seconds) {
        if (timer != null) timer.cancel();

        progressTimer.setMax(exerciseList.get(currentIndex).getDurationSeconds());
        timer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisRemaining = millisUntilFinished;
                int remaining = (int) (millisUntilFinished / 1000);
                String timeString = String.format(java.util.Locale.getDefault(), "%02d:%02d", remaining / 60, remaining % 60);
                tvTimer.setText(timeString);
                progressTimer.setProgress(remaining);
            }

            @Override
            public void onFinish() {
                nextExercise();
            }
        }.start();
    }

    private void nextExercise() {
        currentIndex++;
        startExercise();
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        if (animationRunnable != null) animationHandler.removeCallbacks(animationRunnable);
        super.onDestroy();
    }
}
