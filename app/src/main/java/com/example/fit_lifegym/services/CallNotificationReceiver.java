package com.example.fit_lifegym.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.fit_lifegym.CallActivity;

public class CallNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "CallNotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Action received: " + action);

        if (action == null) return;

        MainServiceRepository repository = new MainServiceRepository(context);

        switch (action) {
            case "ACTION_ACCEPT_CALL":
                String target = intent.getStringExtra("target");
                String targetName = intent.getStringExtra("targetName");
                boolean isVideo = intent.getBooleanExtra("isVideoCall", true);

                Intent callIntent = new Intent(context, CallActivity.class);
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                callIntent.putExtra("target", target);
                callIntent.putExtra("targetName", targetName);
                callIntent.putExtra("isVideoCall", isVideo);
                callIntent.putExtra("isCaller", false);
                context.startActivity(callIntent);
                break;

            case "ACTION_DECLINE_CALL":
            case "ACTION_END_CALL_FROM_NOTIF":
                repository.sendEndCall();
                break;
        }
    }
}
