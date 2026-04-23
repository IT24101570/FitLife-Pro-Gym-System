package com.example.fit_lifegym.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.fit_lifegym.DoctorActivity;
import com.example.fit_lifegym.MainActivity;
import com.example.fit_lifegym.R;
import com.example.fit_lifegym.TrainerActivity;
import com.example.fit_lifegym.utils.DataModel;
import com.example.fit_lifegym.utils.DataModelType;
import com.example.fit_lifegym.utils.SessionManager;
import com.example.fit_lifegym.webrtc.RTCAudioManager;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainService extends Service implements MainRepository.CallListener {

    private static final String TAG = "MainService";

    public static final String ACTION_START_SERVICE       = "START_SERVICE";
    public static final String ACTION_SETUP_VIEWS         = "SETUP_VIEWS";
    public static final String ACTION_END_CALL            = "END_CALL";
    public static final String ACTION_SWITCH_CAMERA       = "SWITCH_CAMERA";
    public static final String ACTION_TOGGLE_AUDIO        = "TOGGLE_AUDIO";
    public static final String ACTION_TOGGLE_VIDEO        = "TOGGLE_VIDEO";
    public static final String ACTION_TOGGLE_AUDIO_DEVICE = "TOGGLE_AUDIO_DEVICE";
    public static final String ACTION_SEND_HEALTH_DATA    = "SEND_HEALTH_DATA";
    public static final String ACTION_STOP_SERVICE        = "STOP_SERVICE";

    public static final String CHANNEL_ID_SERVICE  = "call_service_channel";
    public static final String CHANNEL_ID_INCOMING = "incoming_call_channel";

    public static SurfaceViewRenderer localRenderer;
    public static SurfaceViewRenderer remoteRenderer;

    private static IncomingCallListener incomingCallListener;
    private static EndCallListener      endCallListener;
    private static ConnectedListener    connectedListener;
    private static RemoteStatusListener remoteStatusListener;

    @Inject MainRepository repository;
    private RTCAudioManager rtcAudioManager;
    private SessionManager sessionManager;

    private boolean isCallActive = false;
    private DataModel bufferedIncomingCall = null;

    private HandlerThread serviceThread;
    private Handler serviceHandler;
    private Handler mainHandler;

    private final List<Intent> pendingIntents = new ArrayList<>();

    public static void setIncomingCallListener(IncomingCallListener listener) {
        incomingCallListener = listener;
        if (instance != null && listener != null) {
            instance.deliverBufferedIncomingCall();
        }
    }

    public static void setEndCallListener(EndCallListener listener) {
        endCallListener = listener;
    }

    public static void setConnectedListener(ConnectedListener listener) {
        connectedListener = listener;
    }

    public static void setRemoteStatusListener(RemoteStatusListener listener) {
        remoteStatusListener = listener;
    }

    private static MainService instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.i(TAG, "MainService created");

        sessionManager = new SessionManager(this);
        mainHandler = new Handler(Looper.getMainLooper());

        createNotificationChannels();
        startForegroundWithDynamicNotification(false);

        serviceThread = new HandlerThread("MainService-Worker");
        serviceThread.start();
        serviceHandler = new Handler(serviceThread.getLooper());

        mainHandler.post(() -> {
            try {
                rtcAudioManager = RTCAudioManager.create(MainService.this);
                repository.setCallListener(this);
                
                serviceHandler.post(() -> {
                    initializeSignalingForCurrentUser();
                    processAllPendingIntents();
                });
            } catch (Exception e) {
                Log.e(TAG, "Initialization failed", e);
            }
        });
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm == null) return;

        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID_SERVICE,
                "Ongoing Session Service",
                NotificationManager.IMPORTANCE_LOW
        );
        nm.createNotificationChannel(serviceChannel);

        NotificationChannel incomingChannel = new NotificationChannel(
                CHANNEL_ID_INCOMING,
                "Incoming Live Session",
                NotificationManager.IMPORTANCE_HIGH
        );
        nm.createNotificationChannel(incomingChannel);
    }

    private void initializeSignalingForCurrentUser() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.trim().isEmpty()) return;

        repository.login(userId, "dummy", (success, msg) -> {
            if (!success) Log.e(TAG, "Signaling login failed: " + msg);
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;

        String action = intent.getAction();
        if (ACTION_STOP_SERVICE.equals(action)) {
            stopAndCleanup();
            return START_NOT_STICKY;
        }

        if (repository == null) {
            synchronized (pendingIntents) {
                pendingIntents.add(intent);
            }
            return START_STICKY;
        }

        handleIntent(intent);
        return START_STICKY;
    }

    private synchronized void processAllPendingIntents() {
        synchronized (pendingIntents) {
            for (Intent intent : pendingIntents) {
                handleIntent(intent);
            }
            pendingIntents.clear();
        }
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case ACTION_START_SERVICE:
                initializeSignalingForCurrentUser();
                break;

            case ACTION_SETUP_VIEWS:
                handleSetupViewsIntent(intent);
                break;

            case ACTION_END_CALL:
                repository.endCurrentCall();
                onCallEnded();
                break;

            case ACTION_TOGGLE_AUDIO:
                repository.toggleLocalAudio(intent.getBooleanExtra("shouldBeMuted", false));
                break;

            case ACTION_TOGGLE_VIDEO:
                repository.toggleLocalVideo(intent.getBooleanExtra("shouldBeMuted", false));
                break;

            case ACTION_SWITCH_CAMERA:
                repository.switchLocalCamera();
                break;

            case ACTION_TOGGLE_AUDIO_DEVICE:
                String device = intent.getStringExtra("type");
                if (device != null) {
                    mainHandler.post(() -> {
                        if (rtcAudioManager != null) {
                            try {
                                rtcAudioManager.selectAudioDevice(RTCAudioManager.AudioDevice.valueOf(device));
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to select audio device", e);
                            }
                        }
                    });
                }
                break;

            case ACTION_SEND_HEALTH_DATA:
                String data = intent.getStringExtra("data");
                if (data != null) repository.sendHealthData(data);
                break;
        }
    }

    private void handleSetupViewsIntent(Intent intent) {
        boolean isVideo = intent.getBooleanExtra("isVideoCall", true);
        boolean isCaller = intent.getBooleanExtra("isCaller", true);
        String target = intent.getStringExtra("target");

        if (target == null || target.trim().isEmpty()) {
            onCallEnded();
            return;
        }

        mainHandler.post(() -> {
            if (localRenderer == null || remoteRenderer == null) {
                mainHandler.postDelayed(() -> handleSetupViewsIntent(intent), 400);
                return;
            }

            repository.setupViews(localRenderer, remoteRenderer);

            serviceHandler.post(() -> {
                // START AUDIO MANAGER FIRST
                mainHandler.post(() -> {
                    if (rtcAudioManager != null) {
                        rtcAudioManager.start((selected, available) -> {});
                    }
                });

                if (!isCaller) {
                    repository.prepareForIncomingCallResponse(sessionManager.getName(), target);
                }
                
                // Then start media
                repository.startLocalMedia(isVideo);

                mainHandler.postDelayed(() -> {
                    if (isCaller) {
                        String senderName = sessionManager.getName();
                        repository.initiateOutgoingCall(senderName, target, isVideo, success -> {
                            if (success) {
                                repository.startCallNegotiation();
                            } else {
                                onCallEnded();
                            }
                        });
                    }
                }, 900);
            });
        });
    }

    private void startForegroundWithDynamicNotification(boolean inCall) {
        String title = "Fit Life Gym";
        String text  = inCall ? "Live session in progress" : "Ready for sessions";

        Intent notificationIntent = new Intent(this, MainActivity.class);
        if (sessionManager.isDoctor()) notificationIntent = new Intent(this, DoctorActivity.class);
        else if (sessionManager.isTrainer()) notificationIntent = new Intent(this, TrainerActivity.class);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_SERVICE)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_call)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(pendingIntent);

        if (inCall) {
            Intent endCallIntent = new Intent(this, CallNotificationReceiver.class);
            endCallIntent.setAction("ACTION_END_CALL_FROM_NOTIF");
            PendingIntent endCallPendingIntent = PendingIntent.getBroadcast(this, 1, endCallIntent, PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(R.drawable.ic_end_call, "End Call", endCallPendingIntent);
        }

        int foregroundType = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= 34) {
                foregroundType = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
            }
            
            if (inCall) {
                foregroundType |= ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA |
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && foregroundType != 0) {
                startForeground(101, builder.build(), foregroundType);
            } else {
                startForeground(101, builder.build());
            }
        } catch (Exception e) {
            Log.e(TAG, "startForeground failed", e);
            startForeground(101, builder.build());
        }
    }

    @Override
    public void onIncomingCall(DataModel model) {
        if (model == null || model.getType() == null) return;

        DataModelType type = model.getType();
        if (type == DataModelType.StartVideoCall || type == DataModelType.StartAudioCall || type == DataModelType.Offer) {
            if (bufferedIncomingCall == null || !model.getId().equals(bufferedIncomingCall.getId())) {
                showIncomingCallNotification(model);
                bufferedIncomingCall = model;
            }
        } else if (type == DataModelType.EndCall) {
            cancelIncomingNotification();
            bufferedIncomingCall = null;
        }

        if (incomingCallListener != null) {
            mainHandler.post(() -> { if (incomingCallListener != null) incomingCallListener.onIncomingCall(model); });
        }
    }

    private void showIncomingCallNotification(DataModel model) {
        String callerName = (model.getSenderName() != null && !model.getSenderName().trim().isEmpty())
                ? model.getSenderName() : model.getSender();

        Class<?> targetActivity = MainActivity.class;
        if (sessionManager.isDoctor())   targetActivity = DoctorActivity.class;
        else if (sessionManager.isTrainer()) targetActivity = TrainerActivity.class;

        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Accept Action
        Intent acceptIntent = new Intent(this, CallNotificationReceiver.class);
        acceptIntent.setAction("ACTION_ACCEPT_CALL");
        acceptIntent.putExtra("target", model.getSender());
        acceptIntent.putExtra("targetName", callerName);
        acceptIntent.putExtra("isVideoCall", model.getType() != DataModelType.StartAudioCall);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, 2, acceptIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Decline Action
        Intent declineIntent = new Intent(this, CallNotificationReceiver.class);
        declineIntent.setAction("ACTION_DECLINE_CALL");
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(this, 3, declineIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_INCOMING)
                .setSmallIcon(R.drawable.ic_call)
                .setContentTitle("Incoming Live Session")
                .setContentText(callerName + " is calling you")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(pi, true)
                .setAutoCancel(true)
                .setOngoing(true)
                .addAction(R.drawable.ic_call, "Accept", acceptPendingIntent)
                .addAction(R.drawable.ic_end_call, "Decline", declinePendingIntent)
                .setContentIntent(pi);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(102, builder.build());
    }

    private void cancelIncomingNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(102);
    }

    private void deliverBufferedIncomingCall() {
        if (bufferedIncomingCall != null && incomingCallListener != null) {
            incomingCallListener.onIncomingCall(bufferedIncomingCall);
        }
    }

    @Override
    public void onCallConnected() {
        isCallActive = true;
        startForegroundWithDynamicNotification(true);
        cancelIncomingNotification();
        if (connectedListener != null) {
            mainHandler.post(() -> { if (connectedListener != null) connectedListener.onCallConnected(); });
        }
    }

    @Override
    public void onCallEnded() {
        isCallActive = false;
        startForegroundWithDynamicNotification(false);
        cancelIncomingNotification();
        bufferedIncomingCall = null;
        if (rtcAudioManager != null) rtcAudioManager.stop();
        if (endCallListener != null) {
            mainHandler.post(() -> { if (endCallListener != null) endCallListener.onCallEnded(); });
        }
    }

    @Override
    public void onRemoteCameraStatusChanged(boolean isEnabled) {
        mainHandler.post(() -> { if (remoteStatusListener != null) remoteStatusListener.onRemoteCameraStatusChanged(isEnabled); });
    }

    @Override
    public void onRemoteAudioStatusChanged(boolean isEnabled) {
        mainHandler.post(() -> { if (remoteStatusListener != null) remoteStatusListener.onRemoteAudioStatusChanged(isEnabled); });
    }

    @Override
    public void onPeerBusy() {
        mainHandler.post(() -> { if (remoteStatusListener != null) remoteStatusListener.onPeerBusy(); });
    }

    @Override
    public void onRemoteHealthDataReceived(String data) {
        mainHandler.post(() -> { if (remoteStatusListener != null) remoteStatusListener.onRemoteHealthDataReceived(data); });
    }

    private void stopAndCleanup() {
        repository.endCurrentCall();
        if (rtcAudioManager != null) rtcAudioManager.stop();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (serviceThread != null) serviceThread.quitSafely();
        instance = null;
        super.onDestroy();
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }

    public interface IncomingCallListener { void onIncomingCall(DataModel model); }
    public interface EndCallListener { void onCallEnded(); }
    public interface ConnectedListener { void onCallConnected(); }
    public interface RemoteStatusListener {
        void onRemoteCameraStatusChanged(boolean isEnabled);
        void onRemoteAudioStatusChanged(boolean isEnabled);
        void onPeerBusy();
        void onRemoteHealthDataReceived(String data);
    }
}
