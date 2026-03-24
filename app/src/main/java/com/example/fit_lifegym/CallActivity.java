package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fit_lifegym.services.MainService;
import com.example.fit_lifegym.services.MainServiceRepository;
import com.example.fit_lifegym.utils.DataModel;
import com.example.fit_lifegym.utils.DataModelType;
import com.example.fit_lifegym.utils.SessionManager;
import com.example.fit_lifegym.utils.WearableManager;
import org.webrtc.SurfaceViewRenderer;
import java.util.Locale;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CallActivity extends AppCompatActivity
        implements MainService.EndCallListener,
        MainService.IncomingCallListener,
        MainService.ConnectedListener,
        MainService.RemoteStatusListener {

    private static final String TAG = "CallActivity";

    @Inject MainServiceRepository serviceRepo;
    private SurfaceViewRenderer localView;
    private SurfaceViewRenderer remoteView;
    private TextView callTitleTv, callTimerTv, connectingTitleTv;
    private TextView tvLiveSteps, tvLiveHeartRate;
    private ImageButton toggleMicrophoneButton, toggleCameraButton, endCallButton;
    private ImageButton switchCameraButton, toggleAudioDevice;
    private View controlsLayout, headerLayout, connectingLayout;
    private View callRootLayout;

    private String target;
    private String targetName;
    private boolean isVideoCall = true;
    private boolean isCaller = true;
    private boolean isMicMuted = false;
    private boolean isCamMuted = false;
    private boolean isSpeaker = true;
    private boolean isControlsVisible = true;

    private WearableManager wearableManager;
    private int currentSteps = 0;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private int seconds = 0;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            seconds++;
            int mins = seconds / 60;
            int secs = seconds % 60;
            callTimerTv.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
            timerHandler.postDelayed(this, 1000);
        }
    };

    private final Handler controlsHandler = new Handler(Looper.getMainLooper());
    private final Runnable hideControlsRunnable = this::hideControls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setContentView(R.layout.activity_call);

            wearableManager = new WearableManager(this);

            target = getIntent().getStringExtra("target");
            targetName = getIntent().getStringExtra("targetName");
            isVideoCall = getIntent().getBooleanExtra("isVideoCall", true);
            isCaller = getIntent().getBooleanExtra("isCaller", true);

            if (target == null || target.trim().isEmpty()) {
                Toast.makeText(this, "Invalid call target", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            initViews();
            initListeners();

            MainService.setEndCallListener(this);
            MainService.setIncomingCallListener(this);
            MainService.setConnectedListener(this);
            MainService.setRemoteStatusListener(this);

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    serviceRepo.sendEndCall();
                    finish();
                }
            });

            callRootLayout.post(() -> {
                if (isFinishing()) return;
                assignViewsToService();
                startServiceAndCall();
                startHealthTracking();
                showControls();
            });

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
            finish();
        }
    }

    private void initViews() {
        callRootLayout     = findViewById(R.id.call_root_layout);
        localView          = findViewById(R.id.local_view);
        remoteView         = findViewById(R.id.remote_view);
        callTitleTv        = findViewById(R.id.call_title_tv);
        callTimerTv        = findViewById(R.id.call_timer_tv);
        connectingTitleTv  = findViewById(R.id.connecting_title_tv);
        tvLiveSteps        = findViewById(R.id.tv_live_steps);
        tvLiveHeartRate    = findViewById(R.id.tv_live_heart_rate);
        toggleMicrophoneButton = findViewById(R.id.toggle_microphone_button);
        toggleCameraButton = findViewById(R.id.toggle_camera_button);
        endCallButton      = findViewById(R.id.end_call_button);
        switchCameraButton = findViewById(R.id.switch_camera_button);
        toggleAudioDevice  = findViewById(R.id.toggle_audio_device);
        controlsLayout     = findViewById(R.id.controls_layout);
        headerLayout       = findViewById(R.id.header_layout);
        connectingLayout   = findViewById(R.id.connecting_layout);

        if (localView == null || remoteView == null) {
            finish();
            return;
        }

        String displayName = (targetName != null && !targetName.trim().isEmpty()) ? targetName : target;
        callTitleTv.setText("Call with " + displayName);
        connectingTitleTv.setText("Connecting to " + displayName + "...");

        if (!isVideoCall) {
            toggleCameraButton.setVisibility(View.GONE);
            switchCameraButton.setVisibility(View.GONE);
            localView.setVisibility(View.GONE);
        }
    }

    private void initListeners() {
        callRootLayout.setOnClickListener(v -> {
            if (isControlsVisible) hideControls();
            else showControls();
        });

        endCallButton.setOnClickListener(v -> {
            serviceRepo.sendEndCall();
            finish();
        });

        toggleMicrophoneButton.setOnClickListener(v -> {
            isMicMuted = !isMicMuted;
            toggleMicrophoneButton.setImageResource(isMicMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic_on);
            serviceRepo.toggleAudio(isMicMuted);
            resetAutoHideTimer();
        });

        toggleCameraButton.setOnClickListener(v -> {
            isCamMuted = !isCamMuted;
            toggleCameraButton.setImageResource(isCamMuted ? R.drawable.ic_video_off : R.drawable.ic_video_on);
            serviceRepo.toggleVideo(isCamMuted);
            resetAutoHideTimer();
        });

        switchCameraButton.setOnClickListener(v -> {
            serviceRepo.switchCamera();
            resetAutoHideTimer();
        });

        toggleAudioDevice.setOnClickListener(v -> {
            isSpeaker = !isSpeaker;
            toggleAudioDevice.setImageResource(isSpeaker ? R.drawable.ic_speaker : R.drawable.ic_ear);
            serviceRepo.toggleAudioDevice(isSpeaker ? "SPEAKER_PHONE" : "EARPIECE");
            resetAutoHideTimer();
        });
    }

    private void assignViewsToService() {
        MainService.localRenderer  = localView;
        MainService.remoteRenderer = remoteView;
    }

    private void startServiceAndCall() {
        SessionManager session = new SessionManager(this);
        String username = session.getUserId();
        if (username == null || username.trim().isEmpty()) {
            finish();
            return;
        }
        serviceRepo.startService(username);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;
            serviceRepo.setupViews(isVideoCall, isCaller, target, targetName);
        }, 800);
    }

    private void startHealthTracking() {
        if (wearableManager.hasPermissions()) {
            wearableManager.startSensorTracking(new WearableManager.OnHealthDataListener() {
                @Override public void onHeartRateUpdate(int bpm) {
                    runOnUiThread(() -> tvLiveHeartRate.setText("Heart: " + bpm + " bpm"));
                }
                @Override public void onStepCountUpdate(int steps) {
                    currentSteps += steps;
                    runOnUiThread(() -> tvLiveSteps.setText("Steps: " + currentSteps));
                }
            });
        }
    }

    private void showControls() {
        isControlsVisible = true;
        controlsLayout.setVisibility(View.VISIBLE);
        headerLayout.setVisibility(View.VISIBLE);
        resetAutoHideTimer();
    }

    private void hideControls() {
        isControlsVisible = false;
        controlsLayout.setVisibility(View.GONE);
        headerLayout.setVisibility(View.GONE);
        controlsHandler.removeCallbacks(hideControlsRunnable);
    }

    private void resetAutoHideTimer() {
        controlsHandler.removeCallbacks(hideControlsRunnable);
        controlsHandler.postDelayed(hideControlsRunnable, 5000);
    }

    @Override public void onCallEnded() {
        runOnUiThread(() -> {
            if (!isFinishing()) {
                Toast.makeText(this, "Call Ended", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override public void onIncomingCall(DataModel model) {
        if (model == null) return;
        runOnUiThread(() -> {
            if (model.getType() == DataModelType.EndCall) finish();
        });
    }

    @Override public void onCallConnected() {
        runOnUiThread(() -> {
            if (isFinishing()) return;
            connectingLayout.setVisibility(View.GONE);
            if (seconds == 0) timerHandler.postDelayed(timerRunnable, 1000);
        });
    }

    @Override public void onRemoteCameraStatusChanged(boolean isEnabled) {
        runOnUiThread(() -> {
            if (remoteView != null) remoteView.setVisibility(isEnabled ? View.VISIBLE : View.INVISIBLE);
        });
    }

    @Override public void onRemoteAudioStatusChanged(boolean isEnabled) {}

    @Override public void onPeerBusy() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Peer is busy", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override public void onRemoteHealthDataReceived(String data) {
        runOnUiThread(() -> {
            Log.d(TAG, "Health Data: " + data);
            // Update Trainer UI with member's stats
        });
    }

    @Override
    protected void onDestroy() {
        timerHandler.removeCallbacks(timerRunnable);
        controlsHandler.removeCallbacks(hideControlsRunnable);
        if (wearableManager != null) wearableManager.stopSensorTracking();

        // Safe cleanup of WebRTC renderers
        if (localView != null) {
            localView.release();
            localView = null;
        }
        if (remoteView != null) {
            remoteView.release();
            remoteView = null;
        }

        // Clear static references in service to prevent leaks
        MainService.localRenderer = null;
        MainService.remoteRenderer = null;
        MainService.setEndCallListener(null);
        MainService.setIncomingCallListener(null);
        MainService.setConnectedListener(null);
        MainService.setRemoteStatusListener(null);

        super.onDestroy();
    }
}
