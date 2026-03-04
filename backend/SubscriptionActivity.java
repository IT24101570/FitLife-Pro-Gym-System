package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fit_lifegym.models.Subscription;
import com.example.fit_lifegym.utils.FirebaseHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class SubscriptionActivity extends AppCompatActivity {

    private RadioGroup rgBillingCycle;
    private RadioButton rbMonthly, rbYearly;
    private MaterialCardView cardBasic, cardPremium, cardElite;
    private RadioButton rbBasic, rbPremium, rbElite;
    private TextView tvBasicPrice, tvPremiumPrice, tvElitePrice;
    private TextView tvBasicFeatures, tvPremiumFeatures, tvEliteFeatures;
    private MaterialButton btnSubscribe;
    private ImageView btnBack;

    private String selectedPlan = Subscription.Plan.PREMIUM;
    private String selectedCycle = "MONTHLY";
    private SessionManager sessionManager;
    private DatabaseReference subscriptionsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        sessionManager = new SessionManager(this);
        subscriptionsRef = FirebaseHelper.getSubscriptionsRef();

        initializeViews();
        setupListeners();
        updatePrices();
        checkCurrentSubscription();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rgBillingCycle = findViewById(R.id.rgBillingCycle);
        rbMonthly = findViewById(R.id.rbMonthly);
        rbYearly = findViewById(R.id.rbYearly);

        cardBasic = findViewById(R.id.cardBasic);
        cardPremium = findViewById(R.id.cardPremium);
        cardElite = findViewById(R.id.cardElite);

        rbBasic = findViewById(R.id.rbBasic);
        rbPremium = findViewById(R.id.rbPremium);
        rbElite = findViewById(R.id.rbElite);

        tvBasicPrice = findViewById(R.id.tvBasicPrice);
        tvPremiumPrice = findViewById(R.id.tvPremiumPrice);
        tvElitePrice = findViewById(R.id.tvElitePrice);

        tvBasicFeatures = findViewById(R.id.tvBasicFeatures);
        tvPremiumFeatures = findViewById(R.id.tvPremiumFeatures);
        tvEliteFeatures = findViewById(R.id.tvEliteFeatures);

        btnSubscribe = findViewById(R.id.btnSubscribe);

        tvBasicFeatures.setText(Subscription.Plan.getDescription(Subscription.Plan.BASIC));
        tvPremiumFeatures.setText(Subscription.Plan.getDescription(Subscription.Plan.PREMIUM));
        tvEliteFeatures.setText(Subscription.Plan.getDescription(Subscription.Plan.ELITE));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        rgBillingCycle.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMonthly) {
                selectedCycle = "MONTHLY";
                rbMonthly.setTextColor(getResources().getColor(R.color.accent));
                rbYearly.setTextColor(getResources().getColor(R.color.text_secondary));
            } else {
                selectedCycle = "YEARLY";
                rbYearly.setTextColor(getResources().getColor(R.color.accent));
                rbMonthly.setTextColor(getResources().getColor(R.color.text_secondary));
            }
            updatePrices();
        });

        View.OnClickListener planClickListener = v -> {
            int id = v.getId();
            resetSelection();
            if (id == R.id.cardBasic || id == R.id.rbBasic) {
                selectedPlan = Subscription.Plan.BASIC;
                rbBasic.setChecked(true);
                cardBasic.setStrokeWidth(4);
            } else if (id == R.id.cardPremium || id == R.id.rbPremium) {
                selectedPlan = Subscription.Plan.PREMIUM;
                rbPremium.setChecked(true);
                cardPremium.setStrokeWidth(4);
            } else if (id == R.id.cardElite || id == R.id.rbElite) {
                selectedPlan = Subscription.Plan.ELITE;
                rbElite.setChecked(true);
                cardElite.setStrokeWidth(4);
            }
        };

        cardBasic.setOnClickListener(planClickListener);
        cardPremium.setOnClickListener(planClickListener);
        cardElite.setOnClickListener(planClickListener);
        
        rbBasic.setOnClickListener(planClickListener);
        rbPremium.setOnClickListener(planClickListener);
        rbElite.setOnClickListener(planClickListener);

        btnSubscribe.setOnClickListener(v -> proceedToPayment());
    }

    private void resetSelection() {
        cardBasic.setStrokeWidth(0);
        cardPremium.setStrokeWidth(0);
        cardElite.setStrokeWidth(0);
        rbBasic.setChecked(false);
        rbPremium.setChecked(false);
        rbElite.setChecked(false);
    }

    private void updatePrices() {
        if (selectedCycle.equals("MONTHLY")) {
            tvBasicPrice.setText("$" + Subscription.Plan.getMonthlyPrice(Subscription.Plan.BASIC));
            tvPremiumPrice.setText("$" + Subscription.Plan.getMonthlyPrice(Subscription.Plan.PREMIUM));
            tvElitePrice.setText("$" + Subscription.Plan.getMonthlyPrice(Subscription.Plan.ELITE));
        } else {
            tvBasicPrice.setText("$" + Subscription.Plan.getYearlyPrice(Subscription.Plan.BASIC));
            tvPremiumPrice.setText("$" + Subscription.Plan.getYearlyPrice(Subscription.Plan.PREMIUM));
            tvElitePrice.setText("$" + Subscription.Plan.getYearlyPrice(Subscription.Plan.ELITE));
        }
    }

    private void checkCurrentSubscription() {
        String userId = sessionManager.getUserId();
        subscriptionsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Subscription sub = data.getValue(Subscription.class);
                        if (sub != null && sub.isActive()) {
                            btnSubscribe.setText("Plan: " + sub.getPlanType());
                            break;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void proceedToPayment() {
        double price = selectedCycle.equals("MONTHLY") 
            ? Subscription.Plan.getMonthlyPrice(selectedPlan)
            : Subscription.Plan.getYearlyPrice(selectedPlan);

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("planType", selectedPlan);
        intent.putExtra("billingCycle", selectedCycle);
        intent.putExtra("price", price);
        startActivity(intent);
    }
}
