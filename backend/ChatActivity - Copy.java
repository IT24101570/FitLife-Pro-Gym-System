package com.example.fit_lifegym;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageView btnBack;
    private ChatMessageAdapter adapter;
    private List<ChatMessage> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        setupChat();
        
        // Initial greeting
        addBotMessage("Hello! I'm your FitLife Assistant. How can I help you today? (Try asking about weight, muscle, trainers, or diet)");
    }

    private void initViews() {
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupChat() {
        messageList = new ArrayList<>();
        adapter = new ChatMessageAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adapter);
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (!text.isEmpty()) {
            addUserMessage(text);
            etMessage.setText("");
            processBotResponse(text.toLowerCase());
        }
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

    private void processBotResponse(String userText) {
        // Simulate thinking time
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (userText.contains("weight")) {
                addBotMessage("To lose weight, focus on a calorie deficit and consistent cardio. Check our 'Nutrition Tracker' for meal plans!");
            } else if (userText.contains("muscle")) {
                addBotMessage("Building muscle requires progressive overload and high protein. View our 'Workout Plans' for hypertrophy routines.");
            } else if (userText.contains("trainer")) {
                addBotMessage("You can book a session with our expert trainers in the 'Bookings' section.");
            } else if (userText.contains("doctor") || userText.contains("diet")) {
                addBotMessage("For specialized medical or diet advice, please schedule a consultation with our Gym Doctor.");
            } else {
                addBotMessage("That's interesting! Tell me more, or ask about weight, muscle, or bookings.");
            }
        }, 1000);
    }
}
