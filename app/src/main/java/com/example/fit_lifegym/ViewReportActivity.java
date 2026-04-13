package com.example.fit_lifegym;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fit_lifegym.models.MedicalReport;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ViewReportActivity extends AppCompatActivity {

    private TextView tvDoctorName, tvReportDate, tvPatientName;
    private TextView tvWeight, tvBP, tvBMI, tvHeartRate;
    private TextView tvSymptoms, tvDiagnosis, tvExercises, tvDietary, tvRecommendations;
    private MaterialButton btnDownloadPDF;
    private LinearLayout pdfContentLayout;
    
    private DatabaseReference reportsRef;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);

        bookingId = getIntent().getStringExtra("bookingId");
        reportsRef = FirebaseDatabase.getInstance().getReference("medical_reports");

        initViews();
        setupToolbar();
        
        if (bookingId != null) {
            loadReport();
        } else {
            Toast.makeText(this, "Report not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnDownloadPDF.setOnClickListener(v -> generatePDF());
    }

    private void initViews() {
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvReportDate = findViewById(R.id.tvReportDate);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvWeight = findViewById(R.id.tvWeight);
        tvBP = findViewById(R.id.tvBP);
        tvBMI = findViewById(R.id.tvBMI);
        tvHeartRate = findViewById(R.id.tvHeartRate);
        tvSymptoms = findViewById(R.id.tvSymptoms);
        tvDiagnosis = findViewById(R.id.tvDiagnosis);
        tvExercises = findViewById(R.id.tvExercises);
        tvDietary = findViewById(R.id.tvDietary);
        tvRecommendations = findViewById(R.id.tvRecommendations);
        btnDownloadPDF = findViewById(R.id.btnDownloadPDF);
        pdfContentLayout = findViewById(R.id.pdfContentLayout);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void loadReport() {
        reportsRef.orderByChild("bookingId").equalTo(bookingId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            MedicalReport report = data.getValue(MedicalReport.class);
                            if (report != null) {
                                displayReport(report);
                                return;
                            }
                        }
                    } else {
                        Toast.makeText(ViewReportActivity.this, "No report available", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ViewReportActivity.this, "Error loading report", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void displayReport(MedicalReport report) {
        tvDoctorName.setText("Dr. " + report.getDoctorName());
        tvPatientName.setText("Patient: " + report.getMemberName());
        
        if (report.getReportDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            tvReportDate.setText("Issued on " + sdf.format(report.getReportDate()));
        }

        tvWeight.setText(report.getWeight() != null && !report.getWeight().isEmpty() ? report.getWeight() + " kg" : "N/A");
        tvBP.setText(report.getBloodPressure() != null && !report.getBloodPressure().isEmpty() ? report.getBloodPressure() : "N/A");
        tvBMI.setText(report.getBmi() != null && !report.getBmi().isEmpty() ? report.getBmi() : "N/A");
        tvHeartRate.setText(report.getHeartRate() != null && !report.getHeartRate().isEmpty() ? report.getHeartRate() + " bpm" : "N/A");

        tvSymptoms.setText(report.getSymptoms() != null && !report.getSymptoms().isEmpty() ? report.getSymptoms() : "None reported");
        tvDiagnosis.setText(report.getDiagnosis() != null && !report.getDiagnosis().isEmpty() ? report.getDiagnosis() : "N/A");
        tvExercises.setText(report.getPrescribedExercises() != null && !report.getPrescribedExercises().isEmpty() ? report.getPrescribedExercises() : "None prescribed");
        tvDietary.setText(report.getDietaryAdvice() != null && !report.getDietaryAdvice().isEmpty() ? report.getDietaryAdvice() : "None provided");
        tvRecommendations.setText(report.getRecommendations() != null && !report.getRecommendations().isEmpty() ? report.getRecommendations() : "None provided");
    }

    private void generatePDF() {
        // Create a bitmap from the layout
        Bitmap bitmap = Bitmap.createBitmap(pdfContentLayout.getWidth(), pdfContentLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        pdfContentLayout.draw(canvas);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas pageCanvas = page.getCanvas();
        pageCanvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        String fileName = "Medical_Report_" + System.currentTimeMillis() + ".pdf";
        
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContentResolver().openOutputStream(uri);
            } else {
                java.io.File file = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                fos = new java.io.FileOutputStream(file);
            }

            if (fos != null) {
                document.writeTo(fos);
                fos.close();
                Toast.makeText(this, "PDF Downloaded to Downloads folder", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }
}
