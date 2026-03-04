package com.example.fit_lifegym;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.models.Booking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        bookingId = getIntent().getStringExtra("bookingId");

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
            public void onDataChange(DataSnapshot snapshot) {
                booking = snapshot.getValue(Booking.class);
                if (booking != null) {
                    tvProfessionalName.setText(booking.getProfessionalName());
                    tvServiceType.setText(booking.getServiceType());
                    tvDate.setText(new java.text.SimpleDateFormat("MMM dd, yyyy", 
                        java.util.Locale.getDefault()).format(booking.getDate()));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RateSessionActivity.this, "Error loading booking", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitRating() {
        float rating = ratingBar.getRating();
        String review = etReview.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        bookingsRef.child(bookingId).child("rating").setValue((int) rating);
        bookingsRef.child(bookingId).child("review").setValue(review)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to submit rating", Toast.LENGTH_SHORT).show();
            });
    }
}
