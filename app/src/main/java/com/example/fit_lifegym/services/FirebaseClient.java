package com.example.fit_lifegym.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.fit_lifegym.utils.DataModel;
import com.example.fit_lifegym.utils.UserStatus;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseClient {

    private static final String TAG = "FirebaseClient";
    private final DatabaseReference dbRef;
    private final Gson gson;
    private String currentUsername;
    private DatabaseReference signalingRef;
    private ChildEventListener signalingListener;

    private static final Set<String> processedMessageIds = new HashSet<>();

    @Inject
    public FirebaseClient(DatabaseReference rootRef, Gson gson) {
        this.dbRef = rootRef.child("video_call_users");
        this.gson = gson;
    }

    public String getCurrentUsername() { return currentUsername; }

    public void login(String username, String password, LoginCallback callback) {
        if (username == null || username.trim().isEmpty()) {
            if (callback != null) callback.onResponse(false, "Username required");
            return;
        }
        currentUsername = username.trim();
        dbRef.child(currentUsername).child("status").setValue(UserStatus.ONLINE.name());
        dbRef.child(currentUsername).child("status").onDisconnect().setValue(UserStatus.OFFLINE.name());
        if (callback != null) callback.onResponse(true, null);
    }

    public void observeUsersStatus(UsersStatusCallback callback) {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<UserStatusModel> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String key = ds.getKey();
                    if (key != null && !key.equals(currentUsername)) {
                        String status = ds.child("status").getValue(String.class);
                        if (status == null) status = UserStatus.OFFLINE.name();
                        list.add(new UserStatusModel(key, status));
                    }
                }
                callback.onUsersStatusChanged(list);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void subscribeForLatestEvent(LatestEventListener listener) {
        if (currentUsername == null) return;
        
        if (signalingRef != null && signalingListener != null) {
            signalingRef.removeEventListener(signalingListener);
        }

        signalingRef = dbRef.child(currentUsername).child("signaling");
        signalingListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
                processSnapshot(snapshot, listener);
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String s) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String s) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        signalingRef.addChildEventListener(signalingListener);
    }

    private void processSnapshot(DataSnapshot snapshot, LatestEventListener listener) {
        try {
            Object val = snapshot.getValue();
            if (val == null) return;
            DataModel event = gson.fromJson(val.toString(), DataModel.class);
            if (event == null || event.getId() == null) return;

            if (processedMessageIds.contains(event.getId())) return;
            processedMessageIds.add(event.getId());
            if (processedMessageIds.size() > 500) processedMessageIds.clear();

            listener.onLatestEventReceived(event);
            snapshot.getRef().removeValue();

        } catch (Exception e) {
            Log.e(TAG, "Process error", e);
            snapshot.getRef().removeValue();
        }
    }

    public void sendMessageToOtherClient(DataModel message, MessageCallback callback) {
        if (message.getTarget() == null) {
            Log.e(TAG, "Cannot send message: Target is null");
            if (callback != null) callback.onResponse(false);
            return;
        }
        if (currentUsername == null) {
            Log.e(TAG, "Cannot send message: currentUsername is null");
            if (callback != null) callback.onResponse(false);
            return;
        }
        
        message.setSender(currentUsername);
        if (message.getId() == null) message.setId(System.currentTimeMillis() + "_" + currentUsername);
        message.setTimeStamp(System.currentTimeMillis());

        String payload = gson.toJson(message);
        Log.d(TAG, "→ Sending " + message.getType() + " to " + message.getTarget());
        
        dbRef.child(message.getTarget()).child("signaling").push().setValue(payload)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "→ Successfully sent " + message.getType() + " to " + message.getTarget());
                } else {
                    Log.e(TAG, "→ Failed to send " + message.getType() + " to " + message.getTarget(), task.getException());
                }
                if (callback != null) callback.onResponse(task.isSuccessful());
            });
    }

    public void changeMyStatus(UserStatus status) {
        if (currentUsername != null) dbRef.child(currentUsername).child("status").setValue(status.name());
    }

    public void clearAllCallData() {
        if (currentUsername != null) {
            dbRef.child(currentUsername).child("signaling").removeValue();
        }
    }

    public interface LoginCallback { void onResponse(boolean success, String message); }
    public interface LatestEventListener { void onLatestEventReceived(DataModel event); }
    public interface MessageCallback { void onResponse(boolean success); }
    public interface UsersStatusCallback { void onUsersStatusChanged(List<UserStatusModel> users); }

    public static class UserStatusModel {
        public String username;
        public String status;
        public UserStatusModel(String username, String status) {
            this.username = username;
            this.status = status;
        }
    }
}
