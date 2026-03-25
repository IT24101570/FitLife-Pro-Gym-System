package com.example.fit_lifegym;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.models.Booking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RateSessionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvProfessionalName, tvServiceType, tvDate;
    private RatingBar ratingBar;
    private EditText etReview;
    private Button btnSubmit;
    
    private DatabaseReference bookingsRef;
    private String bookingId;
    private Booking booking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_session);

        bookingsRef = FirebaseDatabase.getInstance(FitLifeApplication.DATABASE_URL).getReference("bookings");
        bookingId = getIntent().getStringExtra("bookingId");

        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Error: Booking ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadBookingDetails();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvProfessionalName = findViewById(R.id.tvProfessionalName);
        tvServiceType = findViewById(R.id.tvServiceType);
        tvDate = findViewById(R.id.tvDate);
        ratingBar = findViewById(R.id.ratingBar);
        etReview = findViewById(R.id.etReview);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submitRating());
    }

    private void loadBookingDetails() {
        bookingsRef.child(bookingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                booking = snapshot.getValue(Booking.class);
                if (booking != null) {
                    tvProfessionalName.setText(booking.getProfessionalName());
                    tvServiceType.setText(booking.getServiceType());
                    if (booking.getDate() != null) {
                        tvDate.setText(new java.text.SimpleDateFormat("MMM dd, yyyy", 
                            java.util.Locale.getDefault()).format(booking.getDate()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RateSessionActivity.this, "Error loading details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitRating() {
        if (booking == null) {
            Toast.makeText(this, "Please wait for details to load.", Toast.LENGTH_SHORT).show();
            return;
        }

        float ratingValue = ratingBar.getRating();
        String reviewText = etReview.getText().toString().trim();

        if (ratingValue == 0) {
            Toast.makeText(this, "Please select at least 1 star.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use updateChildren to avoid overwriting existing data
        Map<String, Object> updates = new HashMap<>();
        updates.put("rating", (int) ratingValue);
        updates.put("review", reviewText);

        bookingsRef.child(bookingId).updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                updateProfessionalRating(booking.getProfessionalId());
                finish();
            })
            .addOnFailureListener(e -> {
                Log.e("RateSession", "Update failed", e);
                Toast.makeText(this, "Submission failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateProfessionalRating(String professionalId) {
        if (professionalId == null) return;
        
        DatabaseReference proRef = FirebaseDatabase.getInstance(FitLifeApplication.DATABASE_URL)
                .getReference("professionals").child(professionalId);

        // Fetch all bookings for this professional to calculate new average
        bookingsRef.orderByChild("professionalId").equalTo(professionalId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double totalRating = 0;
                    int ratingCount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Booking b = ds.getValue(Booking.class);
                        if (b != null && b.getRating() > 0) {
                            totalRating += b.getRating();
                            ratingCount++;
                        }
                    }

                    if (ratingCount > 0) {
                        Map<String, Object> proUpdates = new HashMap<>();
                        proUpdates.put("averageRating", totalRating / ratingCount);
                        proUpdates.put("ratingCount", ratingCount);
                        proRef.updateChildren(proUpdates);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("RateSession", "Error updating pro rating", error.toException());
                }
            });
    }
}
