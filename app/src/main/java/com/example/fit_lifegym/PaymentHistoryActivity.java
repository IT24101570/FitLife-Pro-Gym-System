package com.example.fit_lifegym;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.models.Payment;
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

public class PaymentHistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private PaymentAdapter adapter;
    
    private SessionManager sessionManager;
    private DatabaseReference paymentsRef;
    private List<Payment> paymentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        sessionManager = new SessionManager(this);
        paymentsRef = FirebaseDatabase.getInstance().getReference("payments");
        paymentList = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        loadPayments();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new PaymentAdapter(paymentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadPayments() {
        String userId = sessionManager.getUserId();
        
        paymentsRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    paymentList.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Payment payment = data.getValue(Payment.class);
                        if (payment != null) {
                            paymentList.add(payment);
                        }
                    }
                    
                    Collections.reverse(paymentList);
                    adapter.notifyDataSetChanged();
                    
                    tvEmpty.setVisibility(paymentList.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(paymentList.isEmpty() ? View.GONE : View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Error loading payments");
                }
            });
    }

    private static class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {
        private final List<Payment> payments;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        public PaymentAdapter(List<Payment> payments) {
            this.payments = payments;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Payment payment = payments.get(position);
            
            holder.tvDescription.setText(payment.getDescription());
            holder.tvAmount.setText(payment.getFormattedAmount());
            holder.tvDate.setText(dateFormat.format(payment.getPaymentDate()));
            holder.tvStatus.setText(payment.getStatus());
            
            if (payment.getCardLast4() != null) {
                holder.tvPaymentMethod.setText("•••• " + payment.getCardLast4());
            } else {
                holder.tvPaymentMethod.setText(payment.getPaymentMethod());
            }

            int statusColor;
            if (payment.isSuccessful()) {
                statusColor = holder.itemView.getContext().getColor(R.color.success);
            } else if (payment.isFailed()) {
                statusColor = holder.itemView.getContext().getColor(R.color.error);
            } else {
                statusColor = holder.itemView.getContext().getColor(R.color.warning);
            }
            holder.tvStatus.setTextColor(statusColor);
        }

        @Override
        public int getItemCount() {
            return payments.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDescription, tvAmount, tvDate, tvStatus, tvPaymentMethod;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            }
        }
    }
}
