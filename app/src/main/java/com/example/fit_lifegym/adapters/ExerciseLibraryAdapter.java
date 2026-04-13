package com.example.fit_lifegym.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.R;
import com.example.fit_lifegym.models.ExerciseLibrary;

import java.util.ArrayList;
import java.util.List;

public class ExerciseLibraryAdapter extends RecyclerView.Adapter<ExerciseLibraryAdapter.ViewHolder> {

    private List<ExerciseLibrary> exercises;
    private OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(ExerciseLibrary exercise);
        void onFavoriteClick(ExerciseLibrary exercise);
    }

    public ExerciseLibraryAdapter(OnExerciseClickListener listener) {
        this.exercises = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_library, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseLibrary exercise = exercises.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void setExercises(List<ExerciseLibrary> exercises) {
        this.exercises = exercises;
        notifyDataSetChanged();
    }

    public void updateExercise(ExerciseLibrary exercise) {
        for (int i = 0; i < exercises.size(); i++) {
            if (exercises.get(i).getId().equals(exercise.getId())) {
                exercises.set(i, exercise);
                notifyItemChanged(i);
                break;
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName, tvMuscleGroup, tvEquipment, tvDifficulty, tvCalories, tvPremiumTag;
        ImageView ivExerciseImage;
        ImageButton btnFavorite;

        ViewHolder(View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvMuscleGroup = itemView.findViewById(R.id.tvMuscleGroup);
            tvEquipment = itemView.findViewById(R.id.tvEquipment);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            tvPremiumTag = itemView.findViewById(R.id.tvPremiumTag);
            ivExerciseImage = itemView.findViewById(R.id.ivExerciseImage);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        void bind(ExerciseLibrary exercise) {
            tvExerciseName.setText(exercise.getName());
            tvMuscleGroup.setText(exercise.getMuscleGroup());
            tvEquipment.setText(exercise.getEquipment());
            tvDifficulty.setText(exercise.getDifficulty());
            tvCalories.setText("~" + exercise.getCaloriesPerMinute() + " cal/min");

            if (tvPremiumTag != null) {
                tvPremiumTag.setVisibility(exercise.isPremium() ? View.VISIBLE : View.GONE);
            }

            // Set favorite icon
            if (exercise.isFavorite()) {
                btnFavorite.setImageResource(android.R.drawable.star_big_on);
            } else {
                btnFavorite.setImageResource(android.R.drawable.star_big_off);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(exercise);
                }
            });

            btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(exercise);
                }
            });
        }
    }
}
