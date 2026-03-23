package com.example.fit_lifegym.webrtc;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import org.webrtc.ThreadUtils;
import java.util.List;

@SuppressLint("MissingPermission")
public class BluetoothManager {
    private static final int BLUETOOTH_SCO_TIMEOUT_MS = 4000;
    private static final int MAX_SCO_CONNECTION_ATTEMPTS = 2;
    private final Context context;
    private final RTCAudioManager rtcAudioManager;
    private final android.media.AudioManager audioManager;
    private final Handler handler;
    private final BluetoothProfile.ServiceListener bluetoothServiceListener;
    private final BroadcastReceiver bluetoothHeadsetReceiver;
    private int scoConnectionAttempts;
    private State bluetoothState;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHeadset bluetoothHeadset;
    private BluetoothDevice bluetoothDevice;
    private final Runnable bluetoothTimeoutRunnable = this::bluetoothTimeout;

    private BluetoothManager(Context context, RTCAudioManager rtcAudioManager) {
        ThreadUtils.checkIsOnMainThread();
        this.context = context;
        this.rtcAudioManager = rtcAudioManager;
        this.audioManager = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        bluetoothState = State.UNINITIALIZED;
        bluetoothServiceListener = new BluetoothServiceListener();
        bluetoothHeadsetReceiver = new BluetoothHeadsetBroadcastReceiver();
        handler = new Handler(Looper.getMainLooper());
    }

    static BluetoothManager create(Context context, RTCAudioManager rtcAudioManager) {
        return new BluetoothManager(context, rtcAudioManager);
    }

    public State getState() {
        ThreadUtils.checkIsOnMainThread();
        return bluetoothState;
    }

    public void start() {
        ThreadUtils.checkIsOnMainThread();
        if (!hasPermission()) return;
        if (bluetoothState != State.UNINITIALIZED) return;

        bluetoothHeadset = null;
        bluetoothDevice = null;
        scoConnectionAttempts = 0;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) return;
        if (!audioManager.isBluetoothScoAvailableOffCall()) return;

        if (!bluetoothAdapter.getProfileProxy(context, bluetoothServiceListener, BluetoothProfile.HEADSET)) return;

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        context.registerReceiver(bluetoothHeadsetReceiver, filter);
        bluetoothState = State.HEADSET_UNAVAILABLE;
    }

    public void stop() {
        ThreadUtils.checkIsOnMainThread();
        if (bluetoothAdapter == null) return;
        stopScoAudio();
        if (bluetoothState == State.UNINITIALIZED) return;
        context.unregisterReceiver(bluetoothHeadsetReceiver);
        handler.removeCallbacks(bluetoothTimeoutRunnable);
        if (bluetoothHeadset != null) {
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset);
            bluetoothHeadset = null;
        }
        bluetoothAdapter = null;
        bluetoothDevice = null;
        bluetoothState = State.UNINITIALIZED;
    }

    public boolean startScoAudio() {
        ThreadUtils.checkIsOnMainThread();
        if (scoConnectionAttempts >= MAX_SCO_CONNECTION_ATTEMPTS) return false;
        if (bluetoothState != State.HEADSET_AVAILABLE) return false;
        bluetoothState = State.SCO_CONNECTING;
        audioManager.startBluetoothSco();
        audioManager.setBluetoothScoOn(true);
        scoConnectionAttempts++;
        handler.postDelayed(bluetoothTimeoutRunnable, BLUETOOTH_SCO_TIMEOUT_MS);
        return true;
    }

    public void stopScoAudio() {
        ThreadUtils.checkIsOnMainThread();
        if (bluetoothState != State.SCO_CONNECTING && bluetoothState != State.SCO_CONNECTED) return;
        handler.removeCallbacks(bluetoothTimeoutRunnable);
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);
        bluetoothState = State.SCO_DISCONNECTING;
    }

    public void updateDevice() {
        if (bluetoothState == State.UNINITIALIZED || bluetoothHeadset == null) return;
        List<BluetoothDevice> devices = bluetoothHeadset.getConnectedDevices();
        if (devices.isEmpty()) {
            bluetoothDevice = null;
            bluetoothState = State.HEADSET_UNAVAILABLE;
        } else {
            bluetoothDevice = devices.get(0);
            bluetoothState = State.HEADSET_AVAILABLE;
        }
    }

    private boolean hasPermission() {
        return context.checkPermission(android.Manifest.permission.BLUETOOTH, Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
    }

    private void bluetoothTimeout() {
        ThreadUtils.checkIsOnMainThread();
        if (bluetoothState == State.UNINITIALIZED || bluetoothHeadset == null) return;
        if (bluetoothState != State.SCO_CONNECTING) return;
        boolean scoConnected = false;
        List<BluetoothDevice> devices = bluetoothHeadset.getConnectedDevices();
        if (devices.size() > 0) {
            bluetoothDevice = devices.get(0);
            if (bluetoothHeadset.isAudioConnected(bluetoothDevice)) {
                scoConnected = true;
            }
        }
        if (scoConnected) {
            bluetoothState = State.SCO_CONNECTED;
            scoConnectionAttempts = 0;
        } else {
            stopScoAudio();
        }
        rtcAudioManager.updateAudioDeviceState();
    }

    public enum State {
        UNINITIALIZED, ERROR, HEADSET_UNAVAILABLE, HEADSET_AVAILABLE, SCO_DISCONNECTING, SCO_CONNECTING, SCO_CONNECTED
    }

    private class BluetoothServiceListener implements BluetoothProfile.ServiceListener {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile != BluetoothProfile.HEADSET || bluetoothState == State.UNINITIALIZED) return;
            bluetoothHeadset = (BluetoothHeadset) proxy;
            rtcAudioManager.updateAudioDeviceState();
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile != BluetoothProfile.HEADSET || bluetoothState == State.UNINITIALIZED) return;
            stopScoAudio();
            bluetoothHeadset = null;
            bluetoothDevice = null;
            bluetoothState = State.HEADSET_UNAVAILABLE;
            rtcAudioManager.updateAudioDeviceState();
        }
    }

    private class BluetoothHeadsetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (bluetoothState == State.UNINITIALIZED) return;
            final String action = intent.getAction();
            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);
                if (state == BluetoothHeadset.STATE_CONNECTED) {
                    scoConnectionAttempts = 0;
                    rtcAudioManager.updateAudioDeviceState();
                } else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
                    stopScoAudio();
                    rtcAudioManager.updateAudioDeviceState();
                }
            } else if (BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
                if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    handler.removeCallbacks(bluetoothTimeoutRunnable);
                    if (bluetoothState == State.SCO_CONNECTING) {
                        bluetoothState = State.SCO_CONNECTED;
                        scoConnectionAttempts = 0;
                        rtcAudioManager.updateAudioDeviceState();
                    }
                } else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                    rtcAudioManager.updateAudioDeviceState();
                }
            }
        }
    }
}
