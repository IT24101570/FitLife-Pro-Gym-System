package com.example.fit_lifegym;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.fit_lifegym.models.FitnessGoalSubmission;
import com.example.fit_lifegym.utils.FirebaseHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PersonalizedPlanActivity extends AppCompatActivity {

    private RadioGroup rgGoal;
    private TextInputEditText etNotes;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;

    private ImageView ivPhoto1, ivPhoto2;
    private Uri photoUri1, photoUri2;
    private int currentPhotoSlot = 0;

    private SessionManager sessionManager;
    private DatabaseReference databaseReference;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (currentPhotoSlot == 1) {
                        photoUri1 = imageUri;
                        ivPhoto1.setImageURI(imageUri);
                        ivPhoto1.setPadding(0, 0, 0, 0);
                    } else if (currentPhotoSlot == 2) {
                        photoUri2 = imageUri;
                        ivPhoto2.setImageURI(imageUri);
                        ivPhoto2.setPadding(0, 0, 0, 0);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalized_plan);

        sessionManager = new SessionManager(this);
        databaseReference = FirebaseHelper.getDbRef().child("personalized_plans");

        initViews();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        rgGoal = findViewById(R.id.rgGoal);
        etNotes = findViewById(R.id.etNotes);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        ivPhoto1 = findViewById(R.id.ivPhoto1);
        ivPhoto2 = findViewById(R.id.ivPhoto2);

        ivPhoto1.setOnClickListener(v -> {
            currentPhotoSlot = 1;
            openGallery();
        });
        ivPhoto2.setOnClickListener(v -> {
            currentPhotoSlot = 2;
            openGallery();
        });

        btnSubmit.setOnClickListener(v -> submitPlan());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void submitPlan() {
        int selectedId = rgGoal.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a goal", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoUri1 == null || photoUri2 == null) {
            Toast.makeText(this, "Please upload both progress photos", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton rb = findViewById(selectedId);
        String goal = rb.getText().toString();
        String notes = etNotes.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        uploadImagesToCloudinary(goal, notes);
    }

    private void uploadImagesToCloudinary(String goal, String notes) {
        List<String> uploadedUrls = Collections.synchronizedList(new ArrayList<>());
        Uri[] uris = {photoUri1, photoUri2};
        final int totalImages = uris.length;

        for (Uri uri : uris) {
            String requestId = MediaManager.get().upload(uri)
                    .unsigned("ml_default") // Ensure you have an unsigned upload preset named 'ml_default' in Cloudinary
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String url = (String) resultData.get("secure_url");
                            uploadedUrls.add(url);
                            if (uploadedUrls.size() == totalImages) {
                                saveSubmissionToDatabase(goal, notes, uploadedUrls);
                            }
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                btnSubmit.setEnabled(true);
                                Toast.makeText(PersonalizedPlanActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    }).dispatch();
        }
    }

    private void saveSubmissionToDatabase(String goal, String notes, List<String> photoUrls) {
        String userId = sessionManager.getUserId();
        String userName = sessionManager.getName();
        FitnessGoalSubmission submission = new FitnessGoalSubmission(
                userId, userId, userName, goal, notes, photoUrls, System.currentTimeMillis()
        );

        databaseReference.child(userId).setValue(submission)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Plan request submitted successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
