package com.example.fit_lifegym.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fit_lifegym.CallActivity;
import com.example.fit_lifegym.R;
import com.example.fit_lifegym.RateSessionActivity;
import com.example.fit_lifegym.models.Booking;
import com.example.fit_lifegym.services.MainServiceRepository;
import com.example.fit_lifegym.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private final Context context;
    private final List<Booking> bookingList;
    private String pendingProfessionalId = null;
    private String pendingProfessionalName = null;
    private boolean isPendingVideoCall = true;

    public BookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_card, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.tvProfessionalName.setText(booking.getProfessionalName());
        holder.tvStatus.setText("Status: " + booking.getStatus());

        if (booking.isConfirmed()) {
            holder.callActionsLayout.setVisibility(View.VISIBLE);
            holder.tvBookingDetails.setVisibility(View.VISIBLE);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String details = "Date: " + (booking.getDate() != null ? sdf.format(booking.getDate()) : "N/A") + " at " + booking.getTimeSlot();
            if (booking.getHealthCondition() != null && !booking.getHealthCondition().isEmpty()) {
                details += "\nNote: " + booking.getHealthCondition();
            }
            holder.tvBookingDetails.setText(details);

            holder.btnVideoCall.setOnClickListener(v -> joinLiveSession(booking.getProfessionalId(), booking.getProfessionalName(), true));
            holder.btnAudioCall.setOnClickListener(v -> joinLiveSession(booking.getProfessionalId(), booking.getProfessionalName(), false));
            
            // Hide the old single join button
            holder.btnJoinSession.setVisibility(View.GONE);
        } else {
            holder.callActionsLayout.setVisibility(View.GONE);
            holder.tvBookingDetails.setVisibility(View.GONE);
            holder.btnJoinSession.setVisibility(View.GONE);
        }

        if (booking.isCompleted()) {
            holder.reviewSection.setVisibility(View.VISIBLE);
            holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser) {
                    Intent intent = new Intent(context, RateSessionActivity.class);
                    intent.putExtra("bookingId", booking.getId());
                    context.startActivity(intent);
                }
            });
        } else {
            holder.reviewSection.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    private void joinLiveSession(String professionalId, String professionalName, boolean isVideo) {
        if (professionalId == null || professionalId.isEmpty()) {
            Toast.makeText(context, "Error: Professional ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasRequiredPermissions()) {
            pendingProfessionalId = professionalId;
            pendingProfessionalName = professionalName;
            isPendingVideoCall = isVideo;
            requestPermissions();
            return;
        }

        ensureServiceRunning();

        try {
            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra("target", professionalId);
            intent.putExtra("targetName", professionalName);
            intent.putExtra("isVideoCall", isVideo);
            intent.putExtra("isCaller", true);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Error starting call: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasRequiredPermissions() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void ensureServiceRunning() {
        try {
            SessionManager sessionManager = new SessionManager(context);
            String userId = sessionManager.getUserId();
            if (userId != null && !userId.isEmpty()) {
                MainServiceRepository serviceRepo = new MainServiceRepository(context);
                serviceRepo.startService(userId);
            }
        } catch (Exception ignored) {}
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvProfessionalName, tvStatus, tvBookingDetails;
        Button btnJoinSession, btnVideoCall, btnAudioCall;
        LinearLayout callActionsLayout;
        View reviewSection;
        RatingBar ratingBar;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProfessionalName = itemView.findViewById(R.id.tvBookingProName);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvBookingDetails = itemView.findViewById(R.id.tvBookingDetails);
            btnJoinSession = itemView.findViewById(R.id.btnJoinSession);
            btnVideoCall = itemView.findViewById(R.id.btnVideoCall);
            btnAudioCall = itemView.findViewById(R.id.btnAudioCall);
            callActionsLayout = itemView.findViewById(R.id.callActionsLayout);
            reviewSection = itemView.findViewById(R.id.reviewSection);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
