package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fit_lifegym.models.User;
import com.example.fit_lifegym.utils.FirebaseHelper;
import com.example.fit_lifegym.utils.SessionManager;
import com.example.fit_lifegym.utils.ValidationUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ConversationalOnboardingActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private SessionManager sessionManager;

    private int step = 0;
    private String tempName, tempEmail, tempPassword, tempGoal, tempRole;
    private final String ADMIN_SECRET = "admin123";
    private boolean isExistingUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversational_onboarding);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersRef();
        sessionManager = new SessionManager(this);

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        startConversation();

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                addUserMessage(text);
                processInput(text);
                etMessage.setText("");
            }
        });
    }

    private void startConversation() {
        addBotMessage("Hi! I'm your FitLife assistant. 🤖 I'll help you get started. Do you already have an account? (YES/NO)");
    }

    private void processInput(String input) {
        new Handler().postDelayed(() -> {
            String trimmedInput = input.trim();
            switch (step) {
                case 0: // Check for existing user
                    if (trimmedInput.equalsIgnoreCase("YES")) {
                        isExistingUser = true;
                        step = 10; // Login flow
                        addBotMessage("Welcome back! What's your email address?");
                    } else if (trimmedInput.equalsIgnoreCase("NO")) {
                        isExistingUser = false;
                        step = 1; // Registration flow
                        addBotMessage("Great! Let's get you registered. What's your full name?");
                    } else {
                        addBotMessage("Please answer YES or NO.");
                    }
                    break;

                // --- REGISTRATION FLOW ---
                case 1: // Name
                    tempName = trimmedInput;
                    step++;
                    addBotMessage("Nice to meet you, " + tempName + "! Are you joining as a MEMBER, TRAINER, or DOCTOR?");
                    break;
                case 2: // Role
                    if (trimmedInput.equalsIgnoreCase(ADMIN_SECRET)) {
                        tempRole = "ADMIN";
                        tempGoal = "System Management";
                        step = 3;
                        addBotMessage("Admin access requested. What's your admin email?");
                    } else {
                        String inputRole = trimmedInput.toUpperCase();
                        if (inputRole.equals("MEMBER") || inputRole.equals("TRAINER") || inputRole.equals("DOCTOR")) {
                            tempRole = inputRole;
                            step = 3;
                            addBotMessage("Got it. What's your email address?");
                        } else {
                            addBotMessage("Please specify MEMBER, TRAINER, or DOCTOR.");
                        }
                    }
                    break;
                case 3: // Email
                    if (ValidationUtils.isValidEmail(trimmedInput)) {
                        tempEmail = trimmedInput;
                        step++;
                        addBotMessage("Now, please choose a strong password (min 6 characters).");
                    } else {
                        addBotMessage("Invalid email. Please try again!");
                    }
                    break;
                case 4: // Password
                    if (trimmedInput.length() >= 6) {
                        tempPassword = trimmedInput;
                        if ("MEMBER".equals(tempRole)) {
                            step++;
                            addBotMessage("What's your fitness goal? (Weight Loss, Muscle Gain, Fitness)");
                        } else if ("ADMIN".equals(tempRole)) {
                            addBotMessage("Creating your admin profile... ⏳");
                            registerUser();
                        } else {
                            tempGoal = "Professional Services";
                            addBotMessage("Creating your professional profile... ⏳");
                            registerUser();
                        }
                    } else {
                        addBotMessage("Password too weak! Min 6 characters please.");
                    }
                    break;
                case 5: // Goal
                    tempGoal = trimmedInput;
                    addBotMessage("Perfect! Finalizing your setup... ⏳");
                    registerUser();
                    break;

                // --- LOGIN FLOW ---
                case 10: // Login Email
                    if (ValidationUtils.isValidEmail(trimmedInput)) {
                        tempEmail = trimmedInput;
                        step++;
                        addBotMessage("And your password?");
                    } else {
                        addBotMessage("Invalid email. Please try again!");
                    }
                    break;
                case 11: // Login Password
                    tempPassword = trimmedInput;
                    addBotMessage("Logging you in... ⏳");
                    loginUser();
                    break;
            }
        }, 800);
    }

    private void loginUser() {
        mAuth.signInWithEmailAndPassword(tempEmail, tempPassword)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        loadUserData(firebaseUser.getUid());
                    }
                } else {
                    addBotMessage("Login failed: " + task.getException().getMessage());
                    step = 10; // Reset to email step
                    addBotMessage("Let's try again. What's your email?");
                }
            });
    }

    private void loadUserData(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        sessionManager.createLoginSession(userId, user.getName(), user.getEmail(), user.getRole());
                        addBotMessage("Welcome back, " + user.getName() + "! Redirecting... 🚀");
                        navigateToHome();
                    }
                } else {
                    addBotMessage("User data not found. Let's register you instead.");
                    step = 1;
                    addBotMessage("What's your full name?");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                addBotMessage("Error: " + error.getMessage());
            }
        });
    }

    private void registerUser() {
        mAuth.createUserWithEmailAndPassword(tempEmail, tempPassword)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userId = mAuth.getCurrentUser().getUid();
                    User user = new User(userId, tempName, tempEmail, tempRole);
                    user.setFitnessGoal(tempGoal);
                    
                    usersRef.child(userId).setValue(user)
                        .addOnSuccessListener(aVoid -> {
                            sessionManager.createLoginSession(userId, tempName, tempEmail, tempRole);
                            addBotMessage("Success! Welcome to FitLife. Redirecting... 🎉");
                            navigateToHome();
                        });
                } else {
                    addBotMessage("Error: " + task.getException().getMessage());
                    step = 3; // Retry email
                }
            });
    }

    private void navigateToHome() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(ConversationalOnboardingActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 1500);
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
