package com.example.fit_lifegym.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fit_lifegym.R;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ExerciseSubCategoryAdapter extends RecyclerView.Adapter<ExerciseSubCategoryAdapter.ViewHolder> {

    private Context context;
    private List<SubCategory> subCategories;
    private OnSubCategoryClickListener listener;

    public interface OnSubCategoryClickListener {
        void onSubCategoryClick(SubCategory subCategory);
    }

    public ExerciseSubCategoryAdapter(Context context, List<SubCategory> subCategories, OnSubCategoryClickListener listener) {
        this.context = context;
        this.subCategories = subCategories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise_sub_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubCategory subCategory = subCategories.get(position);
        holder.tvName.setText(subCategory.getName());
        holder.tvCount.setText(subCategory.getExerciseCount() + " Exercises");

        String fullPath = subCategory.getFolder() + "/" + subCategory.getImageName();
        Log.d("ExerciseAdapter", "Loading image: " + fullPath);

        try {
            InputStream is = context.getAssets().open(fullPath);
            Drawable d = Drawable.createFromStream(is, null);
            holder.ivImage.setImageDrawable(d);
            is.close();
        } catch (IOException e) {
            Log.e("ExerciseAdapter", "Error loading image: " + fullPath, e);
            holder.ivImage.setImageResource(R.drawable.first);
        }

        holder.itemView.setOnClickListener(v -> listener.onSubCategoryClick(subCategory));
    }

    @Override
    public int getItemCount() {
        return subCategories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivSubCategoryImage);
            tvName = itemView.findViewById(R.id.tvSubCategoryName);
            tvCount = itemView.findViewById(R.id.tvExerciseCount);
        }
    }

    public static class SubCategory {
        private String name;
        private String folder;
        private String imageName;
        private int exerciseCount;
        private String type;

        public SubCategory(String name, String folder, String imageName, int exerciseCount, String type) {
            this.name = name;
            this.folder = folder;
            this.imageName = imageName;
            this.exerciseCount = exerciseCount;
            this.type = type;
        }

        public String getName() { return name; }
        public String getFolder() { return folder; }
        public String getImageName() { return imageName; }
        public int getExerciseCount() { return exerciseCount; }
        public String getType() { return type; }
    }
}
