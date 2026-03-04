package com.example.fit_lifegym;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BookingActivity extends AppCompatActivity {

    private Spinner spinnerTrainers;
    private RecyclerView rvTimeSlots;
    private Button btnBookNow;
    private ImageButton btnBack;
    private TimeSlotAdapter timeSlotAdapter;
    private List<String> timeSlots;
    private String selectedTimeSlot = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        spinnerTrainers = findViewById(R.id.spinnerTrainers);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        btnBookNow = findViewById(R.id.btnBookNow);
        btnBack = findViewById(R.id.btnBack);

        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Setup trainers spinner
        String[] trainers = {
            "Select Professional",
            "John Smith - Personal Trainer",
            "Sarah Johnson - Yoga Instructor",
            "Mike Davis - Strength Coach",
            "Dr. Emily Brown - Nutritionist",
            "Dr. Robert Wilson - Physiotherapist"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, trainers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrainers.setAdapter(adapter);

        // Setup time slots
        timeSlots = new ArrayList<>();
        timeSlots.add("09:00 AM - 10:00 AM");
        timeSlots.add("10:00 AM - 11:00 AM");
        timeSlots.add("11:00 AM - 12:00 PM");
        timeSlots.add("02:00 PM - 03:00 PM");
        timeSlots.add("03:00 PM - 04:00 PM");
        timeSlots.add("04:00 PM - 05:00 PM");
        timeSlots.add("05:00 PM - 06:00 PM");
        timeSlots.add("06:00 PM - 07:00 PM");

        timeSlotAdapter = new TimeSlotAdapter(timeSlots, new TimeSlotAdapter.OnTimeSlotClickListener() {
            @Override
            public void onTimeSlotClick(String timeSlot) {
                selectedTimeSlot = timeSlot;
                Toast.makeText(BookingActivity.this, "Selected: " + timeSlot, Toast.LENGTH_SHORT).show();
            }
        });

        rvTimeSlots.setLayoutManager(new LinearLayoutManager(this));
        rvTimeSlots.setAdapter(timeSlotAdapter);

        btnBookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String trainer = spinnerTrainers.getSelectedItem().toString();
                if (trainer.equals("Select Professional")) {
                    Toast.makeText(BookingActivity.this, "Please select a professional", Toast.LENGTH_SHORT).show();
                } else if (selectedTimeSlot.isEmpty()) {
                    Toast.makeText(BookingActivity.this, "Please select a time slot", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BookingActivity.this, 
                        "Booking confirmed!\n" + trainer + "\n" + selectedTimeSlot, 
                        Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}