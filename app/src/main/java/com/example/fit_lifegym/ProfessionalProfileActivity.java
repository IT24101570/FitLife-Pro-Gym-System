package com.example.fit_lifegym;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
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

import java.util.Map;

public class ProfessionalProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    
    private TextInputEditText etName, etWorkingPlace, etLocations, etContact, etAccountHolderName, etAccountNumber, etSpecialization, etHourlyFee, etLicense, etDescription;
    private TextInputLayout tilName, tilWorkingPlace, tilLocations, tilContact, tilAccountHolderName, tilAccountNumber, tilSpecialization, tilHourlyFee, tilLicense, tilDescription;
    private MaterialButton btnSubmitRequest;
    private ImageView ivProfilePicture;
    
    private String userRole;
    private String userId;
    private String currentImageUrl;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    uploadImageToCloudinary(imageUri);
                }
            }
    );

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
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        etName = findViewById(R.id.etName);
        etWorkingPlace = findViewById(R.id.etWorkingPlace);
        etLocations = findViewById(R.id.etLocations);
        etContact = findViewById(R.id.etContact);
        etAccountHolderName = findViewById(R.id.etAccountHolderName);
        etAccountNumber = findViewById(R.id.etAccountNumber);
        etSpecialization = findViewById(R.id.etSpecialization);
        etHourlyFee = findViewById(R.id.etHourlyFee);
        etLicense = findViewById(R.id.etLicense);
        etDescription = findViewById(R.id.etDescription);

        tilName = findViewById(R.id.tilName);
        tilWorkingPlace = findViewById(R.id.tilWorkingPlace);
        tilLocations = findViewById(R.id.tilLocations);
        tilContact = findViewById(R.id.tilContact);
        tilAccountHolderName = findViewById(R.id.tilAccountHolderName);
        tilAccountNumber = findViewById(R.id.tilAccountNumber);
        tilSpecialization = findViewById(R.id.tilSpecialization);
        tilHourlyFee = findViewById(R.id.tilHourlyFee);
        tilLicense = findViewById(R.id.tilLicense);
        tilDescription = findViewById(R.id.tilDescription);

        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

        if ("DOCTOR".equals(userRole)) {
            tilLicense.setVisibility(View.VISIBLE);
        } else {
            tilLicense.setVisibility(View.GONE);
        }
        
        // Pre-fill name from session
        etName.setText(sessionManager.getName());

        ivProfilePicture.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void uploadImageToCloudinary(Uri uri) {
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
        MediaManager.get().upload(uri)
                .unsigned("ml_default")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        currentImageUrl = (String) resultData.get("secure_url");
                        Glide.with(ProfessionalProfileActivity.this).load(currentImageUrl).into(ivProfilePicture);
                        Toast.makeText(ProfessionalProfileActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> Toast.makeText(ProfessionalProfileActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void loadCurrentData() {
        // First, pre-fill contact from User node
        databaseReference.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String phone = snapshot.child("phone").getValue(String.class);
                            if (phone != null && !phone.isEmpty()) {
                                // Strip country code if it's there and length > 10
                                if (phone.startsWith("+") && phone.length() > 10) {
                                    phone = phone.substring(phone.length() - 10);
                                }
                                etContact.setText(phone);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });

        // Then, load existing professional profile data if it exists
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
                                
                                String bank = p.getBankDetails();
                                if (bank != null && bank.contains(" | ")) {
                                    String[] parts = bank.split(" \\| ", 2);
                                    etAccountHolderName.setText(parts[0]);
                                    etAccountNumber.setText(parts[1]);
                                } else if (bank != null) {
                                    etAccountNumber.setText(bank);
                                }
                                
                                etSpecialization.setText(p.getSpecialization());
                                etHourlyFee.setText(String.valueOf(p.getHourlyFee()));
                                etDescription.setText(p.getDescription());
                                if (p.getLicenseNumber() != null) {
                                    etLicense.setText(p.getLicenseNumber());
                                }
                                currentImageUrl = p.getImageUrl();
                                if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                                    Glide.with(ProfessionalProfileActivity.this).load(currentImageUrl).into(ivProfilePicture);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void submitProfile() {
        // Clear all previous errors
        clearErrors();

        String name = etName.getText().toString().trim();
        String workingPlace = etWorkingPlace.getText().toString().trim();
        String locations = etLocations.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String holderName = etAccountHolderName.getText().toString().trim();
        String accountNumber = etAccountNumber.getText().toString().trim();
        String specialization = etSpecialization.getText().toString().trim();
        String feeStr = etHourlyFee.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String license = etLicense.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            tilName.setError(getString(R.string.error_name_required));
            isValid = false;
        }
        if (workingPlace.isEmpty()) {
            tilWorkingPlace.setError(getString(R.string.error_operations_required));
            isValid = false;
        }
        if (locations.isEmpty()) {
            tilLocations.setError(getString(R.string.error_jurisdictions_required));
            isValid = false;
        }
        
        if (contact.isEmpty()) {
            tilContact.setError(getString(R.string.error_contact_required));
            isValid = false;
        } else if (contact.length() < 10) {
            tilContact.setError(getString(R.string.error_contact_invalid));
            isValid = false;
        }
        
        if (holderName.isEmpty()) {
            tilAccountHolderName.setError(getString(R.string.error_beneficiary_required));
            isValid = false;
        }
        
        if (accountNumber.isEmpty()) {
            tilAccountNumber.setError(getString(R.string.error_account_required));
            isValid = false;
        } else if (accountNumber.length() < 8) {
            tilAccountNumber.setError(getString(R.string.error_account_invalid));
            isValid = false;
        }
        
        if (specialization.isEmpty()) {
            tilSpecialization.setError(getString(R.string.error_expertise_required));
            isValid = false;
        }
        
        double fee = 0;
        if (feeStr.isEmpty()) {
            tilHourlyFee.setError(getString(R.string.error_fee_required));
            isValid = false;
        } else {
            try {
                fee = Double.parseDouble(feeStr);
                if (fee <= 0) {
                    tilHourlyFee.setError(getString(R.string.error_fee_invalid));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilHourlyFee.setError(getString(R.string.msg_invalid_numbers));
                isValid = false;
            }
        }
        
        if (description.isEmpty()) {
            tilDescription.setError(getString(R.string.error_dossier_required));
            isValid = false;
        } else if (description.length() < 20) {
            tilDescription.setError(getString(R.string.error_dossier_short));
            isValid = false;
        }

        if ("DOCTOR".equals(userRole) && license.isEmpty()) {
            tilLicense.setError(getString(R.string.error_credentials_required));
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(this, getString(R.string.msg_validation_failed), Toast.LENGTH_SHORT).show();
            return;
        }

        String bankDetails = holderName + " | " + accountNumber;

        Professional professional = new Professional(userId, name, userRole, specialization);
        professional.setImageUrl(currentImageUrl);
        professional.setWorkingPlace(workingPlace);
        professional.setAvailableLocations(locations);
        professional.setContactNumber(contact);
        professional.setBankDetails(bankDetails);
        professional.setHourlyFee(fee);
        professional.setDescription(description);
        professional.setApprovalStatus("PENDING");
        professional.setEmail(sessionManager.getEmail());
        
        if ("DOCTOR".equals(userRole)) {
            professional.setLicenseNumber(license);
        }

        databaseReference.child("professionals").child(userId).setValue(professional)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfessionalProfileActivity.this, getString(R.string.msg_request_submitted), Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfessionalProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearErrors() {
        tilName.setError(null);
        tilWorkingPlace.setError(null);
        tilLocations.setError(null);
        tilContact.setError(null);
        tilAccountHolderName.setError(null);
        tilAccountNumber.setError(null);
        tilSpecialization.setError(null);
        tilHourlyFee.setError(null);
        tilLicense.setError(null);
        tilDescription.setError(null);
    }

}
