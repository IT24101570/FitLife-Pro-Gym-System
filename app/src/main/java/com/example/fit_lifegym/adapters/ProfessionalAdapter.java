package com.example.fit_lifegym.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.R;
import com.example.fit_lifegym.models.Professional;
import com.example.fit_lifegym.utils.UserStatus;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfessionalAdapter extends RecyclerView.Adapter<ProfessionalAdapter.ProfessionalViewHolder> {

    private List<Professional> professionals;
    private OnProfessionalClickListener listener;
    private int selectedPosition = -1;
    private Map<String, String> userStatuses = new HashMap<>();
    private boolean showCallButtons = true;

    public interface OnProfessionalClickListener {
        void onProfessionalClick(Professional professional, int position);
        default void onVideoCallClick(Professional professional) {}
        default void onAudioCallClick(Professional professional) {}
    }

    public ProfessionalAdapter(List<Professional> professionals, OnProfessionalClickListener listener) {
        this.professionals = professionals;
        this.listener = listener;
    }

    public ProfessionalAdapter(List<Professional> professionals, boolean showCallButtons, OnProfessionalClickListener listener) {
        this.professionals = professionals;
        this.showCallButtons = showCallButtons;
        this.listener = listener;
    }

    public void updateStatuses(Map<String, String> statuses) {
        this.userStatuses = statuses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfessionalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_professional, parent, false);
        return new ProfessionalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfessionalViewHolder holder, int position) {
        Professional professional = professionals.get(position);
        
        holder.tvName.setText(professional.getName());
        holder.tvSpecialization.setText(professional.getSpecialization());
        holder.tvRating.setText("⭐ " + professional.getRating());
        
        // Use hourlyFee since we set it in loadProfessionals
        holder.tvPrice.setText("$" + (int) professional.getHourlyFee());

        // Status handling
        String status = userStatuses.getOrDefault(professional.getId(), UserStatus.OFFLINE.name());
        holder.tvStatus.setText(status);
        
        if (UserStatus.ONLINE.name().equals(status)) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.success));
        } else if (UserStatus.IN_CALL.name().equals(status)) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.warning));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
        }

        // Highlight selected item
        if (selectedPosition == position) {
            holder.cardProfessional.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.surface_variant));
            holder.cardProfessional.setStrokeWidth(4);
            holder.cardProfessional.setStrokeColor(holder.itemView.getContext().getResources().getColor(R.color.accent));
        } else {
            holder.cardProfessional.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.surface));
            holder.cardProfessional.setStrokeWidth(0);
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            listener.onProfessionalClick(professional, selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return professionals.size();
    }

    static class ProfessionalViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardProfessional;
        TextView tvName, tvSpecialization, tvRating, tvPrice, tvStatus;

        ProfessionalViewHolder(View itemView) {
            super(itemView);
            cardProfessional = itemView.findViewById(R.id.cardProfessional);
            tvName = itemView.findViewById(R.id.tvProfessionalName);
            tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
