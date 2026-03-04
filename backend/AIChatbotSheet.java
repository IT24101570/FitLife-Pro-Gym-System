package com.example.fit_lifegym;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.models.NutritionLog;
import com.example.fit_lifegym.models.User;
import com.example.fit_lifegym.utils.FirebaseHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AIChatbotSheet extends BottomSheetDialogFragment {

    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    private SessionManager sessionManager;
    private DatabaseReference workoutsRef, userRef, nutritionRef;
    
    private User currentUser;
    private NutritionLog currentNutrition;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        
        // This ensures the typing area moves up when the keyboard appears
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                
                // Set height to match parent for full screen effect
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chat, container, false);

        sessionManager = new SessionManager(getContext());
        String userId = sessionManager.getUserId();
        if (userId != null) {
            workoutsRef = FirebaseHelper.getWorkoutsRef(userId);
            userRef = FirebaseHelper.getUsersRef().child(userId);
            nutritionRef = FirebaseHelper.getDatabase().getReference("nutrition").child(userId);
        }

        rvChat = view.findViewById(R.id.rvChat);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setVisibility(View.GONE);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);

        rvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChat.setAdapter(adapter);

        fetchUserData();
        
        String welcome = "Welcome to FitLife Gym! 🏋️‍♂️ I'm your AI assistant. You can ask me about:\n\n" +
                "• How to book a Trainer or Doctor\n" +
                "• Tracking your Workouts and Nutrition\n" +
                "• Upgrading your membership\n" +
                "• Using the Video Library or Social Feed\n\n" +
                "How can I help you today?";
        addBotMessage(welcome);

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                addUserMessage(text);
                etMessage.setText("");
                new Handler().postDelayed(() -> addBotMessage(generateAIResponse(text.toLowerCase())), 800);
            }
        });

        return view;
    }

    private void fetchUserData() {
        if (userRef != null) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currentUser = snapshot.getValue(User.class);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        
        if (nutritionRef != null) {
            nutritionRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        currentNutrition = ds.getValue(NutritionLog.class);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private String generateAIResponse(String input) {
        // App Navigation & Instructions
        if (input.contains("book") || input.contains("trainer") || input.contains("doctor")) {
            return "To book a session, go to the home dashboard and look for 'Book Doctor' or 'Book Trainer' cards under the Elite Concierge section. 📅";
        }

        if (input.contains("workout") || input.contains("track") || input.contains("tracker")) {
            return "You can track your workouts using the 'Tracker' card on your home screen. To see workout plans, click on 'Workout Plans' to explore our library! 🏋️‍♀️";
        }

        if (input.contains("nutrition") || input.contains("meal") || input.contains("food") || input.contains("calories")) {
            return "Manage your diet in the 'Nutrition' section. You can also view custom 'Meal Plans' assigned by our experts. Tracking calories is easy! 🥗";
        }

        if (input.contains("upgrade") || input.contains("premium") || input.contains("elite") || input.contains("membership")) {
            return "Ready for the next level? Tap the 'Upgrade to Elite' button at the bottom of your home screen to unlock all premium features! 💎";
        }

        if (input.contains("video") || input.contains("library") || input.contains("watch")) {
            return "Check out the 'Video Library' for high-quality workout videos and instructions from our pro trainers! 🎥";
        }

        if (input.contains("social") || input.contains("feed") || input.contains("community")) {
            return "Join the conversation! Use the 'Social Feed' to share your progress and see updates from other FitLife members. 🤝";
        }

        if (input.contains("app") || input.contains("how to") || input.contains("help") || input.contains("instructions")) {
            return "FitLife Gym app helps you manage your entire fitness journey. Use the cards on the main screen to book sessions, track progress, watch videos, and stay social! Need more help? Just ask! 📱";
        }

        // Personalized Greeting
        if (input.contains("hi") || input.contains("hello")) {
            String name = (currentUser != null) ? currentUser.getName() : "friend";
            return "Hello " + name + "! How can I guide you through the FitLife app today?";
        }

        // Specific Nutrition Insight (Dynamic)
        if ((input.contains("my") && input.contains("calories")) || input.contains("today")) {
            if (currentNutrition != null) {
                int eaten = currentNutrition.getTotalCalories();
                int remaining = currentNutrition.getRemainingCalories();
                return "You've logged " + eaten + " kcal today. You have " + remaining + " kcal left. Keep up the good work! 🍎";
            }
        }

        return "I'm here to help you navigate FitLife! You can ask about booking trainers, tracking meals, workout plans, or our social feed. What's on your mind? 💡";
    }

    private void addUserMessage(String text) {
        messageList.add(new ChatMessage(text, ChatMessage.TYPE_USER));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
    }

    private void addBotMessage(String text) {
        messageList.add(new ChatMessage(text, ChatMessage.TYPE_BOT));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
    }
}
