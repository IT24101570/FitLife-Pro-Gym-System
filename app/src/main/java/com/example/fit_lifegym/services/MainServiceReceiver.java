package com.example.fit_lifegym.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MainServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "ACTION_EXIT_CALL".equals(intent.getAction())) {
            MainServiceRepository serviceRepository = new MainServiceRepository(context);
            serviceRepository.sendEndCall();
        }
    }
}
