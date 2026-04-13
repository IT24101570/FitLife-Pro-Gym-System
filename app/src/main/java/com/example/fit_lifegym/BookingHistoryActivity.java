package com.example.fit_lifegym;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.example.fit_lifegym.services.MainService;
import com.example.fit_lifegym.services.MainServiceRepository;
import com.example.fit_lifegym.utils.DataModel;
import com.example.fit_lifegym.utils.DataModelType;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.permissionx.guolindev.PermissionX;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BookingHistoryActivity extends AppCompatActivity implements MainService.IncomingCallListener {

    private static final String TAG = "BookingHistoryActivity";
    private ImageView btnBack;
    private RecyclerView recyclerView;
    private View tvEmpty;
    private TextView tvTotalBookings, tvUpcoming, tvCompleted;
    private BookingHistoryAdapter adapter;
    
    private SessionManager sessionManager;
    private DatabaseReference bookingsRef;
    private List<Booking> bookingList;
    private String userRole;
    private MainServiceRepository serviceRepo;

    // Incoming Call UI
    private View incomingCallLayout;
    private TextView incomingCallTitleTv;
    private Button acceptButton, declineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        sessionManager = new SessionManager(this);
        userRole = sessionManager.getRole();
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        bookingList = new ArrayList<>();
        serviceRepo = new MainServiceRepository(this);

        initializeViews();
        setupRecyclerView();
        loadBookings();

        // Ensure service is running to receive calls
        if (sessionManager.isLoggedIn()) {
            checkPermissionsAndStartService();
        }
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Incoming Call UI
        incomingCallLayout = findViewById(R.id.incomingCallLayout);
        incomingCallTitleTv = findViewById(R.id.incomingCallTitleTv);
        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);

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
        adapter = new BookingHistoryAdapter(bookingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void checkPermissionsAndStartService() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        PermissionX.init(this)
            .permissions(permissions)
            .request((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    serviceRepo.startService(sessionManager.getUserId());
                } else {
                    serviceRepo.startService(sessionManager.getUserId());
                }
            });
    }

    @Override
    public void onIncomingCall(DataModel model) {
        if (model == null || model.getType() == null) return;
        
        Log.d(TAG, "onIncomingCall: " + model.getType());
        runOnUiThread(() -> {
            if (model.getType() == DataModelType.StartVideoCall || 
                model.getType() == DataModelType.StartAudioCall ||
                model.getType() == DataModelType.Offer) {
                
                if (incomingCallLayout != null && incomingCallLayout.getVisibility() == View.VISIBLE) return;

                String displayName = (model.getSenderName() != null && !model.getSenderName().isEmpty()) 
                                    ? model.getSenderName() : model.getSender();
                
                if (incomingCallTitleTv != null) {
                    incomingCallTitleTv.setText(displayName + " is calling...");
                }
                if (incomingCallLayout != null) {
                    incomingCallLayout.setVisibility(View.VISIBLE);
                }
                
                if (acceptButton != null) {
                    acceptButton.setOnClickListener(v -> {
                        if (incomingCallLayout != null) incomingCallLayout.setVisibility(View.GONE);
                        Intent intent = new Intent(BookingHistoryActivity.this, CallActivity.class);
                        intent.putExtra("target", model.getSender());
                        intent.putExtra("targetName", displayName);
                        intent.putExtra("isVideoCall", model.getType() != DataModelType.StartAudioCall);
                        intent.putExtra("isCaller", false);
                        startActivity(intent);
                    });
                }
                
                if (declineButton != null) {
                    declineButton.setOnClickListener(v -> {
                        if (incomingCallLayout != null) incomingCallLayout.setVisibility(View.GONE);
                        serviceRepo.sendEndCall();
                    });
                }
            } else if (model.getType() == DataModelType.EndCall) {
                if (incomingCallLayout != null) incomingCallLayout.setVisibility(View.GONE);
            }
        });
    }

    private void loadBookings() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;
        
        // If Professional, filter by professionalId, else by userId
        boolean isPro = "TRAINER".equals(userRole) || "DOCTOR".equals(userRole);
        String filterField = isPro ? "professionalId" : "userId";
        
        bookingsRef.orderByChild(filterField).equalTo(userId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                    
                    if (tvEmpty != null) tvEmpty.setVisibility(bookingList.isEmpty() ? View.VISIBLE : View.GONE);
                    if (recyclerView != null) recyclerView.setVisibility(bookingList.isEmpty() ? View.GONE : View.VISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(BookingHistoryActivity.this, "Error loading bookings", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void updateStatus(Booking booking, String newStatus) {
        bookingsRef.child(booking.getId()).child("status").setValue(newStatus)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainService.setIncomingCallListener(this);
    }

    @Override
    protected void onPause() {
        MainService.setIncomingCallListener(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        MainService.setIncomingCallListener(null);
        super.onDestroy();
    }

    private class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {
        private final List<Booking> bookings;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        public BookingHistoryAdapter(List<Booking> bookings) {
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
            boolean isDoctor = "DOCTOR".equals(userRole);
            
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
                    holder.btnCreateReport.setVisibility(View.GONE);
                    holder.btnViewReport.setVisibility(View.GONE);
                } else if ("CONFIRMED".equals(booking.getStatus())) {
                    holder.btnReschedule.setVisibility(View.VISIBLE);
                    holder.btnReschedule.setText("Complete");
                    holder.btnReschedule.setOnClickListener(v -> updateStatus(booking, "COMPLETED"));
                    holder.btnCancel.setVisibility(View.GONE);
                    holder.btnCreateReport.setVisibility(View.GONE);
                    holder.btnViewReport.setVisibility(View.GONE);
                } else if ("COMPLETED".equals(booking.getStatus())) {
                    holder.btnReschedule.setVisibility(View.GONE);
                    holder.btnCancel.setVisibility(View.GONE);
                    
                    // Show "Create Report" button only for Doctors
                    if (isDoctor) {
                        holder.btnCreateReport.setVisibility(View.VISIBLE);
                        holder.btnCreateReport.setOnClickListener(v -> {
                            Intent intent = new Intent(BookingHistoryActivity.this, CreateReportActivity.class);
                            intent.putExtra("bookingId", booking.getId());
                            intent.putExtra("memberId", booking.getUserId());
                            intent.putExtra("memberName", booking.getUserName());
                            if (booking.getBookingDate() != null) {
                                intent.putExtra("bookingDate", booking.getBookingDate().getTime());
                            }
                            startActivity(intent);
                        });
                        holder.btnViewReport.setVisibility(View.VISIBLE);
                        holder.btnViewReport.setOnClickListener(v -> {
                            Intent intent = new Intent(BookingHistoryActivity.this, ViewReportActivity.class);
                            intent.putExtra("bookingId", booking.getId());
                            startActivity(intent);
                        });
                    } else {
                        holder.btnCreateReport.setVisibility(View.GONE);
                        holder.btnViewReport.setVisibility(View.GONE);
                    }
                } else {
                    holder.btnReschedule.setVisibility(View.GONE);
                    holder.btnCancel.setVisibility(View.GONE);
                    holder.btnCreateReport.setVisibility(View.GONE);
                    holder.btnViewReport.setVisibility(View.GONE);
                }
            } else {
                // Member logic
                holder.layoutMemberInfo.setVisibility(View.GONE);
                holder.btnReschedule.setVisibility(View.GONE);
                holder.btnCreateReport.setVisibility(View.GONE);
                if ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus())) {
                    holder.btnCancel.setVisibility(View.VISIBLE);
                    holder.btnCancel.setText("Cancel");
                    holder.btnCancel.setOnClickListener(v -> updateStatus(booking, "CANCELLED"));
                    holder.btnViewReport.setVisibility(View.GONE);
                } else if ("COMPLETED".equals(booking.getStatus())) {
                    holder.btnCancel.setVisibility(View.GONE);
                    holder.btnViewReport.setVisibility(View.VISIBLE);
                    holder.btnViewReport.setOnClickListener(v -> {
                        Intent intent = new Intent(BookingHistoryActivity.this, ViewReportActivity.class);
                        intent.putExtra("bookingId", booking.getId());
                        startActivity(intent);
                    });
                } else {
                    holder.btnCancel.setVisibility(View.GONE);
                    holder.btnViewReport.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return bookings.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvProfessionalName, tvServiceType, tvDate, tvTime, tvStatus, tvHealthCondition;
            Button btnCancel, btnReschedule, btnCreateReport, btnViewReport;
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
                btnCreateReport = itemView.findViewById(R.id.btnCreateReport);
                btnViewReport = itemView.findViewById(R.id.btnViewReport);
                layoutMemberInfo = itemView.findViewById(R.id.layoutMemberInfo);
            }
        }
    }
}
