package com.example.fit_lifegym;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.models.Professional;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfessionalProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    
    private TextInputEditText etName, etWorkingPlace, etLocations, etContact, etBankDetails, etSpecialization, etHourlyFee, etLicense, etDescription;
    private TextInputLayout tilLicense;
    private MaterialButton btnSubmitRequest;
    
    private String userRole;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_profile);

        sessionManager = new SessionManager(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        
        userId = sessionManager.getUserId();
        userRole = sessionManager.getRole();

        initViews();
        loadCurrentData();
        
        btnSubmitRequest.setOnClickListener(v -> submitProfile());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etWorkingPlace = findViewById(R.id.etWorkingPlace);
        etLocations = findViewById(R.id.etLocations);
        etContact = findViewById(R.id.etContact);
        etBankDetails = findViewById(R.id.etBankDetails);
        etSpecialization = findViewById(R.id.etSpecialization);
        etHourlyFee = findViewById(R.id.etHourlyFee);
        etLicense = findViewById(R.id.etLicense);
        etDescription = findViewById(R.id.etDescription);
        tilLicense = findViewById(R.id.tilLicense);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

        if ("DOCTOR".equals(userRole)) {
            tilLicense.setVisibility(View.VISIBLE);
        } else {
            tilLicense.setVisibility(View.GONE);
        }
        
        // Pre-fill name from session
        etName.setText(sessionManager.getName());
    }

    private void loadCurrentData() {
        databaseReference.child("professionals").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Professional p = snapshot.getValue(Professional.class);
                            if (p != null) {
                                etWorkingPlace.setText(p.getWorkingPlace());
                                etLocations.setText(p.getAvailableLocations());
                                etContact.setText(p.getContactNumber());
                                etBankDetails.setText(p.getBankDetails());
                                etSpecialization.setText(p.getSpecialization());
                                etHourlyFee.setText(String.valueOf(p.getHourlyFee()));
                                etDescription.setText(p.getDescription());
                                if (p.getLicenseNumber() != null) {
                                    etLicense.setText(p.getLicenseNumber());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void submitProfile() {
        String name = etName.getText().toString().trim();
        String workingPlace = etWorkingPlace.getText().toString().trim();
        String locations = etLocations.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String bank = etBankDetails.getText().toString().trim();
        String specialization = etSpecialization.getText().toString().trim();
        String feeStr = etHourlyFee.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String license = etLicense.getText().toString().trim();

        if (name.isEmpty() || workingPlace.isEmpty() || contact.isEmpty() || feeStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double fee;
        try {
            fee = Double.parseDouble(feeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid hourly fee", Toast.LENGTH_SHORT).show();
            return;
        }

        Professional professional = new Professional(userId, name, userRole, specialization);
        professional.setWorkingPlace(workingPlace);
        professional.setAvailableLocations(locations);
        professional.setContactNumber(contact);
        professional.setBankDetails(bank);
        professional.setHourlyFee(fee);
        professional.setDescription(description);
        professional.setApprovalStatus("PENDING");
        professional.setEmail(sessionManager.getEmail());
        
        if ("DOCTOR".equals(userRole)) {
            professional.setLicenseNumber(license);
        }

        databaseReference.child("professionals").child(userId).setValue(professional)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfessionalProfileActivity.this, "Profile request submitted to Admin!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfessionalProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
