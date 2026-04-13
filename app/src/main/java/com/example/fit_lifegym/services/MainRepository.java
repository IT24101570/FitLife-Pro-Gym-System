package com.example.fit_lifegym.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.fit_lifegym.utils.DataModel;
import com.example.fit_lifegym.utils.DataModelType;
import com.example.fit_lifegym.utils.IceCandidateModel;
import com.example.fit_lifegym.utils.UserStatus;
import com.example.fit_lifegym.webrtc.WebRTCClient;
import com.google.gson.Gson;
import org.webrtc.IceCandidate;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainRepository implements WebRTCClient.Listener {

    private static final String TAG = "MainRepository";

    public interface CallListener {
        void onIncomingCall(DataModel model);
        void onCallEnded();
        void onCallConnected();
        default void onRemoteCameraStatusChanged(boolean isEnabled) {}
        default void onRemoteAudioStatusChanged(boolean isEnabled) {}
        default void onPeerBusy() {}
        default void onRemoteHealthDataReceived(String data) {}
    }

    private final FirebaseClient firebaseClient;
    private final WebRTCClient webRTCClient;
    private final Gson gson;

    private String currentTarget;
    private String myDisplayName;
    private String pendingOfferSdp;
    private final List<IceCandidate> pendingCandidates = new ArrayList<>();
    
    private volatile boolean isLocalMediaReady = false;
    private long sessionStartTime = 0;
    private boolean isInCall = false;

    private CallListener callListener;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public MainRepository(FirebaseClient firebaseClient, WebRTCClient webRTCClient, Gson gson) {
        this.firebaseClient = firebaseClient;
        this.webRTCClient = webRTCClient;
        this.gson = gson;
        this.webRTCClient.setListener(this);
    }

    public void setCallListener(CallListener listener) {
        this.callListener = listener;
    }

    public void login(String username, String password, FirebaseClient.LoginCallback callback) {
        executor.execute(() -> {
            firebaseClient.login(username, password, (success, msg) -> {
                if (success) {
                    mainHandler.post(() -> {
                        webRTCClient.initialize(username);
                        firebaseClient.subscribeForLatestEvent(this::handleIncomingSignaling);
                    });
                }
                if (callback != null) callback.onResponse(success, msg);
            });
        });
    }

    public void observeUsersStatus(FirebaseClient.UsersStatusCallback callback) {
        firebaseClient.observeUsersStatus(callback);
    }

    private void handleIncomingSignaling(DataModel event) {
        if (event == null || event.getType() == null) return;
        if (event.getSender() == null || event.getSender().equals(firebaseClient.getCurrentUsername())) return;

        // Session Start Protection
        if (sessionStartTime > 0 && event.getTimeStamp() < sessionStartTime) {
            Log.d(TAG, "Discarding old signaling: " + event.getType());
            return;
        }

        DataModelType type = event.getType();
        Log.i(TAG, "← Signaling: " + type + " from " + event.getSender());

        switch (type) {
            case StartVideoCall:
            case StartAudioCall:
                if (isInCall) {
                    sendPeerBusy(event.getSender());
                } else if (currentTarget == null) {
                    currentTarget = event.getSender();
                    mainHandler.post(() -> { if (callListener != null) callListener.onIncomingCall(event); });
                }
                break;

            case PeerBusy:
                mainHandler.post(() -> { if (callListener != null) callListener.onPeerBusy(); });
                cleanupCallState();
                break;

            case Offer:
                currentTarget = event.getSender();
                // FIX: Ensure local media (tracks) are added BEFORE processing offer to create answer
                if (isLocalMediaReady) {
                    processRemoteOffer(event.getData());
                } else {
                    pendingOfferSdp = event.getData();
                    Log.d(TAG, "Offer received but local media not ready, buffering");
                }
                break;

            case Answer:
                if (event.getData() != null) {
                    webRTCClient.setRemoteAnswer(event.getData());
                }
                break;

            case IceCandidates:
                try {
                    IceCandidateModel candidateModel = gson.fromJson(event.getData(), IceCandidateModel.class);
                    IceCandidate candidate = new IceCandidate(candidateModel.getSdpMid(), candidateModel.getSdpMLineIndex(), candidateModel.getSdp());
                    if (webRTCClient.isPeerConnectionReady()) {
                        webRTCClient.addIceCandidate(candidate);
                    } else {
                        pendingCandidates.add(candidate);
                        Log.d(TAG, "ICE buffered");
                    }
                } catch (Exception e) { Log.e(TAG, "ICE parse error", e); }
                break;

            case CameraStatus:
                mainHandler.post(() -> { 
                    if (callListener != null) callListener.onRemoteCameraStatusChanged("true".equals(event.getData())); 
                });
                break;

            case AudioStatus:
                mainHandler.post(() -> { 
                    if (callListener != null) callListener.onRemoteAudioStatusChanged("true".equals(event.getData())); 
                });
                break;

            case HealthData:
                mainHandler.post(() -> {
                    if (callListener != null) callListener.onRemoteHealthDataReceived(event.getData());
                });
                break;

            case EndCall:
                mainHandler.post(() -> { if (callListener != null) callListener.onCallEnded(); });
                cleanupCallState();
                break;
        }
    }

    private void sendPeerBusy(String target) {
        DataModel busyModel = new DataModel(null, target, DataModelType.PeerBusy, null);
        firebaseClient.sendMessageToOtherClient(busyModel, null);
    }

    private void processRemoteOffer(String offerSdp) {
        if (offerSdp == null || offerSdp.isEmpty()) return;
        webRTCClient.setRemoteOfferAndCreateAnswer(offerSdp, sdp -> {
            DataModel answer = new DataModel(null, currentTarget, DataModelType.Answer, sdp.description);
            answer.setSenderName(myDisplayName);
            Log.i(TAG, "→ Signaling: Answer sent to " + currentTarget);
            firebaseClient.sendMessageToOtherClient(answer, null);
        });
    }

    public void initiateOutgoingCall(String senderName, String target, boolean isVideo, FirebaseClient.MessageCallback callback) {
        this.currentTarget = target;
        this.myDisplayName = senderName;
        this.sessionStartTime = System.currentTimeMillis();

        DataModel callRequest = new DataModel(null, target, isVideo ? DataModelType.StartVideoCall : DataModelType.StartAudioCall, null);
        callRequest.setSenderName(senderName);
        firebaseClient.sendMessageToOtherClient(callRequest, callback);
    }

    public void prepareForIncomingCallResponse(String myName, String target) {
        this.myDisplayName = myName;
        this.currentTarget = target;
        this.sessionStartTime = System.currentTimeMillis();
    }

    public void setupViews(SurfaceViewRenderer local, SurfaceViewRenderer remote) {
        webRTCClient.setLocalView(local);
        webRTCClient.setRemoteView(remote);
    }

    public void startLocalMedia(boolean isVideo) {
        isLocalMediaReady = false;
        executor.execute(() -> {
            try {
                webRTCClient.preparePeerConnection();
                webRTCClient.startLocalMedia(isVideo);
                isLocalMediaReady = true;
                Log.d(TAG, "Local media ready, tracks added");
                
                mainHandler.post(() -> {
                    if (pendingOfferSdp != null) {
                        Log.d(TAG, "Processing buffered offer");
                        processRemoteOffer(pendingOfferSdp);
                        pendingOfferSdp = null;
                    }
                    if (!pendingCandidates.isEmpty()) {
                        for (IceCandidate c : pendingCandidates) {
                            webRTCClient.addIceCandidate(c);
                        }
                        pendingCandidates.clear();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "startLocalMedia failed", e);
                cleanupCallState();
            }
        });
    }

    public void startCallNegotiation() {
        executor.execute(() -> {
            int waited = 0;
            while (!isLocalMediaReady && waited < 40) {
                try { Thread.sleep(100); } catch (Exception ignored) {}
                waited++;
            }
            if (isLocalMediaReady) {
                webRTCClient.createOffer(sdp -> {
                    DataModel offer = new DataModel(null, currentTarget, DataModelType.Offer, sdp.description);
                    offer.setSenderName(myDisplayName);
                    Log.i(TAG, "→ Signaling: Offer sent to " + currentTarget);
                    firebaseClient.sendMessageToOtherClient(offer, null);
                });
            } else {
                Log.e(TAG, "Failed to start negotiation: Local media not ready");
            }
        });
    }

    public void sendHealthData(String healthJson) {
        if (currentTarget != null) {
            DataModel healthModel = new DataModel(null, currentTarget, DataModelType.HealthData, healthJson);
            firebaseClient.sendMessageToOtherClient(healthModel, null);
        }
    }

    public void endCurrentCall() {
        if (currentTarget != null) {
            firebaseClient.sendMessageToOtherClient(new DataModel(null, currentTarget, DataModelType.EndCall, null), null);
        }
        cleanupCallState();
    }

    private void cleanupCallState() {
        Log.d(TAG, "Cleaning up repository state");
        isLocalMediaReady = false;
        isInCall = false;
        sessionStartTime = 0;
        pendingOfferSdp = null;
        pendingCandidates.clear();
        currentTarget = null;
        webRTCClient.cleanup();
        firebaseClient.clearAllCallData();
        firebaseClient.changeMyStatus(UserStatus.ONLINE);
    }

    @Override
    public void onTransferEventToSocket(DataModel data) {
        if (currentTarget != null) {
            data.setTarget(currentTarget);
            Log.i(TAG, "→ Signaling: " + data.getType() + " sent to " + currentTarget);
            firebaseClient.sendMessageToOtherClient(data, null);
        } else {
            Log.e(TAG, "→ Signaling Error: Cannot send " + data.getType() + " - currentTarget is null");
        }
    }

    @Override public void onCallConnected() {
        isInCall = true;
        firebaseClient.changeMyStatus(UserStatus.IN_CALL);
        mainHandler.post(() -> { if (callListener != null) callListener.onCallConnected(); });
    }

    @Override public void onCallDisconnected() {
        mainHandler.post(() -> { if (callListener != null) callListener.onCallEnded(); });
        cleanupCallState();
    }

    public void setStatusOnline() { firebaseClient.changeMyStatus(UserStatus.ONLINE); }
    
    public void toggleLocalAudio(boolean muted) { 
        webRTCClient.toggleAudio(muted); 
        sendAudioStatus(!muted);
    }
    
    public void toggleLocalVideo(boolean muted) { 
        webRTCClient.toggleVideo(muted); 
        sendCameraStatus(!muted);
    }

    private void sendCameraStatus(boolean isEnabled) {
        if (currentTarget != null) {
            firebaseClient.sendMessageToOtherClient(new DataModel(null, currentTarget, DataModelType.CameraStatus, String.valueOf(isEnabled)), null);
        }
    }

    private void sendAudioStatus(boolean isEnabled) {
        if (currentTarget != null) {
            firebaseClient.sendMessageToOtherClient(new DataModel(null, currentTarget, DataModelType.AudioStatus, String.valueOf(isEnabled)), null);
        }
    }

    public void switchLocalCamera() { webRTCClient.switchCamera(); }
}
