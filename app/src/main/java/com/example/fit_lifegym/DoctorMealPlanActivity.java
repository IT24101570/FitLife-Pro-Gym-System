package com.example.fit_lifegym;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.models.Booking;
import com.example.fit_lifegym.models.MealPlan;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DoctorMealPlanActivity extends AppCompatActivity {

    private RecyclerView rvMembers;
    private View tvEmpty;
    private ImageView btnBack;
    private MemberAdapter adapter;
    private List<Booking> confirmedBookings;
    private DatabaseReference bookingsRef, assignedMealPlansRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_meal_plan);

        sessionManager = new SessionManager(this);
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        assignedMealPlansRef = FirebaseDatabase.getInstance().getReference("assigned_meal_plans");
        confirmedBookings = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadConfirmedMembers();
    }

    private void initViews() {
        rvMembers = findViewById(R.id.rvMembers);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new MemberAdapter(confirmedBookings);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(adapter);
    }

    private void loadConfirmedMembers() {
        String doctorId = sessionManager.getUserId();
        bookingsRef.orderByChild("professionalId").equalTo(doctorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        confirmedBookings.clear();
                        Set<String> uniqueUserIds = new HashSet<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Booking b = ds.getValue(Booking.class);
                            if (b != null && "CONFIRMED".equals(b.getStatus())) {
                                if (!uniqueUserIds.contains(b.getUserId())) {
                                    confirmedBookings.add(b);
                                    uniqueUserIds.add(b.getUserId());
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(confirmedBookings.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void showMealPlansDialog(Booking booking) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_meal_plans, null);
        RecyclerView rvDialogMealPlans = dialogView.findViewById(R.id.rvMealPlans);
        View tvDialogEmpty = dialogView.findViewById(R.id.tvEmpty);
        ImageView btnDialogBack = dialogView.findViewById(R.id.btnBack);
        btnDialogBack.setVisibility(View.GONE); 
        
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        if (tvTitle != null) tvTitle.setText("Plans for " + booking.getUserName());

        List<MealPlan> assignedPlans = new ArrayList<>();
        MealPlanDialogAdapter dialogAdapter = new MealPlanDialogAdapter(assignedPlans, booking);
        rvDialogMealPlans.setLayoutManager(new LinearLayoutManager(this));
        rvDialogMealPlans.setAdapter(dialogAdapter);

        // Load plans assigned to THIS specific member by THIS doctor with real-time updates
        ValueEventListener plansListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                assignedPlans.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MealPlan plan = ds.getValue(MealPlan.class);
                    if (plan != null) {
                        plan.setId(ds.getKey());
                        assignedPlans.add(plan);
                    }
                }
                dialogAdapter.notifyDataSetChanged();
                tvDialogEmpty.setVisibility(assignedPlans.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        assignedMealPlansRef.child(booking.getUserId()).orderByChild("creatorId").equalTo(sessionManager.getUserId())
                .addValueEventListener(plansListener);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Assigned Meal Plans")
                .setView(dialogView)
                .setPositiveButton("Assign New", (d, which) -> showCreateMealPlanDialog(booking))
                .setNegativeButton("Close", (d, which) -> {
                    assignedMealPlansRef.child(booking.getUserId()).removeEventListener(plansListener);
                })
                .setOnDismissListener(d -> assignedMealPlansRef.child(booking.getUserId()).removeEventListener(plansListener))
                .create();
        dialog.show();
    }

    private void showCreateMealPlanDialog(Booking booking) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_meal_plan, null);
        TextInputEditText etPlanName = dialogView.findViewById(R.id.etPlanName);
        TextInputEditText etGoal = dialogView.findViewById(R.id.etGoal);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etTotalCalories = dialogView.findViewById(R.id.etTotalCalories);
        TextInputEditText etProtein = dialogView.findViewById(R.id.etProtein);
        TextInputEditText etCarbs = dialogView.findViewById(R.id.etCarbs);
        TextInputEditText etFats = dialogView.findViewById(R.id.etFats);

        new AlertDialog.Builder(this)
                .setTitle("New Plan for " + booking.getUserName())
                .setView(dialogView)
                .setPositiveButton("Assign", (dialog, which) -> {
                    String name = etPlanName.getText().toString().trim();
                    String goal = etGoal.getText().toString().trim();
                    String desc = etDescription.getText().toString().trim();
                    String calStr = etTotalCalories.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(calStr)) {
                        Toast.makeText(this, "Plan name and calories required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MealPlan plan = new MealPlan();
                    plan.setId(UUID.randomUUID().toString());
                    plan.setName(name);
                    plan.setGoal(goal);
                    plan.setDescription(desc);
                    try {
                        plan.setTotalCalories(Integer.parseInt(calStr));
                        plan.setProtein(etProtein.getText().toString().isEmpty() ? 0 : Double.parseDouble(etProtein.getText().toString()));
                        plan.setCarbs(etCarbs.getText().toString().isEmpty() ? 0 : Double.parseDouble(etCarbs.getText().toString()));
                        plan.setFats(etFats.getText().toString().isEmpty() ? 0 : Double.parseDouble(etFats.getText().toString()));
                    } catch (Exception e) { 
                        Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
                        return; 
                    }
                    
                    plan.setCreatorId(sessionManager.getUserId());
                    plan.setCreatorName(sessionManager.getName());

                    assignedMealPlansRef.child(booking.getUserId()).child(plan.getId()).setValue(plan)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Plan Assigned Successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to assign plan", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
        private final List<Booking> members;
        public MemberAdapter(List<Booking> members) { this.members = members; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_confirmed_member, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking b = members.get(position);
            holder.tvName.setText(b.getUserName());
            holder.btnCreatePlan.setOnClickListener(v -> showCreateMealPlanDialog(b));
            holder.btnManagePlans.setOnClickListener(v -> showMealPlansDialog(b));
        }

        @Override
        public int getItemCount() { return members.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName; MaterialButton btnCreatePlan, btnManagePlans;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvMemberName);
                btnCreatePlan = itemView.findViewById(R.id.btnCreatePlan);
                btnManagePlans = itemView.findViewById(R.id.btnManagePlans);
            }
        }
    }

    private void showEditMealPlanDialog(Booking booking, MealPlan plan) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_meal_plan, null);
        TextInputEditText etPlanName = dialogView.findViewById(R.id.etPlanName);
        TextInputEditText etGoal = dialogView.findViewById(R.id.etGoal);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etTotalCalories = dialogView.findViewById(R.id.etTotalCalories);
        TextInputEditText etProtein = dialogView.findViewById(R.id.etProtein);
        TextInputEditText etCarbs = dialogView.findViewById(R.id.etCarbs);
        TextInputEditText etFats = dialogView.findViewById(R.id.etFats);

        etPlanName.setText(plan.getName());
        etGoal.setText(plan.getGoal());
        etDescription.setText(plan.getDescription());
        etTotalCalories.setText(String.valueOf(plan.getTotalCalories()));
        etProtein.setText(String.valueOf(plan.getProtein()));
        etCarbs.setText(String.valueOf(plan.getCarbs()));
        etFats.setText(String.valueOf(plan.getFats()));

        new AlertDialog.Builder(this)
                .setTitle("Edit Plan for " + booking.getUserName())
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = etPlanName.getText().toString().trim();
                    String goal = etGoal.getText().toString().trim();
                    String desc = etDescription.getText().toString().trim();
                    String calStr = etTotalCalories.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(calStr)) {
                        Toast.makeText(this, "Plan name and calories required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    plan.setName(name);
                    plan.setGoal(goal);
                    plan.setDescription(desc);
                    try {
                        plan.setTotalCalories(Integer.parseInt(calStr));
                        plan.setProtein(etProtein.getText().toString().isEmpty() ? 0 : Double.parseDouble(etProtein.getText().toString()));
                        plan.setCarbs(etCarbs.getText().toString().isEmpty() ? 0 : Double.parseDouble(etCarbs.getText().toString()));
                        plan.setFats(etFats.getText().toString().isEmpty() ? 0 : Double.parseDouble(etFats.getText().toString()));
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    assignedMealPlansRef.child(booking.getUserId()).child(plan.getId()).setValue(plan)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Plan Updated Successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update plan", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMealPlan(Booking booking, MealPlan plan) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Plan")
                .setMessage("Are you sure you want to delete this meal plan?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    assignedMealPlansRef.child(booking.getUserId()).child(plan.getId()).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Plan Deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete plan", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class MealPlanDialogAdapter extends RecyclerView.Adapter<MealPlanDialogAdapter.ViewHolder> {
        private final List<MealPlan> plans;
        private final Booking booking;
        public MealPlanDialogAdapter(List<MealPlan> plans, Booking booking) { 
            this.plans = plans; 
            this.booking = booking;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_plan, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MealPlan plan = plans.get(position);
            holder.tvName.setText(plan.getName());
            holder.tvGoal.setText(plan.getGoal().toUpperCase());
            holder.tvCalories.setText(plan.getTotalCalories() + " kcal");
            holder.tvDescription.setText(plan.getDescription());
            holder.tvProtein.setText((int)plan.getProtein() + "g");
            holder.tvCarbs.setText((int)plan.getCarbs() + "g");
            holder.tvFats.setText((int)plan.getFats() + "g");
            
            holder.btnActivate.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnEdit.setOnClickListener(v -> showEditMealPlanDialog(booking, plan));
            holder.btnDelete.setOnClickListener(v -> deleteMealPlan(booking, plan));
        }

        @Override
        public int getItemCount() { return plans.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvGoal, tvCalories, tvDescription, tvProtein, tvCarbs, tvFats; 
            View btnActivate, btnEdit, btnDelete;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvPlanName);
                tvGoal = itemView.findViewById(R.id.tvGoal);
                tvCalories = itemView.findViewById(R.id.tvCalories);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvProtein = itemView.findViewById(R.id.tvProtein);
                tvCarbs = itemView.findViewById(R.id.tvCarbs);
                tvFats = itemView.findViewById(R.id.tvFats);
                btnActivate = itemView.findViewById(R.id.btnActivate);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}
