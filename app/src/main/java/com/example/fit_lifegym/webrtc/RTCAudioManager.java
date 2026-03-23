package com.example.fit_lifegym.webrtc;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import androidx.core.content.ContextCompat;
import org.webrtc.ThreadUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressLint("MissingPermission")
public class RTCAudioManager {
    private static final String TAG = "RTCAudioManager";
    private final Context context;
    private final AudioManager audioManager;
    private final BluetoothManager bluetoothManager;
    private final BroadcastReceiver wiredHeadsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state", 0);
            hasWiredHeadset = state == 1;
            updateAudioDeviceState();
        }
    };
    private AudioManagerState amState = AudioManagerState.UNINITIALIZED;
    private AudioManagerEvents audioManagerEvents;
    private int savedAudioMode = AudioManager.MODE_INVALID;
    private boolean savedIsSpeakerPhoneOn = false;
    private boolean savedIsMicrophoneMute = false;
    private boolean hasWiredHeadset = false;
    private AudioDevice defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
    private AudioDevice selectedAudioDevice = AudioDevice.NONE;
    private AudioDevice userSelectedAudioDevice = AudioDevice.NONE;
    private ProximitySensor proximitySensor;
    private Set<AudioDevice> audioDevices = new HashSet<>();
    private AudioFocusRequest audioFocusRequest;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private boolean isWiredHeadsetReceiverRegistered = false;

    private RTCAudioManager(Context context) {
        ThreadUtils.checkIsOnMainThread();
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.bluetoothManager = BluetoothManager.create(context, this);
        this.proximitySensor = ProximitySensor.create(context, this::onProximitySensorChangedState);
    }

    public static RTCAudioManager create(Context context) {
        return new RTCAudioManager(context);
    }

    private void onProximitySensorChangedState() {
        if (audioDevices.size() == 2 && audioDevices.contains(AudioDevice.EARPIECE) &&
            audioDevices.contains(AudioDevice.SPEAKER_PHONE)) {
            if (proximitySensor.sensorReportsNearState()) {
                setAudioDeviceInternal(AudioDevice.EARPIECE);
            } else {
                setAudioDeviceInternal(AudioDevice.SPEAKER_PHONE);
            }
        }
    }

    public void start(AudioManagerEvents events) {
        ThreadUtils.checkIsOnMainThread();
        if (amState == AudioManagerState.RUNNING) return;
        audioManagerEvents = events;
        amState = AudioManagerState.RUNNING;
        
        savedAudioMode = audioManager.getMode();
        savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
        savedIsMicrophoneMute = audioManager.isMicrophoneMute();
        hasWiredHeadset = hasWiredHeadsetInternal();
        
        audioFocusChangeListener = focusChange -> Log.d(TAG, "onAudioFocusChange: " + focusChange);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build();
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setMicrophoneMute(false);
        userSelectedAudioDevice = AudioDevice.NONE;
        selectedAudioDevice = AudioDevice.NONE;
        audioDevices.clear();
        
        // Android 12+ Bluetooth Permission Guard
        if (hasBluetoothConnectPermission()) {
            bluetoothManager.start();
        } else {
            Log.w(TAG, "No BLUETOOTH_CONNECT permission → Skipping Bluetooth init to prevent crash");
        }
        
        updateAudioDeviceState();
        
        if (!isWiredHeadsetReceiverRegistered) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(wiredHeadsetReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                context.registerReceiver(wiredHeadsetReceiver, filter);
            }
            isWiredHeadsetReceiverRegistered = true;
        }
        
        proximitySensor.start();
    }

    private boolean hasBluetoothConnectPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) 
                == PackageManager.PERMISSION_GRANTED;
    }

    public void stop() {
        ThreadUtils.checkIsOnMainThread();
        if (amState != AudioManagerState.RUNNING) return;
        amState = AudioManagerState.UNINITIALIZED;
        
        if (isWiredHeadsetReceiverRegistered) {
            try {
                context.unregisterReceiver(wiredHeadsetReceiver);
            } catch (Exception e) {
                Log.w(TAG, "Failed to unregister wiredHeadsetReceiver", e);
            }
            isWiredHeadsetReceiverRegistered = false;
        }
        
        bluetoothManager.stop();
        audioManager.setSpeakerphoneOn(savedIsSpeakerPhoneOn);
        audioManager.setMicrophoneMute(savedIsMicrophoneMute);
        audioManager.setMode(savedAudioMode);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest != null) audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else if (audioFocusChangeListener != null) {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }

        audioFocusChangeListener = null;
        proximitySensor.stop();
        audioManagerEvents = null;
    }

    private void setAudioDeviceInternal(AudioDevice device) {
        Log.d(TAG, "setAudioDeviceInternal: " + device);
        switch (device) {
            case SPEAKER_PHONE: audioManager.setSpeakerphoneOn(true); break;
            case EARPIECE:
            case BLUETOOTH:
            case WIRED_HEADSET: audioManager.setSpeakerphoneOn(false); break;
            default: break;
        }
        selectedAudioDevice = device;
    }

    public void selectAudioDevice(AudioDevice device) {
        ThreadUtils.checkIsOnMainThread();
        if (!audioDevices.contains(device)) {
            Log.w(TAG, "Device " + device + " not available");
        }
        userSelectedAudioDevice = device;
        updateAudioDeviceState();
    }

    private boolean hasWiredHeadsetInternal() {
        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
        for (AudioDeviceInfo device : devices) {
            int type = device.getType();
            if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET || type == AudioDeviceInfo.TYPE_USB_DEVICE || type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEarpiece() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public void updateAudioDeviceState() {
        ThreadUtils.checkIsOnMainThread();
        
        Set<AudioDevice> newAudioDevices = new HashSet<>();
        
        if (hasBluetoothConnectPermission()) {
            bluetoothManager.updateDevice();
            if (bluetoothManager.getState() == BluetoothManager.State.SCO_CONNECTED || 
                bluetoothManager.getState() == BluetoothManager.State.SCO_CONNECTING || 
                bluetoothManager.getState() == BluetoothManager.State.HEADSET_AVAILABLE) {
                newAudioDevices.add(AudioDevice.BLUETOOTH);
            }
        }

        if (hasWiredHeadset) {
            newAudioDevices.add(AudioDevice.WIRED_HEADSET);
        } else {
            newAudioDevices.add(AudioDevice.SPEAKER_PHONE);
            if (hasEarpiece()) newAudioDevices.add(AudioDevice.EARPIECE);
        }

        boolean audioDeviceSetUpdated = !audioDevices.equals(newAudioDevices);
        audioDevices = newAudioDevices;

        if (hasBluetoothConnectPermission()) {
            if (bluetoothManager.getState() == BluetoothManager.State.HEADSET_UNAVAILABLE && userSelectedAudioDevice == AudioDevice.BLUETOOTH) {
                userSelectedAudioDevice = AudioDevice.NONE;
            }

            boolean needBluetoothAudioStart = bluetoothManager.getState() == BluetoothManager.State.HEADSET_AVAILABLE && (userSelectedAudioDevice == AudioDevice.NONE || userSelectedAudioDevice == AudioDevice.BLUETOOTH);
            if (needBluetoothAudioStart) {
                if (!bluetoothManager.startScoAudio()) audioDevices.remove(AudioDevice.BLUETOOTH);
            }
        }

        AudioDevice newAudioDevice = AudioDevice.NONE;
        if (hasBluetoothConnectPermission() && bluetoothManager.getState() == BluetoothManager.State.SCO_CONNECTED) {
            newAudioDevice = AudioDevice.BLUETOOTH;
        } else if (hasWiredHeadset) {
            newAudioDevice = AudioDevice.WIRED_HEADSET;
        } else if (userSelectedAudioDevice != AudioDevice.NONE) {
            newAudioDevice = userSelectedAudioDevice;
        } else {
            newAudioDevice = defaultAudioDevice;
        }

        if (newAudioDevice != selectedAudioDevice || audioDeviceSetUpdated) {
            setAudioDeviceInternal(newAudioDevice);
            if (audioManagerEvents != null) audioManagerEvents.onAudioDeviceChanged(selectedAudioDevice, audioDevices);
        }
    }

    public enum AudioDevice { SPEAKER_PHONE, WIRED_HEADSET, EARPIECE, BLUETOOTH, NONE }
    public enum AudioManagerState { UNINITIALIZED, RUNNING }
    public interface AudioManagerEvents {
        void onAudioDeviceChanged(AudioDevice selectedAudioDevice, Set<AudioDevice> availableAudioDevices);
    }
}
