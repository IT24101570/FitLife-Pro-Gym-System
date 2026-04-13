package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.models.Payment;
import com.example.fit_lifegym.models.PromoCode;
import com.example.fit_lifegym.models.Subscription;
import com.example.fit_lifegym.utils.FirebaseHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class PaymentActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvPlanName, tvOriginalPrice, tvDiscount, tvFinalPrice, etHourlyFee;
    private EditText etCardNumber, etExpiry, etCvv, etPromoCode;
    private Button btnApplyPromo, btnPay;
    private ProgressBar progressBar;
    private View discountLayout, hourlyFeeLayout;

    private String planType, billingCycle, bookingId;
    private double originalPrice, discount = 0, finalPrice;
    private PromoCode appliedPromoCode;
    
    private SessionManager sessionManager;
    private DatabaseReference paymentsRef, subscriptionsRef, promoCodesRef, bookingsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        sessionManager = new SessionManager(this);
        paymentsRef = FirebaseHelper.getPaymentsRef();
        subscriptionsRef = FirebaseHelper.getSubscriptionsRef();
        promoCodesRef = FirebaseHelper.getDbRef().child("promoCodes");
        bookingsRef = FirebaseHelper.getBookingsRef();

        getIntentData();
        initializeViews();
        setupListeners();
        updatePriceDisplay();
    }

    private void getIntentData() {
        planType = getIntent().getStringExtra("planType");
        billingCycle = getIntent().getStringExtra("billingCycle");
        originalPrice = getIntent().getDoubleExtra("price", 0);
        bookingId = getIntent().getStringExtra("bookingId");
        if (originalPrice == 0) {
            // Safety fallback if intent extras are missing
            originalPrice = 49.99;
            planType = "PREMIUM";
            billingCycle = "MONTHLY";
        }
        finalPrice = originalPrice;
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPlanName = findViewById(R.id.tvPlanName);
        tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvFinalPrice = findViewById(R.id.tvFinalPrice);
        discountLayout = findViewById(R.id.discountLayout);
        etHourlyFee = findViewById(R.id.etHourlyFee);
        hourlyFeeLayout = findViewById(R.id.hourlyFeeLayout);

        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCvv = findViewById(R.id.etCvv);
        etPromoCode = findViewById(R.id.etPromoCode);

        btnApplyPromo = findViewById(R.id.btnApplyPromo);
        btnPay = findViewById(R.id.btnPay);
        progressBar = findViewById(R.id.progressBar);

        if (tvPlanName != null) tvPlanName.setText(planType + " - " + billingCycle);
        if (discountLayout != null) discountLayout.setVisibility(View.GONE);

        if (bookingId != null) {
            if (hourlyFeeLayout != null) hourlyFeeLayout.setVisibility(View.VISIBLE);
            if (etHourlyFee != null) etHourlyFee.setText(String.format("$%.2f", originalPrice));
        } else {
            if (hourlyFeeLayout != null) hourlyFeeLayout.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnApplyPromo != null) btnApplyPromo.setOnClickListener(v -> applyPromoCode());
        if (btnPay != null) btnPay.setOnClickListener(v -> processPayment());

        // Card number formatter
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String text = s.toString().replaceAll("\\s", "");
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < text.length(); i++) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ");
                    formatted.append(text.charAt(i));
                }
                etCardNumber.setText(formatted.toString());
                etCardNumber.setSelection(formatted.length());
                isFormatting = false;
            }
        });

        // Expiry date formatter (MM/YY)
        etExpiry.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String text = s.toString().replaceAll("/", "");
                if (text.length() >= 2) {
                    String mm = text.substring(0, 2);
                    String yy = text.substring(2);
                    etExpiry.setText(mm + "/" + yy);
                    etExpiry.setSelection(etExpiry.getText().length());
                }
                isFormatting = false;
            }
        });
    }

    private void applyPromoCode() {
        String code = etPromoCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_enter_promo), Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        promoCodesRef.orderByChild("code").equalTo(code)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    progressBar.setVisibility(View.GONE);
                    if (snapshot.exists()) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            PromoCode promo = data.getValue(PromoCode.class);
                            if (promo != null && promo.isValid()) {
                                applyDiscount(promo);
                                return;
                            }
                        }
                        Toast.makeText(PaymentActivity.this, getString(R.string.msg_code_expired), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PaymentActivity.this, getString(R.string.msg_invalid_code), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) { progressBar.setVisibility(View.GONE); }
            });
    }

    private void applyDiscount(PromoCode promo) {
        discount = promo.calculateDiscount(originalPrice);
        finalPrice = originalPrice - discount;
        discountLayout.setVisibility(View.VISIBLE);
        updatePriceDisplay();
        Toast.makeText(this, getString(R.string.msg_promo_applied), Toast.LENGTH_SHORT).show();
    }

    private void updatePriceDisplay() {
        tvOriginalPrice.setText(String.format("$%.2f", originalPrice));
        tvDiscount.setText(String.format("-$%.2f", discount));
        tvFinalPrice.setText(String.format("$%.2f", finalPrice));
    }

    private void processPayment() {
        if (!validateInputs()) return;

        progressBar.setVisibility(View.VISIBLE);
        btnPay.setEnabled(false);

        // Simulate Stripe processing
        new android.os.Handler().postDelayed(() -> {
            savePaymentAndSubscription();
        }, 2000);
    }

    private boolean validateInputs() {
        String cardNumber = etCardNumber.getText().toString().replaceAll("\\s", "");
        String expiry = etExpiry.getText().toString().trim();
        String cvv = etCvv.getText().toString().trim();

        if (cardNumber.length() < 16) {
            etCardNumber.setError(getString(R.string.error_invalid_card));
            return false;
        }

        if (expiry.length() != 5 || !expiry.contains("/")) {
            etExpiry.setError(getString(R.string.error_invalid_expiry_format));
            return false;
        }

        try {
            String[] parts = expiry.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            
            Calendar cal = Calendar.getInstance();
            int curMonth = cal.get(Calendar.MONTH) + 1;
            int curYear = cal.get(Calendar.YEAR) % 100;

            if (month < 1 || month > 12) {
                etExpiry.setError(getString(R.string.error_invalid_month));
                return false;
            }
            if (year < curYear || (year == curYear && month < curMonth)) {
                etExpiry.setError(getString(R.string.error_card_expired));
                return false;
            }
        } catch (Exception e) {
            etExpiry.setError(getString(R.string.error_invalid_expiry));
            return false;
        }

        if (cvv.length() < 3) {
            etCvv.setError(getString(R.string.error_invalid_cvv));
            return false;
        }

        return true;
    }

    private void savePaymentAndSubscription() {
        String userId = sessionManager.getUserId();
        String cardNumber = etCardNumber.getText().toString().replaceAll("\\s", "");
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        
        // 1. Create Payment Record
        String description = (bookingId != null) ? getString(R.string.label_booking_prefix, planType) : getString(R.string.label_subscription_suffix, planType);
        Payment payment = new Payment(userId, finalPrice, description);
        payment.setStatus("SUCCESS");
        payment.setPaymentDate(new Date());
        payment.setCardLast4(last4);
        payment.setPaymentMethod("CARD");
        
        DatabaseReference userPaymentsRef = paymentsRef.child(userId);
        String pId = userPaymentsRef.push().getKey();
        payment.setId(pId);
        userPaymentsRef.child(pId).setValue(payment);

        if (bookingId != null) {
            // Update booking status to COMPLETED after payment
            bookingsRef.child(bookingId).child("status").setValue("COMPLETED").addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, getString(R.string.msg_payment_success_booking), Toast.LENGTH_LONG).show();
                finish();
            });
        } else {
            // 2. Create/Update Subscription
            Subscription sub = new Subscription(userId, planType, finalPrice, billingCycle);
            sub.setStatus("ACTIVE");
            sub.setStartDate(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(billingCycle.equals("MONTHLY") ? Calendar.MONTH : Calendar.YEAR, 1);
            sub.setEndDate(cal.getTime());
            
            subscriptionsRef.child(userId).child("current").setValue(sub).addOnSuccessListener(aVoid -> {
                // Update local session immediately
                sessionManager.setPremium(true);
                
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, getString(R.string.msg_payment_success_sub), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
