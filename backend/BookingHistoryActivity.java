package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.models.Booking;
import com.example.fit_lifegym.utils.SessionManager;
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

public class BookingHistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView recyclerView;
    private View tvEmpty;
    private TextView tvTotalBookings, tvUpcoming, tvCompleted;
    private BookingAdapter adapter;
    
    private SessionManager sessionManager;
    private DatabaseReference bookingsRef;
    private List<Booking> bookingList;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        sessionManager = new SessionManager(this);
        userRole = sessionManager.getRole();
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        bookingList = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        loadBookings();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);

        View cardTotal = findViewById(R.id.cardTotal);
        if (cardTotal != null) {
            ((TextView)cardTotal.findViewById(R.id.tvStatLabel)).setText("Total");
            tvTotalBookings = cardTotal.findViewById(R.id.tvStatValue);
        }

        View cardUpcoming = findViewById(R.id.cardUpcoming);
        if (cardUpcoming != null) {
            ((TextView)cardUpcoming.findViewById(R.id.tvStatLabel)).setText("Pending");
            tvUpcoming = cardUpcoming.findViewById(R.id.tvStatValue);
        }

        View cardCompleted = findViewById(R.id.cardCompleted);
        if (cardCompleted != null) {
            ((TextView)cardCompleted.findViewById(R.id.tvStatLabel)).setText("Done");
            tvCompleted = cardCompleted.findViewById(R.id.tvStatValue);
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new BookingAdapter(bookingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadBookings() {
        String userId = sessionManager.getUserId();
        
        // If Professional, filter by professionalId, else by userId
        boolean isPro = "TRAINER".equals(userRole) || "DOCTOR".equals(userRole);
        String filterField = isPro ? "professionalId" : "userId";
        
        bookingsRef.orderByChild(filterField).equalTo(userId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    bookingList.clear();
                    int pendingCount = 0;
                    int completedCount = 0;
                    
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Booking booking = data.getValue(Booking.class);
                        if (booking != null) {
                            booking.setId(data.getKey());
                            bookingList.add(booking);
                            
                            if ("PENDING".equals(booking.getStatus())) {
                                pendingCount++;
                            } else if ("COMPLETED".equals(booking.getStatus())) {
                                completedCount++;
                            }
                        }
                    }
                    
                    Collections.reverse(bookingList);
                    adapter.notifyDataSetChanged();
                    
                    if (tvTotalBookings != null) tvTotalBookings.setText(String.valueOf(bookingList.size()));
                    if (tvUpcoming != null) tvUpcoming.setText(String.valueOf(pendingCount));
                    if (tvCompleted != null) tvCompleted.setText(String.valueOf(completedCount));
                    
                    tvEmpty.setVisibility(bookingList.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(bookingList.isEmpty() ? View.GONE : View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(BookingHistoryActivity.this, "Error loading bookings", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void updateStatus(Booking booking, String newStatus) {
        bookingsRef.child(booking.getId()).child("status").setValue(newStatus)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show());
    }

    private class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
        private final List<Booking> bookings;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        public BookingAdapter(List<Booking> bookings) {
            this.bookings = bookings;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking booking = bookings.get(position);
            
            boolean isPro = "TRAINER".equals(userRole) || "DOCTOR".equals(userRole);
            
            holder.tvProfessionalName.setText(isPro ? booking.getUserName() : booking.getProfessionalName());
            holder.tvServiceType.setText(booking.getServiceType());
            
            if (booking.getBookingDate() != null) {
                holder.tvDate.setText(dateFormat.format(booking.getBookingDate()));
            }
            holder.tvTime.setText(booking.getTimeSlot());
            holder.tvStatus.setText(booking.getStatus());
            
            // Show health condition/member details for professionals
            if (isPro) {
                holder.layoutMemberInfo.setVisibility(View.VISIBLE);
                String health = booking.getHealthCondition();
                holder.tvHealthCondition.setText("Health Note: " + (health != null && !health.isEmpty() ? health : "No specific note provided"));
                
                // Status control logic
                if ("PENDING".equals(booking.getStatus())) {
                    holder.btnReschedule.setVisibility(View.VISIBLE);
                    holder.btnReschedule.setText("Confirm");
                    holder.btnReschedule.setOnClickListener(v -> updateStatus(booking, "CONFIRMED"));
                    holder.btnCancel.setVisibility(View.VISIBLE);
                    holder.btnCancel.setText("Reject");
                    holder.btnCancel.setOnClickListener(v -> updateStatus(booking, "CANCELLED"));
                } else if ("CONFIRMED".equals(booking.getStatus())) {
                    holder.btnReschedule.setVisibility(View.VISIBLE);
                    holder.btnReschedule.setText("Complete");
                    holder.btnReschedule.setOnClickListener(v -> updateStatus(booking, "COMPLETED"));
                    holder.btnCancel.setVisibility(View.GONE);
                } else {
                    holder.btnReschedule.setVisibility(View.GONE);
                    holder.btnCancel.setVisibility(View.GONE);
                }
            } else {
                // Member logic
                holder.layoutMemberInfo.setVisibility(View.GONE);
                holder.btnReschedule.setVisibility(View.GONE);
                if ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus())) {
                    holder.btnCancel.setVisibility(View.VISIBLE);
                    holder.btnCancel.setText("Cancel");
                    holder.btnCancel.setOnClickListener(v -> updateStatus(booking, "CANCELLED"));
                } else {
                    holder.btnCancel.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return bookings.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvProfessionalName, tvServiceType, tvDate, tvTime, tvStatus, tvHealthCondition;
            Button btnCancel, btnReschedule;
            LinearLayout layoutMemberInfo;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvProfessionalName = itemView.findViewById(R.id.tvProfessionalName);
                tvServiceType = itemView.findViewById(R.id.tvServiceType);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvHealthCondition = itemView.findViewById(R.id.tvHealthCondition);
                btnCancel = itemView.findViewById(R.id.btnCancel);
                btnReschedule = itemView.findViewById(R.id.btnReschedule);
                layoutMemberInfo = itemView.findViewById(R.id.layoutMemberInfo);
            }
        }
    }
}
