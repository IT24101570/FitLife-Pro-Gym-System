package com.example.fit_lifegym.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fit_lifegym.R;
import com.example.fit_lifegym.models.ExerciseAction;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ExerciseListAdapter extends RecyclerView.Adapter<ExerciseListAdapter.ViewHolder> {

    private Context context;
    private List<ExerciseAction> exercises;

    public ExerciseListAdapter(Context context, List<ExerciseAction> exercises) {
        this.context = context;
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseAction exercise = exercises.get(position);
        holder.tvName.setText(exercise.getName());
        holder.tvDuration.setText(String.format("%02d:%02d", exercise.getDurationSeconds() / 60, exercise.getDurationSeconds() % 60));

        try {
            // Load the first image as thumbnail
            InputStream is = context.getAssets().open("img_exercise/" + exercise.getFolderName() + "/1.webp");
            Drawable d = Drawable.createFromStream(is, null);
            holder.ivThumbnail.setImageDrawable(d);
            is.close();
        } catch (IOException e) {
            holder.ivThumbnail.setImageResource(R.drawable.first);
        }
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvName, tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivExerciseThumbnail);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvDuration = itemView.findViewById(R.id.tvExerciseDuration);
        }
    }
}
