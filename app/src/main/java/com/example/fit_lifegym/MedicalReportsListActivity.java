package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fit_lifegym.models.MedicalReport;
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

public class MedicalReportsListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmpty;
    private MedicalReportAdapter adapter;
    private List<MedicalReport> reportList;
    
    private DatabaseReference reportsRef;
    private SessionManager sessionManager;
    private String userId, userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_reports_list);

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        userRole = sessionManager.getRole();
        reportsRef = FirebaseDatabase.getInstance().getReference("medical_reports");
        reportList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        
        loadReports();

        swipeRefresh.setOnRefreshListener(this::loadReports);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new MedicalReportAdapter(reportList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadReports() {
        swipeRefresh.setRefreshing(true);
        
        // Filter by doctorId or memberId depending on role
        String filterField = "DOCTOR".equals(userRole) ? "doctorId" : "memberId";
        
        reportsRef.orderByChild(filterField).equalTo(userId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    reportList.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        MedicalReport report = data.getValue(MedicalReport.class);
                        if (report != null) {
                            reportList.add(report);
                        }
                    }
                    Collections.reverse(reportList);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(reportList.isEmpty() ? View.VISIBLE : View.GONE);
                    swipeRefresh.setRefreshing(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(MedicalReportsListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private class MedicalReportAdapter extends RecyclerView.Adapter<MedicalReportAdapter.ViewHolder> {
        private final List<MedicalReport> reports;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        public MedicalReportAdapter(List<MedicalReport> reports) {
            this.reports = reports;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medical_report, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MedicalReport report = reports.get(position);
            
            holder.tvTitle.setText("DOCTOR".equals(userRole) ? "Patient: " + report.getMemberName() : "Doctor: " + report.getDoctorName());
            holder.tvDiagnosis.setText("Diagnosis: " + report.getDiagnosis());
            if (report.getReportDate() != null) {
                holder.tvDate.setText(dateFormat.format(report.getReportDate()));
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MedicalReportsListActivity.this, ViewReportActivity.class);
                intent.putExtra("bookingId", report.getBookingId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return reports.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDiagnosis, tvDate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDiagnosis = itemView.findViewById(R.id.tvDiagnosis);
                tvDate = itemView.findViewById(R.id.tvDate);
            }
        }
    }
}
