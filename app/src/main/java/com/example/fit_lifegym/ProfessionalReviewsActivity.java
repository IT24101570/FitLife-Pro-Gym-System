package com.example.fit_lifegym;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.models.Booking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ProfessionalReviewsActivity extends AppCompatActivity {

    private RecyclerView rvReviews;
    private TextView tvAverageRating, tvTotalReviews, tvNoReviews;
    private RatingBar averageRatingBar;
    private ImageView btnBack;
    
    private List<Booking> reviewList = new ArrayList<>();
    private ReviewAdapter adapter;
    private DatabaseReference bookingsRef;
    private String professionalId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_reviews);

        professionalId = new com.example.fit_lifegym.utils.SessionManager(this).getUserId();
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");

        initViews();
        loadReviews();
    }

    private void initViews() {
        rvReviews = findViewById(R.id.rvReviews);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalReviews = findViewById(R.id.tvTotalReviews);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        averageRatingBar = findViewById(R.id.averageRatingBar);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter(reviewList);
        rvReviews.setAdapter(adapter);
    }

    private void loadReviews() {
        bookingsRef.orderByChild("professionalId").equalTo(professionalId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    reviewList.clear();
                    double totalRating = 0;
                    int ratingCount = 0;

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Booking b = ds.getValue(Booking.class);
                        if (b != null && b.getRating() > 0) {
                            reviewList.add(b);
                            totalRating += b.getRating();
                            ratingCount++;
                        }
                    }

                    Collections.reverse(reviewList);
                    adapter.notifyDataSetChanged();

                    if (ratingCount > 0) {
                        double avg = totalRating / ratingCount;
                        tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", avg));
                        tvTotalReviews.setText("Based on " + ratingCount + " reviews");
                        averageRatingBar.setRating((float) avg);
                        tvNoReviews.setVisibility(View.GONE);
                    } else {
                        tvAverageRating.setText("0.0");
                        tvTotalReviews.setText("No reviews yet");
                        averageRatingBar.setRating(0);
                        tvNoReviews.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfessionalReviewsActivity.this, "Error loading reviews", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private static class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
        private List<Booking> reviews;

        ReviewAdapter(List<Booking> reviews) { this.reviews = reviews; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking b = reviews.get(position);
            holder.tvUserName.setText(b.getUserName() != null ? b.getUserName() : "Anonymous User");
            holder.ratingBar.setRating(b.getRating());
            holder.tvReview.setText(b.getReview());
            holder.tvService.setText(b.getServiceType());
            
            if (b.getDate() != null) {
                holder.tvDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(b.getDate()));
            }
        }

        @Override
        public int getItemCount() { return reviews.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserName, tvDate, tvReview, tvService;
            RatingBar ratingBar;

            ViewHolder(View itemView) {
                super(itemView);
                tvUserName = itemView.findViewById(R.id.tvUserName);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvReview = itemView.findViewById(R.id.tvReview);
                tvService = itemView.findViewById(R.id.tvService);
                ratingBar = itemView.findViewById(R.id.ratingBar);
            }
        }
    }
}
