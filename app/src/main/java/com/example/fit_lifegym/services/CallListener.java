package com.example.fit_lifegym.services;

import com.example.fit_lifegym.utils.DataModel;

public interface CallListener {
    void onIncomingCall(DataModel model);
    void onCallEnded();
    void onCallConnected();
}
