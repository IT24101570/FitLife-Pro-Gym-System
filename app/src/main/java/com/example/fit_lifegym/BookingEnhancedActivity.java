package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.adapters.ProfessionalAdapter;
import com.example.fit_lifegym.adapters.TimeSlotAdapter;
import com.example.fit_lifegym.models.Booking;
import com.example.fit_lifegym.models.Professional;
import com.example.fit_lifegym.services.FirebaseClient;
import com.example.fit_lifegym.services.MainRepository;
import com.example.fit_lifegym.utils.FirebaseHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.applandeo.materialcalendarview.CalendarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BookingEnhancedActivity extends AppCompatActivity {

    private RecyclerView rvProfessionals, rvTimeSlots;
    private CalendarView calendarView;
    private MaterialCardView cardSummary;
    private TextView tvSummary, tvNoSlots;
    private EditText etHealthCondition, etSearchPro;
    private ChipGroup cgFilters;
    private MaterialButton btnConfirmBooking;
    private ImageView btnBack;

    private ProfessionalAdapter professionalAdapter;
    private TimeSlotAdapter timeSlotAdapter;
    private SessionManager sessionManager;

    private Professional selectedProfessional;
    private Calendar selectedDate;
    private String selectedTimeSlot;
    private String bookingType; // DOCTOR or TRAINER

    private final List<Professional> allProfessionals = new ArrayList<>();
    private final List<Professional> filteredProfessionals = new ArrayList<>();
    private final List<String> availableTimeSlots = new ArrayList<>();
    private DatabaseReference professionalsRef;

    @Inject MainRepository mainRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_enhanced);

        sessionManager = new SessionManager(this);
        professionalsRef = FirebaseHelper.getProfessionalsRef();
        bookingType = getIntent().getStringExtra("type");
        if (bookingType == null) bookingType = "TRAINER";

        initializeViews();
        setupCalendar();
        setupListeners();
        loadProfessionalsFromFirebase();
        
        observeStatuses();
    }

    private void observeStatuses() {
        mainRepository.observeUsersStatus(users -> {
            Map<String, String> statusMap = new HashMap<>();
            for (FirebaseClient.UserStatusModel user : users) {
                statusMap.put(user.username, user.status);
            }
            runOnUiThread(() -> {
                if (professionalAdapter != null) {
                    professionalAdapter.updateStatuses(statusMap);
                }
            });
        });
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvProfessionals = findViewById(R.id.rvProfessionals);
        calendarView = findViewById(R.id.calendarView);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        tvNoSlots = findViewById(R.id.tvNoSlots);
        etHealthCondition = findViewById(R.id.etHealthCondition);
        etSearchPro = findViewById(R.id.etSearchPro);
        cgFilters = findViewById(R.id.cgFilters);
        cardSummary = findViewById(R.id.cardSummary);
        tvSummary = findViewById(R.id.tvSummary);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        rvProfessionals.setLayoutManager(new LinearLayoutManager(this));
        rvTimeSlots.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        
        ((TextView)findViewById(R.id.tvTitle)).setText("Book " + (bookingType.equals("DOCTOR") ? "Consultation" : "Training"));
        
        professionalAdapter = new ProfessionalAdapter(filteredProfessionals, new ProfessionalAdapter.OnProfessionalClickListener() {
            @Override
            public void onProfessionalClick(Professional professional, int position) {
                selectedProfessional = professional;
                updateBookingSummary();
            }

            @Override
            public void onVideoCallClick(Professional professional) {
                startCall(professional, true);
            }

            @Override
            public void onAudioCallClick(Professional professional) {
                startCall(professional, false);
            }
        });
        rvProfessionals.setAdapter(professionalAdapter);
    }

    private void startCall(Professional p, boolean isVideo) {
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("target", p.getId());
        intent.putExtra("targetName", p.getName());
        intent.putExtra("isVideoCall", isVideo);
        intent.putExtra("isCaller", true);
        startActivity(intent);
    }

    private void setupCalendar() {
        Calendar minDate = Calendar.getInstance();
        calendarView.setMinimumDate(minDate);
        
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 30);
        calendarView.setMaximumDate(maxDate);

        calendarView.setOnDayClickListener(eventDay -> {
            selectedDate = eventDay.getCalendar();
            loadAvailableTimeSlots();
            updateBookingSummary();
        });
    }

    private void loadProfessionalsFromFirebase() {
        professionalsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProfessionals.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Professional p = ds.getValue(Professional.class);
                    if (p != null) {
                        p.setId(ds.getKey());
                        if (bookingType.equalsIgnoreCase(p.getType()) && "APPROVED".equalsIgnoreCase(p.getApprovalStatus())) {
                            allProfessionals.add(p);
                        }
                    }
                }
                filterProfessionals();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterProfessionals() {
        String query = etSearchPro.getText().toString().toLowerCase().trim();
        filteredProfessionals.clear();

        for (Professional p : allProfessionals) {
            boolean matchesSearch = p.getName().toLowerCase().contains(query) || 
                                   (p.getSpecialization() != null && p.getSpecialization().toLowerCase().contains(query));
            if (matchesSearch) {
                filteredProfessionals.add(p);
            }
        }

        int checkedId = cgFilters.getCheckedChipId();
        if (checkedId == R.id.chipPrice) {
            Collections.sort(filteredProfessionals, (p1, p2) -> Double.compare(p1.getHourlyFee(), p2.getHourlyFee()));
        } else if (checkedId == R.id.chipRating) {
            Collections.sort(filteredProfessionals, (p1, p2) -> Double.compare(p2.getRating(), p1.getRating()));
        }

        professionalAdapter.notifyDataSetChanged();
    }

    private void loadAvailableTimeSlots() {
        availableTimeSlots.clear();
        String[] slots = {"09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "04:00 PM", "05:00 PM"};
        Collections.addAll(availableTimeSlots, slots);

        timeSlotAdapter = new TimeSlotAdapter(availableTimeSlots, timeSlot -> {
            selectedTimeSlot = timeSlot;
            updateBookingSummary();
        });
        rvTimeSlots.setAdapter(timeSlotAdapter);
        rvTimeSlots.setVisibility(View.VISIBLE);
        tvNoSlots.setVisibility(View.GONE);
    }

    private void updateBookingSummary() {
        if (selectedProfessional != null && selectedDate != null && selectedTimeSlot != null) {
            cardSummary.setVisibility(View.VISIBLE);
            btnConfirmBooking.setEnabled(true);

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
            String summary = "Professional: " + selectedProfessional.getName() + "\n" +
                    "Date: " + dateFormat.format(selectedDate.getTime()) + "\n" +
                    "Time: " + selectedTimeSlot + "\n" +
                    "Fee: $" + selectedProfessional.getHourlyFee();
            tvSummary.setText(summary);
        } else {
            cardSummary.setVisibility(View.GONE);
            btnConfirmBooking.setEnabled(false);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());

        etSearchPro.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterProfessionals(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        cgFilters.setOnCheckedStateChangeListener((group, checkedIds) -> filterProfessionals());
    }

    private void confirmBooking() {
        String healthCondition = etHealthCondition.getText().toString().trim();
        
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID().toString());
        booking.setUserId(sessionManager.getUserId());
        booking.setUserName(sessionManager.getName());
        booking.setProfessionalId(selectedProfessional.getId());
        booking.setProfessionalName(selectedProfessional.getName());
        booking.setServiceType(selectedProfessional.getSpecialization());
        if (selectedDate != null) {
            booking.setDate(selectedDate.getTime());
        }
        booking.setTimeSlot(selectedTimeSlot);
        booking.setHealthCondition(healthCondition);
        booking.setPrice(selectedProfessional.getHourlyFee());
        booking.setStatus("PENDING");

        FirebaseHelper.getBookingsRef().child(booking.getId()).setValue(booking)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Booking request sent!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to book: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
