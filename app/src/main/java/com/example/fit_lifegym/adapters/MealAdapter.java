package com.example.fit_lifegym.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.R;
import com.example.fit_lifegym.models.Meal;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> meals;
    private OnMealActionListener listener;

    public interface OnMealActionListener {
        void onEditMeal(Meal meal, int position);
        void onDeleteMeal(Meal meal, int position);
    }

    public MealAdapter(List<Meal> meals, OnMealActionListener listener) {
        this.meals = meals;
        this.listener = listener;
    }
    
    public void updateMeals(List<Meal> newMeals) {
        this.meals = newMeals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = meals.get(position);
        
        // Set meal type with emoji
        String mealTypeDisplay = getMealTypeEmoji(meal.getMealType()) + " " + meal.getMealType();
        holder.tvMealType.setText(mealTypeDisplay);
        
        holder.tvMealName.setText(meal.getName());
        
        // Format meal details
        String details = meal.getTotalCalories() + " cal • " +
                "P: " + (int) meal.getTotalProtein() + "g • " +
                "C: " + (int) meal.getTotalCarbs() + "g • " +
                "F: " + (int) meal.getTotalFats() + "g";
        holder.tvMealDetails.setText(details);

        holder.btnEditMeal.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditMeal(meal, holder.getAdapterPosition());
            }
        });

        holder.btnDeleteMeal.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteMeal(meal, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    private String getMealTypeEmoji(String mealType) {
        switch (mealType.toUpperCase()) {
            case "BREAKFAST":
                return "🍳";
            case "LUNCH":
                return "🍱";
            case "DINNER":
                return "🍽️";
            case "SNACK":
                return "🍎";
            default:
                return "🍴";
        }
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardMeal;
        TextView tvMealType, tvMealName, tvMealDetails;
        ImageButton btnEditMeal, btnDeleteMeal;

        MealViewHolder(View itemView) {
            super(itemView);
            cardMeal = itemView.findViewById(R.id.cardMeal);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvMealDetails = itemView.findViewById(R.id.tvMealDetails);
            btnEditMeal = itemView.findViewById(R.id.btnEditMeal);
            btnDeleteMeal = itemView.findViewById(R.id.btnDeleteMeal);
        }
    }
}
