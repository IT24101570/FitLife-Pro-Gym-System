package com.example.fit_lifegym.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.fit_lifegym.MainActivity;
import com.example.fit_lifegym.MedicalReportsListActivity;
import com.example.fit_lifegym.R;

import java.util.Calendar;

public class NotificationHelper {

    private static final String CHANNEL_ID = "fitlife_reminders";
    private static final String CHANNEL_NAME = "FitLife Reminders";
    private static final String WORKOUT_CHANNEL_ID = "workout_reminders";
    private static final String BOOKING_CHANNEL_ID = "booking_reminders";
    private static final String REPORT_CHANNEL_ID = "medical_reports";

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Workout Reminders Channel
            NotificationChannel workoutChannel = new NotificationChannel(
                    WORKOUT_CHANNEL_ID,
                    "Workout Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            workoutChannel.setDescription("Daily workout reminders");
            notificationManager.createNotificationChannel(workoutChannel);

            // Booking Reminders Channel
            NotificationChannel bookingChannel = new NotificationChannel(
                    BOOKING_CHANNEL_ID,
                    "Booking Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            bookingChannel.setDescription("Booking and appointment reminders");
            notificationManager.createNotificationChannel(bookingChannel);

            // Medical Reports Channel
            NotificationChannel reportChannel = new NotificationChannel(
                    REPORT_CHANNEL_ID,
                    "Medical Reports",
                    NotificationManager.IMPORTANCE_HIGH);
            reportChannel.setDescription("Notifications for new medical reports");
            notificationManager.createNotificationChannel(reportChannel);

            // General Reminders Channel
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            generalChannel.setDescription("General FitLife notifications");
            notificationManager.createNotificationChannel(generalChannel);
        }
    }

    public void showWorkoutReminder(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, WORKOUT_CHANNEL_ID)
                .setSmallIcon(R.drawable.fav_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(1, builder.build());
    }

    public void showBookingReminder(String title, String message, long bookingTime) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, BOOKING_CHANNEL_ID)
                .setSmallIcon(R.drawable.fav_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(2, builder.build());
    }

    public void showReportNotification(String doctorName) {
        Intent intent = new Intent(context, MedicalReportsListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, REPORT_CHANNEL_ID)
                .setSmallIcon(R.drawable.fav_icon)
                .setContentTitle(context.getString(R.string.notif_new_report_title))
                .setContentText(context.getString(R.string.notif_new_report_msg, doctorName))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(3, builder.build());
    }

    public void scheduleWorkoutReminder(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WorkoutReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent);
        }
    }

    public void scheduleBookingReminder(long bookingTime, String bookingDetails) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BookingReminderReceiver.class);
        intent.putExtra("booking_details", bookingDetails);
        
        // Schedule 1 hour before
        long reminderTime = bookingTime - (60 * 60 * 1000);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 
                (int) bookingTime, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null && reminderTime > System.currentTimeMillis()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
    }

    public void cancelWorkoutReminder() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WorkoutReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    // Broadcast Receivers
    public static class WorkoutReminderReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationHelper helper = new NotificationHelper(context);
            helper.showWorkoutReminder(
                    context.getString(R.string.notif_workout_reminder_title),
                    context.getString(R.string.notif_workout_reminder_msg)
            );
        }
    }

    public static class BookingReminderReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String bookingDetails = intent.getStringExtra("booking_details");
            NotificationHelper helper = new NotificationHelper(context);
            helper.showBookingReminder(
                    context.getString(R.string.notif_booking_reminder_title),
                    context.getString(R.string.notif_booking_reminder_msg, bookingDetails),
                    System.currentTimeMillis()
            );
        }
    }
}
