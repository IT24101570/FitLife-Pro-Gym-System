package com.example.fit_lifegym.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.R;
import com.example.fit_lifegym.models.Exercise;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private Context context;
    private List<Exercise.ExerciseLog> exercises;
    private OnExerciseActionListener listener;

    public interface OnExerciseActionListener {
        void onEdit(int position);
        void onDelete(int position);
    }

    public ExerciseAdapter(Context context, List<Exercise.ExerciseLog> exercises, OnExerciseActionListener listener) {
        this.context = context;
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise.ExerciseLog exercise = exercises.get(position);
        
        holder.tvExerciseName.setText(exercise.getExerciseName());
        
        String details = exercise.getSets() + " sets × " + exercise.getReps() + " reps";
        if (exercise.getWeight() > 0) {
            details += " • " + (int) exercise.getWeight() + " kg";
        }
        holder.tvExerciseDetails.setText(details);
        
        // Set muscle group icon based on exercise name (simplified)
        String muscleGroup = getMuscleGroupEmoji(exercise.getExerciseName());
        holder.tvMuscleGroup.setText(muscleGroup);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(holder.getAdapterPosition());
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    private String getMuscleGroupEmoji(String exerciseName) {
        String name = exerciseName.toLowerCase();
        if (name.contains("bench") || name.contains("chest") || name.contains("push")) {
            return "💪 Chest";
        } else if (name.contains("squat") || name.contains("leg") || name.contains("lunge")) {
            return "🦵 Legs";
        } else if (name.contains("pull") || name.contains("row") || name.contains("back")) {
            return "� Back";
        } else if (name.contains("curl") || name.contains("tricep") || name.contains("arm")) {
            return "💪 Arms";
        } else if (name.contains("shoulder") || name.contains("press")) {
            return "�️ Shoulders";
        } else if (name.contains("plank") || name.contains("crunch") || name.contains("abs")) {
            return "🎯 Core";
        } else {
            return "💪 Full Body";
        }
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardExercise;
        TextView tvExerciseName, tvExerciseDetails, tvMuscleGroup;
        ImageButton btnEdit, btnDelete;

        ExerciseViewHolder(View itemView) {
            super(itemView);
            cardExercise = itemView.findViewById(R.id.cardExercise);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseDetails = itemView.findViewById(R.id.tvExerciseDetails);
            tvMuscleGroup = itemView.findViewById(R.id.tvMuscleGroup);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
