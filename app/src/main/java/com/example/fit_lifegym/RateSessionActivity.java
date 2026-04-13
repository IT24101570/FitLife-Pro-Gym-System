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

        int ratingValue = (int) ratingBar.getRating();
        String reviewText = etReview.getText().toString().trim();

        if (ratingValue == 0) {
            Toast.makeText(this, "Please select at least 1 star.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use updateChildren to avoid overwriting existing data
        Map<String, Object> updates = new HashMap<>();
        updates.put("rating", ratingValue);
        updates.put("review", reviewText);

        bookingsRef.child(bookingId).updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                updateProfessionalRating(booking.getProfessionalId(), ratingValue);
                finish();
            })
            .addOnFailureListener(e -> {
                Log.e("RateSession", "Update failed", e);
                Toast.makeText(this, "Submission failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateProfessionalRating(String professionalId, int newRating) {
        if (professionalId == null) return;
        
        DatabaseReference proRef = FirebaseDatabase.getInstance(FitLifeApplication.DATABASE_URL)
                .getReference("professionals").child(professionalId);

        proRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                com.example.fit_lifegym.models.Professional p = currentData.getValue(com.example.fit_lifegym.models.Professional.class);
                if (p == null) {
                    return com.google.firebase.database.Transaction.success(currentData);
                }

                int count = p.getRatingCount();
                double avg = p.getRating(); // This uses getRating() which might be the averageRating field

                double totalPoints = avg * count;
                count++;
                double newAvg = (totalPoints + newRating) / count;

                p.setRatingCount(count);
                p.setRating(newAvg);
                
                currentData.setValue(p);
                return com.google.firebase.database.Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    Log.e("RateSession", "Transaction failed", error.toException());
                }
            }
        });
    }
}
