package com.example.fit_lifegym.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.R;
import com.example.fit_lifegym.models.WorkoutSession;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(WorkoutSession session);
    }

    private Context context;
    private List<WorkoutSession> historyList;
    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public WorkoutHistoryAdapter(Context context, List<WorkoutSession> historyList, OnItemClickListener listener) {
        this.context = context;
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutSession session = historyList.get(position);
        
        holder.tvWorkoutType.setText(session.getWorkoutName() != null ? session.getWorkoutName() : session.getWorkoutType());
        if (session.getStartTime() != null) {
            holder.tvDate.setText(dateFormat.format(session.getStartTime()));
            holder.tvTime.setText(timeFormat.format(session.getStartTime()));
        }
        holder.tvCalories.setText(session.getTotalCaloriesBurned() + " kcal");
        
        if (session.getEndTime() != null && session.getStartTime() != null) {
            long durationMillis = session.getEndTime().getTime() - session.getStartTime().getTime();
            long minutes = durationMillis / (1000 * 60);
            holder.tvDuration.setText(minutes + " min");
        } else {
            holder.tvDuration.setText(session.getDurationMinutes() + " min");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(session);
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkoutType, tvDuration, tvDate, tvTime, tvCalories;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkoutType = itemView.findViewById(R.id.tvWorkoutType);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCalories = itemView.findViewById(R.id.tvCalories);
        }
    }
}
