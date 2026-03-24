package com.example.fit_lifegym.webrtc;

import android.util.Log;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class MySdpObserver implements SdpObserver {
    private static final String TAG = "MySdpObserver";

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        // Default implementation: do nothing
    }

    @Override
    public void onSetSuccess() {
        // Default implementation: do nothing
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e(TAG, "SDP creation failed: " + s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e(TAG, "SDP set failed: " + s);
    }
}
