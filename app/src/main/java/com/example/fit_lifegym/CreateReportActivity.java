package com.example.fit_lifegym;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fit_lifegym.models.MedicalReport;
import com.example.fit_lifegym.utils.NotificationHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.example.fit_lifegym.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateReportActivity extends AppCompatActivity {

    private TextView tvMemberName, tvBookingDate;
    private TextInputEditText etWeight, etBP, etBMI, etHeartRate;
    private TextInputLayout tilWeight, tilBP, tilBMI, tilHeartRate;
    private TextInputEditText etSymptoms, etDiagnosis, etExercises, etDietary, etRecommendations;
    private MaterialButton btnSubmitReport;
    
    private DatabaseReference reportsRef, userEventsRef;
    private SessionManager sessionManager;
    private NotificationHelper notificationHelper;
    private String bookingId, memberId, memberName;
    private long bookingDateLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);

        sessionManager = new SessionManager(this);
        notificationHelper = new NotificationHelper(this);
        reportsRef = FirebaseDatabase.getInstance().getReference("medical_reports");
        userEventsRef = FirebaseDatabase.getInstance().getReference("users");

        bookingId = getIntent().getStringExtra("bookingId");
        memberId = getIntent().getStringExtra("memberId");
        memberName = getIntent().getStringExtra("memberName");
        bookingDateLong = getIntent().getLongExtra("bookingDate", 0);

        initViews();
        setupToolbar();
        
        tvMemberName.setText("Name: " + memberName);
        if (bookingDateLong > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            tvBookingDate.setText("Session Date: " + sdf.format(new Date(bookingDateLong)));
        }

        btnSubmitReport.setOnClickListener(v -> submitReport());
    }

    private void initViews() {
        tvMemberName = findViewById(R.id.tvMemberName);
        tvBookingDate = findViewById(R.id.tvBookingDate);
        
        etWeight = findViewById(R.id.etWeight);
        tilWeight = findViewById(R.id.tilWeight);
        
        etBP = findViewById(R.id.etBP);
        tilBP = findViewById(R.id.tilBP);
        
        etBMI = findViewById(R.id.etBMI);
        tilBMI = findViewById(R.id.tilBMI);
        
        etHeartRate = findViewById(R.id.etHeartRate);
        tilHeartRate = findViewById(R.id.tilHeartRate);
        
        etSymptoms = findViewById(R.id.etSymptoms);
        etDiagnosis = findViewById(R.id.etDiagnosis);
        etExercises = findViewById(R.id.etExercises);
        etDietary = findViewById(R.id.etDietary);
        etRecommendations = findViewById(R.id.etRecommendations);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        etWeight.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateWeight(s.toString());
            }
        });

        etBP.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateBloodPressure(s.toString());
            }
        });

        etBMI.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateBMI(s.toString());
            }
        });

        etHeartRate.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateHeartRate(s.toString());
            }
        });
    }

    private boolean validateWeight(String weight) {
        if (weight.isEmpty()) {
            tilWeight.setError("Weight is required");
            return false;
        }
        try {
            double w = Double.parseDouble(weight);
            if (w < 10 || w > 500) {
                tilWeight.setError("Invalid weight (10-500 kg)");
                return false;
            }
        } catch (NumberFormatException e) {
            tilWeight.setError("Invalid number");
            return false;
        }
        tilWeight.setError(null);
        return true;
    }

    private boolean validateBloodPressure(String bp) {
        if (bp.isEmpty()) {
            tilBP.setError("Blood pressure is required");
            return false;
        }
        if (!bp.matches("\\d{2,3}/\\d{2,3}")) {
            tilBP.setError("Use format: 120/80");
            return false;
        }
        tilBP.setError(null);
        return true;
    }

    private boolean validateBMI(String bmi) {
        if (bmi.isEmpty()) {
            tilBMI.setError("BMI is required");
            return false;
        }
        try {
            double b = Double.parseDouble(bmi);
            if (b < 10 || b > 100) {
                tilBMI.setError("Invalid BMI (10-100)");
                return false;
            }
        } catch (NumberFormatException e) {
            tilBMI.setError("Invalid number");
            return false;
        }
        tilBMI.setError(null);
        return true;
    }

    private boolean validateHeartRate(String hr) {
        if (hr.isEmpty()) {
            tilHeartRate.setError("Heart rate is required");
            return false;
        }
        try {
            int h = Integer.parseInt(hr);
            if (h < 30 || h > 250) {
                tilHeartRate.setError("Invalid rate (30-250 bpm)");
                return false;
            }
        } catch (NumberFormatException e) {
            tilHeartRate.setError("Invalid number");
            return false;
        }
        tilHeartRate.setError(null);
        return true;
    }

    private abstract class SimpleTextWatcher implements android.text.TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(android.text.Editable s) {}
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void submitReport() {
        String weight = etWeight.getText().toString().trim();
        String bp = etBP.getText().toString().trim();
        String bmi = etBMI.getText().toString().trim();
        String heartRate = etHeartRate.getText().toString().trim();
        String diagnosis = etDiagnosis.getText().toString().trim();

        boolean isValid = validateWeight(weight) & 
                         validateBloodPressure(bp) & 
                         validateBMI(bmi) & 
                         validateHeartRate(heartRate);

        if (!isValid) {
            Toast.makeText(this, "Please fix errors in the form", Toast.LENGTH_SHORT).show();
            return;
        }

        if (diagnosis.isEmpty()) {
            etDiagnosis.setError("Diagnosis is required");
            return;
        }

        String reportId = reportsRef.push().getKey();
        MedicalReport report = new MedicalReport();
        report.setId(reportId);
        report.setBookingId(bookingId);
        report.setMemberId(memberId);
        report.setMemberName(memberName);
        report.setDoctorId(sessionManager.getUserId());
        report.setDoctorName(sessionManager.getName());
        report.setReportDate(new Date());
        
        report.setWeight(weight);
        report.setBloodPressure(bp);
        report.setBmi(etBMI.getText().toString().trim());
        report.setHeartRate(etHeartRate.getText().toString().trim());
        report.setSymptoms(etSymptoms.getText().toString().trim());
        report.setDiagnosis(diagnosis);
        report.setPrescribedExercises(etExercises.getText().toString().trim());
        report.setDietaryAdvice(etDietary.getText().toString().trim());
        report.setRecommendations(etRecommendations.getText().toString().trim());

        if (reportId != null) {
            reportsRef.child(reportId).setValue(report)
                .addOnSuccessListener(aVoid -> {
                    // Update user's latest_event to trigger a notification or update their feed
                    Map<String, Object> update = new HashMap<>();
                    update.put("latest_event", "NEW_REPORT_FROM_" + sessionManager.getName());
                    userEventsRef.child(memberId).updateChildren(update);

                    Toast.makeText(this, "Report sent successfully to " + memberName, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }
}
