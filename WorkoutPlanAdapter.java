package com.example.fit_lifegym.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.R;
import com.example.fit_lifegym.models.WorkoutPlan;

import java.util.List;

public class WorkoutPlanAdapter extends RecyclerView.Adapter<WorkoutPlanAdapter.ViewHolder> {

    private List<WorkoutPlan> workoutPlans;
    private OnWorkoutPlanClickListener listener;

    public interface OnWorkoutPlanClickListener {
        void onActivateClick(WorkoutPlan plan);
    }

    public WorkoutPlanAdapter(List<WorkoutPlan> workoutPlans, OnWorkoutPlanClickListener listener) {
        this.workoutPlans = workoutPlans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutPlan plan = workoutPlans.get(position);
        holder.tvPlanName.setText(plan.getName());
        holder.tvGoal.setText(plan.getGoal().toUpperCase());
        holder.tvDifficulty.setText(plan.getDifficulty());
        holder.tvDescription.setText(plan.getDescription());
        holder.tvDuration.setText(plan.getDurationWeeks() + " Weeks");
        holder.tvWorkouts.setText(plan.getWorkoutsPerWeek() + "/Week");

        holder.btnActivate.setOnClickListener(v -> {
            if (listener != null) listener.onActivateClick(plan);
        });
    }

    @Override
    public int getItemCount() {
        return workoutPlans.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlanName, tvGoal, tvDifficulty, tvDescription, tvDuration, tvWorkouts;
        Button btnActivate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvGoal = itemView.findViewById(R.id.tvGoal);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvWorkouts = itemView.findViewById(R.id.tvWorkouts);
            btnActivate = itemView.findViewById(R.id.btnActivate);
        }
    }
}
