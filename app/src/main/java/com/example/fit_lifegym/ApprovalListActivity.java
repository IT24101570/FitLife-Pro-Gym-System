package com.example.fit_lifegym;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.models.Professional;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ApprovalListActivity extends AppCompatActivity {

    private static final String TAG = "ApprovalListActivity";
    private RecyclerView rvApprovalList;
    private TextView tvNoRequests, tvTitle;
    private String type;
    private DatabaseReference dbRef;
    private List<Professional> professionalList;
    private ApprovalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval_list);

        type = getIntent().getStringExtra("type");
        dbRef = FirebaseDatabase.getInstance().getReference("professionals");
        
        rvApprovalList = findViewById(R.id.rvApprovalList);
        tvNoRequests = findViewById(R.id.tvNoRequests);
        tvTitle = findViewById(R.id.tvTitle);
        
        if (tvTitle != null) {
            tvTitle.setText(type + " Approvals");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        professionalList = new ArrayList<>();
        adapter = new ApprovalAdapter(professionalList);
        rvApprovalList.setLayoutManager(new LinearLayoutManager(this));
        rvApprovalList.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        // Fetch all professionals and filter in code to avoid indexing issues and handle potential type mismatches
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                professionalList.clear();
                Log.d(TAG, "Loading requests for type: " + type);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Professional p = ds.getValue(Professional.class);
                    if (p != null) {
                        Log.d(TAG, "Found professional: " + p.getName() + " type: " + p.getType() + " status: " + p.getApprovalStatus());
                        if (type.equalsIgnoreCase(p.getType()) && "PENDING".equals(p.getApprovalStatus())) {
                            p.setId(ds.getKey()); // Ensure ID is set from the node key
                            professionalList.add(p);
                        }
                    }
                }
                
                if (professionalList.isEmpty()) {
                    tvNoRequests.setVisibility(View.VISIBLE);
                    tvNoRequests.setText("No pending " + type + " requests found.");
                } else {
                    tvNoRequests.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ApprovalListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class ApprovalAdapter extends RecyclerView.Adapter<ApprovalAdapter.ViewHolder> {
        private List<Professional> list;

        public ApprovalAdapter(List<Professional> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_approval_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Professional p = list.get(position);
            holder.tvName.setText(p.getName());
            holder.tvRole.setText(p.getType());
            holder.tvSpecialization.setText(p.getSpecialization());
            
            StringBuilder details = new StringBuilder();
            details.append("Working Place: ").append(p.getWorkingPlace()).append("\n");
            details.append("Contact: ").append(p.getContactNumber()).append("\n");
            details.append("Fee: $").append(p.getHourlyFee());
            if ("DOCTOR".equals(p.getType())) {
                details.append("\nLicense: ").append(p.getLicenseNumber());
            }
            holder.tvDetails.setText(details.toString());

            holder.btnApprove.setOnClickListener(v -> updateStatus(p.getId(), "APPROVED"));
            holder.btnReject.setOnClickListener(v -> updateStatus(p.getId(), "REJECTED"));
        }

        private void updateStatus(String id, String status) {
            dbRef.child(id).child("approvalStatus").setValue(status)
                    .addOnSuccessListener(aVoid -> Toast.makeText(ApprovalListActivity.this, "Professional has been " + status, Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRole, tvSpecialization, tvDetails;
            Button btnApprove, btnReject;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvRole = itemView.findViewById(R.id.tvRole);
                tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
                tvDetails = itemView.findViewById(R.id.tvDetails);
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }
}
