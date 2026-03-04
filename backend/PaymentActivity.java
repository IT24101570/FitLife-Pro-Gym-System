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
    private TextView tvPlanName, tvOriginalPrice, tvDiscount, tvFinalPrice;
    private EditText etCardNumber, etExpiry, etCvv, etPromoCode;
    private Button btnApplyPromo, btnPay;
    private ProgressBar progressBar;
    private View discountLayout;

    private String planType, billingCycle;
    private double originalPrice, discount = 0, finalPrice;
    private PromoCode appliedPromoCode;
    
    private SessionManager sessionManager;
    private DatabaseReference paymentsRef, subscriptionsRef, promoCodesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        sessionManager = new SessionManager(this);
        paymentsRef = FirebaseHelper.getPaymentsRef();
        subscriptionsRef = FirebaseHelper.getSubscriptionsRef();
        promoCodesRef = FirebaseHelper.getDbRef().child("promoCodes");

        getIntentData();
        initializeViews();
        setupListeners();
        updatePriceDisplay();
    }

    private void getIntentData() {
        planType = getIntent().getStringExtra("planType");
        billingCycle = getIntent().getStringExtra("billingCycle");
        originalPrice = getIntent().getDoubleExtra("price", 0);
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

        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCvv = findViewById(R.id.etCvv);
        etPromoCode = findViewById(R.id.etPromoCode);

        btnApplyPromo = findViewById(R.id.btnApplyPromo);
        btnPay = findViewById(R.id.btnPay);
        progressBar = findViewById(R.id.progressBar);

        if (tvPlanName != null) tvPlanName.setText(planType + " - " + billingCycle);
        if (discountLayout != null) discountLayout.setVisibility(View.GONE);
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
    }

    private void applyPromoCode() {
        String code = etPromoCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            Toast.makeText(this, "Enter promo code", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(PaymentActivity.this, "Code expired", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PaymentActivity.this, "Invalid code", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, "Promo applied!", Toast.LENGTH_SHORT).show();
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
        if (etCardNumber.getText().toString().replaceAll("\\s", "").length() < 16) {
            Toast.makeText(this, "Invalid card", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void savePaymentAndSubscription() {
        String userId = sessionManager.getUserId();
        
        // 1. Create Payment Record
        Payment payment = new Payment(userId, finalPrice, planType + " Subscription");
        payment.setStatus("SUCCESS");
        payment.setPaymentDate(new Date());
        String pId = paymentsRef.child(userId).push().getKey();
        paymentsRef.child(userId).child(pId).setValue(payment);

        // 2. Create/Update Subscription
        Subscription sub = new Subscription(userId, planType, finalPrice, billingCycle);
        sub.setStatus("ACTIVE");
        sub.setStartDate(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(billingCycle.equals("MONTHLY") ? Calendar.MONTH : Calendar.YEAR, 1);
        sub.setEndDate(cal.getTime());
        
        subscriptionsRef.child(userId).child("current").setValue(sub).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Payment Successful!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
