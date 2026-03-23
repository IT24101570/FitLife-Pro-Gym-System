package com.example.fit_lifegym.webrtc;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.fit_lifegym.utils.DataModel;
import com.example.fit_lifegym.utils.DataModelType;
import com.example.fit_lifegym.utils.IceCandidateModel;
import com.google.gson.Gson;

import org.webrtc.*;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WebRTCClient implements PeerConnection.Observer {

    private static final String TAG = "WebRTCClient";

    public interface Listener {
        void onTransferEventToSocket(DataModel data);
        void onCallConnected();
        void onCallDisconnected();
    }

    private Listener listener;
    private final Context context;
    private final Gson gson;

    private EglBase eglBase;
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private AudioDeviceModule audioDeviceModule;

    private AudioTrack localAudioTrack;
    private VideoTrack localVideoTrack;
    private CameraVideoCapturer videoCapturer;
    private SurfaceTextureHelper surfaceTextureHelper;

    private SurfaceViewRenderer localView;
    private SurfaceViewRenderer remoteView;
    private VideoTrack remoteVideoTrack;

    private final List<IceCandidate> pendingIceCandidates = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final List<PeerConnection.IceServer> iceServers = new ArrayList<PeerConnection.IceServer>() {{
        add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
        add(PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer());
        add(PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer());
        
        // Public TURN Servers (metered.ca)
        add(PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
                .setUsername("openrelayproject").setPassword("openrelayproject").createIceServer());
        add(PeerConnection.IceServer.builder("turn:openrelay.metered.ca:443")
                .setUsername("openrelayproject").setPassword("openrelayproject").createIceServer());
        add(PeerConnection.IceServer.builder("turn:openrelay.metered.ca:443?transport=tcp")
                .setUsername("openrelayproject").setPassword("openrelayproject").createIceServer());
    }};

    @Inject
    public WebRTCClient(Context context, Gson gson) {
        this.context = context.getApplicationContext();
        this.gson = gson;
        initEglBase();
    }

    private synchronized void initEglBase() {
        if (eglBase == null) eglBase = EglBase.create();
    }

    public void setListener(Listener listener) { this.listener = listener; }

    public synchronized void initialize(String username) {
        if (peerConnectionFactory != null) return;
        initEglBase();

        try {
            PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(context)
                            .setEnableInternalTracer(true)
                            .createInitializationOptions()
            );

            this.audioDeviceModule = JavaAudioDeviceModule.builder(context)
                    .setUseHardwareAcousticEchoCanceler(false)
                    .setUseHardwareNoiseSuppressor(false)
                    .setAudioSource(MediaRecorder.AudioSource.MIC)
                    .createAudioDeviceModule();

            peerConnectionFactory = PeerConnectionFactory.builder()
                    .setAudioDeviceModule(audioDeviceModule)
                    .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBase.getEglBaseContext()))
                    .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), true, true))
                    .setOptions(new PeerConnectionFactory.Options())
                    .createPeerConnectionFactory();

            Log.d(TAG, "WebRTC Engine Initialized");
        } catch (Exception e) { Log.e(TAG, "WebRTC init failed", e); }
    }

    public boolean isPeerConnectionReady() { return peerConnection != null; }

    public void preparePeerConnection() {
        if (peerConnection != null) return;
        
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        rtcConfig.iceCandidatePoolSize = 10;
        rtcConfig.iceTransportsType = PeerConnection.IceTransportsType.ALL; 

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, this);
        Log.d(TAG, "PeerConnection prepared");
    }

    public void setLocalView(SurfaceViewRenderer view) {
        this.localView = view;
        mainHandler.post(() -> {
            initViewInternal(view, true);
            if (localVideoTrack != null) localVideoTrack.addSink(view);
        });
    }

    public void setRemoteView(SurfaceViewRenderer view) {
        this.remoteView = view;
        mainHandler.post(() -> {
            initViewInternal(view, false);
            if (remoteVideoTrack != null) remoteVideoTrack.addSink(view);
        });
    }

    private void initViewInternal(SurfaceViewRenderer view, boolean isLocal) {
        try {
            view.init(eglBase.getEglBaseContext(), null);
            view.setMirror(isLocal);
            view.setEnableHardwareScaler(true);
            view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
            if (isLocal) view.setZOrderMediaOverlay(true);
        } catch (Exception e) { Log.d(TAG, "View init error: " + e.getMessage()); }
    }

    public void startLocalMedia(boolean isVideo) {
        if (peerConnectionFactory == null) return;

        if (localAudioTrack == null) {
            localAudioTrack = peerConnectionFactory.createAudioTrack("audio_track", peerConnectionFactory.createAudioSource(new MediaConstraints()));
            localAudioTrack.setEnabled(true);
        }

        if (isVideo && localVideoTrack == null) {
            videoCapturer = createVideoCapturer();
            if (videoCapturer != null) {
                surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
                VideoSource videoSource = peerConnectionFactory.createVideoSource(false);
                videoCapturer.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());
                videoCapturer.startCapture(640, 480, 30);
                localVideoTrack = peerConnectionFactory.createVideoTrack("video_track", videoSource);
                localVideoTrack.setEnabled(true);
                if (localView != null) localVideoTrack.addSink(localView);
            }
        }
        // Deferred track binding to ensure transceivers are matched properly
    }

    private void updateTransceiversWithLocalTracks() {
        if (peerConnection == null) return;
        List<RtpTransceiver> transceivers = peerConnection.getTransceivers();
        
        if (transceivers.isEmpty()) {
            List<String> streamIds = Collections.singletonList("ARDAMS");
            if (localAudioTrack != null) peerConnection.addTrack(localAudioTrack, streamIds);
            if (localVideoTrack != null) peerConnection.addTrack(localVideoTrack, streamIds);
        } else {
            for (RtpTransceiver transceiver : transceivers) {
                if (transceiver.getMediaType() == MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO && localAudioTrack != null) {
                    transceiver.getSender().setTrack(localAudioTrack, false);
                    transceiver.setDirection(RtpTransceiver.RtpTransceiverDirection.SEND_RECV);
                } else if (transceiver.getMediaType() == MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO && localVideoTrack != null) {
                    transceiver.getSender().setTrack(localVideoTrack, false);
                    transceiver.setDirection(RtpTransceiver.RtpTransceiverDirection.SEND_RECV);
                }
            }
        }
    }

    public void createOffer(SdpCallback callback) {
        updateTransceiversWithLocalTracks();
        MediaConstraints constraints = new MediaConstraints();
        // Constraints are empty for Unified Plan; transceivers handle the intent.

        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onSetSuccess() {
                        callback.onSdpCreated(sdp);
                        drainIceCandidates();
                    }
                    @Override public void onCreateSuccess(SessionDescription sessionDescription) {}
                    @Override public void onCreateFailure(String s) { Log.e(TAG, "setLocalDescription failure: " + s); }
                    @Override public void onSetFailure(String s) { Log.e(TAG, "setLocalDescription failure: " + s); }
                }, sdp);
            }
            @Override public void onSetSuccess() {}
            @Override public void onCreateFailure(String s) { Log.e(TAG, "createOffer failure: " + s); }
            @Override public void onSetFailure(String s) {}
        }, constraints);
    }

    public void setRemoteOfferAndCreateAnswer(String offerSdp, SdpCallback callback) {
        SessionDescription remoteDesc = new SessionDescription(SessionDescription.Type.OFFER, offerSdp);
        peerConnection.setRemoteDescription(new SdpObserver() {
            @Override
            public void onSetSuccess() {
                updateTransceiversWithLocalTracks();
                drainIceCandidates();
                peerConnection.createAnswer(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sdp) {
                        peerConnection.setLocalDescription(new SdpObserver() {
                            @Override
                            public void onSetSuccess() { callback.onSdpCreated(sdp); }
                            @Override public void onCreateSuccess(SessionDescription ds) {}
                            @Override public void onCreateFailure(String s) { Log.e(TAG, "setLocalDescription failure: " + s); }
                            @Override public void onSetFailure(String s) { Log.e(TAG, "setLocalDescription failure: " + s); }
                        }, sdp);
                    }
                    @Override public void onSetSuccess() {}
                    @Override public void onCreateFailure(String s) { Log.e(TAG, "createAnswer failure: " + s); }
                    @Override public void onSetFailure(String s) {}
                }, new MediaConstraints());
            }
            @Override public void onCreateSuccess(SessionDescription ds) {}
            @Override public void onCreateFailure(String s) { Log.e(TAG, "setRemoteDescription failure: " + s); }
            @Override public void onSetFailure(String s) {}
        }, remoteDesc);
    }

    public void setRemoteAnswer(String answerSdp) {
        peerConnection.setRemoteDescription(new SdpObserver() {
            @Override
            public void onSetSuccess() { drainIceCandidates(); }
            @Override public void onCreateSuccess(SessionDescription ds) {}
            @Override public void onCreateFailure(String s) { Log.e(TAG, "setRemoteDescription failure: " + s); }
            @Override public void onSetFailure(String s) {}
        }, new SessionDescription(SessionDescription.Type.ANSWER, answerSdp));
    }

    public void addIceCandidate(IceCandidate candidate) {
        if (peerConnection != null && peerConnection.getRemoteDescription() != null) {
            peerConnection.addIceCandidate(candidate);
        } else { pendingIceCandidates.add(candidate); }
    }

    private void drainIceCandidates() {
        if (peerConnection == null || peerConnection.getRemoteDescription() == null) return;
        for (IceCandidate c : pendingIceCandidates) peerConnection.addIceCandidate(c);
        pendingIceCandidates.clear();
    }

    @Override public void onSignalingChange(PeerConnection.SignalingState state) {}
    
    @Override public void onIceConnectionChange(PeerConnection.IceConnectionState state) {
        Log.d(TAG, "onIceConnectionChange: " + state);
        
        if (state == PeerConnection.IceConnectionState.CONNECTED) {
            mainHandler.postDelayed(() -> {
                if (peerConnection != null) {
                    peerConnection.getStats(report -> {
                        Log.d("WebRTC-Stats", "--- Stats Delivered ---");
                        for (RTCStats stats : report.getStatsMap().values()) {
                            if (stats.getType().equals("inbound-rtp")) {
                                Log.d("WebRTC-Stats", "Inbound (" + stats.getMembers().get("kind") + "): " + stats.getMembers().get("bytesReceived") + " bytes");
                            }
                        }
                    });
                }
            }, 5000);
        }

        if (state == PeerConnection.IceConnectionState.CONNECTED || state == PeerConnection.IceConnectionState.COMPLETED) {
            mainHandler.post(() -> { if (listener != null) listener.onCallConnected(); });
        } else if (state == PeerConnection.IceConnectionState.DISCONNECTED || state == PeerConnection.IceConnectionState.FAILED) {
            mainHandler.post(() -> { if (listener != null) listener.onCallDisconnected(); });
        }
    }

    @Override public void onIceConnectionReceivingChange(boolean receiving) {}
    @Override public void onIceGatheringChange(PeerConnection.IceGatheringState state) {}
    @Override public void onIceCandidate(IceCandidate candidate) {
        if (listener != null) {
            IceCandidateModel model = new IceCandidateModel(candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp);
            listener.onTransferEventToSocket(new DataModel(null, null, DataModelType.IceCandidates, gson.toJson(model)));
        }
    }
    @Override public void onIceCandidatesRemoved(IceCandidate[] candidates) {}
    
    @Override public void onAddStream(MediaStream stream) {
        Log.d(TAG, "onAddStream: " + stream.getId());
        if (!stream.videoTracks.isEmpty()) {
            remoteVideoTrack = stream.videoTracks.get(0);
            remoteVideoTrack.setEnabled(true);
            mainHandler.post(() -> { if (remoteView != null) remoteVideoTrack.addSink(remoteView); });
        }
        if (!stream.audioTracks.isEmpty()) {
            stream.audioTracks.get(0).setEnabled(true);
        }
    }
    @Override public void onRemoveStream(MediaStream stream) {}
    @Override public void onDataChannel(DataChannel channel) {}
    @Override public void onRenegotiationNeeded() {
        Log.d(TAG, "onRenegotiationNeeded");
    }
    
    @Override public void onAddTrack(RtpReceiver receiver, MediaStream[] streams) {
        MediaStreamTrack track = receiver.track();
        Log.d(TAG, "onAddTrack: " + track.kind() + " | id: " + track.id());
        if (track instanceof VideoTrack) {
            remoteVideoTrack = (VideoTrack) track;
            remoteVideoTrack.setEnabled(true);
            mainHandler.post(() -> {
                if (remoteView != null) remoteVideoTrack.addSink(remoteView);
            });
        } else if (track instanceof AudioTrack) {
            track.setEnabled(true);
        }
    }

    public void toggleAudio(boolean mute) { if (localAudioTrack != null) localAudioTrack.setEnabled(!mute); }
    public void toggleVideo(boolean mute) { if (localVideoTrack != null) localVideoTrack.setEnabled(!mute); }
    public void switchCamera() { if (videoCapturer != null) videoCapturer.switchCamera(null); }

    public void cleanup() {
        Log.d(TAG, "Cleaning up WebRTC resources");
        try {
            if (videoCapturer != null) {
                videoCapturer.stopCapture();
                videoCapturer.dispose();
                videoCapturer = null;
            }
            if (peerConnection != null) {
                peerConnection.close();
                peerConnection.dispose();
                peerConnection = null;
            }
            if (surfaceTextureHelper != null) {
                surfaceTextureHelper.dispose();
                surfaceTextureHelper = null;
            }
            if (audioDeviceModule != null) {
                audioDeviceModule.release();
                audioDeviceModule = null;
            }
            localVideoTrack = null;
            localAudioTrack = null;
            remoteVideoTrack = null;
            Log.d(TAG, "Cleanup completed");
        } catch (Exception e) { Log.e(TAG, "Cleanup error", e); }
    }

    private CameraVideoCapturer createVideoCapturer() {
        Camera2Enumerator enumerator = new Camera2Enumerator(context);
        for (String name : enumerator.getDeviceNames()) {
            if (enumerator.isFrontFacing(name)) return enumerator.createCapturer(name, null);
        }
        return null;
    }

    public interface SdpCallback { void onSdpCreated(SessionDescription sdp); }
}
