package com.example.fit_lifegym.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import javax.inject.Inject;

public class MainServiceRepository {

    private final Context context;

    @Inject
    public MainServiceRepository(Context context) {
        this.context = context;
    }

    public void startService(String username) {
        if (username == null || username.isEmpty()) {
            Log.e("MainServiceRepository", "Cannot start service: username is null or empty");
            return;
        }
        
        try {
            Intent intent = new Intent(context, MainService.class);
            intent.putExtra("username", username);
            intent.setAction(MainService.ACTION_START_SERVICE);
            startServiceIntent(intent);
        } catch (Exception e) {
            Log.e("MainServiceRepository", "Error starting service", e);
        }
    }

    private void startServiceIntent(Intent intent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception e) {
            Log.e("MainServiceRepository", "Failed to start service intent", e);
        }
    }

    public void setupViews(boolean videoCall, boolean caller, String target, String targetName) {
        if (target == null || target.isEmpty()) {
            Log.e("MainServiceRepository", "Cannot setup views: target is null or empty");
            return;
        }
        
        try {
            Intent intent = new Intent(context, MainService.class);
            intent.setAction(MainService.ACTION_SETUP_VIEWS);
            intent.putExtra("isVideoCall", videoCall);
            intent.putExtra("target", target);
            intent.putExtra("targetName", targetName);
            intent.putExtra("isCaller", caller);
            startServiceIntent(intent);
        } catch (Exception e) {
            Log.e("MainServiceRepository", "Error setting up views", e);
        }
    }

    public void sendEndCall() {
        Intent intent = new Intent(context, MainService.class);
        intent.setAction(MainService.ACTION_END_CALL);
        startServiceIntent(intent);
    }

    public void switchCamera() {
        Intent intent = new Intent(context, MainService.class);
        intent.setAction(MainService.ACTION_SWITCH_CAMERA);
        startServiceIntent(intent);
    }

    public void toggleAudio(boolean shouldBeMuted) {
        Intent intent = new Intent(context, MainService.class);
        intent.setAction(MainService.ACTION_TOGGLE_AUDIO);
        intent.putExtra("shouldBeMuted", shouldBeMuted);
        startServiceIntent(intent);
    }

    public void toggleVideo(boolean shouldBeMuted) {
        Intent intent = new Intent(context, MainService.class);
        intent.setAction(MainService.ACTION_TOGGLE_VIDEO);
        intent.putExtra("shouldBeMuted", shouldBeMuted);
        startServiceIntent(intent);
    }

    public void toggleAudioDevice(String type) {
        Intent intent = new Intent(context, MainService.class);
        intent.setAction(MainService.ACTION_TOGGLE_AUDIO_DEVICE);
        intent.putExtra("type", type);
        startServiceIntent(intent);
    }

    public void stopService() {
        Intent intent = new Intent(context, MainService.class);
        intent.setAction(MainService.ACTION_STOP_SERVICE);
        startServiceIntent(intent);
    }
}
