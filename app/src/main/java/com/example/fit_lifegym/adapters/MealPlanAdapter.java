package com.example.fit_lifegym.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.R;
import com.example.fit_lifegym.models.MealPlan;

import java.util.List;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.ViewHolder> {

    private List<MealPlan> mealPlans;
    private OnMealPlanClickListener listener;

    public interface OnMealPlanClickListener {
        void onActivateClick(MealPlan plan);
    }

    public MealPlanAdapter(List<MealPlan> mealPlans, OnMealPlanClickListener listener) {
        this.mealPlans = mealPlans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealPlan plan = mealPlans.get(position);
        holder.tvPlanName.setText(plan.getName());
        holder.tvGoal.setText(plan.getGoal().toUpperCase());
        holder.tvCalories.setText(plan.getTotalCalories() + " kcal");
        holder.tvDescription.setText(plan.getDescription());
        holder.tvProtein.setText((int)plan.getProtein() + "g");
        holder.tvCarbs.setText((int)plan.getCarbs() + "g");
        holder.tvFats.setText((int)plan.getFats() + "g");

        holder.btnActivate.setOnClickListener(v -> {
            if (listener != null) listener.onActivateClick(plan);
        });
    }

    @Override
    public int getItemCount() {
        return mealPlans.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Button btnActivate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvGoal = itemView.findViewById(R.id.tvGoal);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvProtein = itemView.findViewById(R.id.tvProtein);
            tvCarbs = itemView.findViewById(R.id.tvCarbs);
            tvFats = itemView.findViewById(R.id.tvFats);
            btnActivate = itemView.findViewById(R.id.btnActivate);
        }
    }
}
