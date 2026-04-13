package com.example.fit_lifegym;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
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
    private String tempName, tempEmail, tempPassword, tempGoal, tempRole, tempPhone;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    
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

        setupPhoneAuth();
        startConversation();

        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (!text.isEmpty()) {
            addUserMessage(text);
            processInput(text);
            etMessage.setText("");
        }
    }

    private void startConversation() {
        addBotMessage("Hi! I'm your FitLife assistant. 🤖 I'll help you get started. Do you already have an account? (YES/NO)");
    }

    private void setupPhoneAuth() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // This is called for instant verification or auto-retrieval
                addBotMessage("Phone verified automatically! ⚡");
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                addBotMessage("Verification failed: " + e.getMessage());
                step = 6; // Back to phone input
                addBotMessage("Please try again. Enter your phone number (e.g., +94...):");
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                addBotMessage("OTP Sent! 📩 Please type the 6-digit code you received:(test OTP code is: 123456)");
                step = 7;
            }
        };
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode(String code) {
        if (mVerificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        addBotMessage("Verifying code... ⏳");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        addBotMessage("Phone verified! Creating your account... 🚀");
                        // Now that we are signed in with Phone, link Email/Password
                        linkEmailPassword();
                    } else {
                        addBotMessage("Invalid OTP code. Please try again.");
                    }
                });
    }

    private void linkEmailPassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(tempEmail, tempPassword);
            user.linkWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserToDatabase(user.getUid());
                    } else {
                        addBotMessage("Error linking email: " + task.getException().getMessage());
                    }
                });
        }
    }

    private void saveUserToDatabase(String userId) {
        User user = new User(userId, tempName, tempEmail, tempRole);
        user.setFitnessGoal(tempGoal);
        user.setPhone(tempPhone);
        
        usersRef.child(userId).setValue(user)
            .addOnSuccessListener(aVoid -> {
                sessionManager.createLoginSession(userId, tempName, tempEmail, tempPhone, tempRole, tempPassword);
                addBotMessage("Registration Complete! Welcome to FitLife. 🎉");
                navigateToHome();
            })
            .addOnFailureListener(e -> addBotMessage("Database Error: " + e.getMessage()));
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
                        addBotMessage("Now, please choose a strong password (min 8 characters, with at least one digit, one lowercase, and one uppercase letter).");
                    } else {
                        addBotMessage("Invalid email. Please try again!");
                    }
                    break;
                case 4: // Password
                    if (ValidationUtils.isStrongPassword(trimmedInput)) {
                        tempPassword = trimmedInput;
                        if ("MEMBER".equals(tempRole)) {
                            step++;
                            addBotMessage("What's your fitness goal? (Weight Loss, Muscle Gain, Fitness)");
                        } else {
                            if ("ADMIN".equals(tempRole)) {
                                tempGoal = "System Management";
                            } else {
                                tempGoal = "Professional Services";
                            }
                            step = 6;
                            addBotMessage("Almost there! What's your phone number for verification? (This feature is firebase paid version.this is test phone number:\n"+" +94 70 391 6542,\n" + "+94 74 082 9891,\n" + "+94 70 552 9897 )");
                        }
                    } else {
                        addBotMessage("Password too weak! Min 6 characters please.");
                    }
                    break;
                case 5: // Goal
                    tempGoal = trimmedInput;
                    step = 6;
                    addBotMessage("Perfect! Finally, what's your phone number for verification? (This feature is firebase paid version.this is test phone number:\n"+"+94 70 391 6542,\n" + "+94 74 082 9891,\n" + "+94 70 552 9897 )");
                    break;
                case 6: // Phone Number
                    if (Patterns.PHONE.matcher(trimmedInput).matches()) {
                        tempPhone = trimmedInput;
                        addBotMessage("Sending verification code to " + tempPhone + "... 📱");
                        sendVerificationCode(tempPhone);
                    } else {
                        addBotMessage("Invalid phone number. Please include your country code (e.g., +1...).");
                    }
                    break;
                case 7: // OTP Code
                    if (trimmedInput.length() == 6) {
                        verifyCode(trimmedInput);
                    } else {
                        addBotMessage("Please enter a valid 6-digit code.");
                    }
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
                    addBotMessage("Login failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
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
                        sessionManager.createLoginSession(userId, user.getName(), user.getEmail(), user.getPhone(), user.getRole(), tempPassword);
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
                    user.setPhone(tempPhone);
                    
                    usersRef.child(userId).setValue(user)
                        .addOnSuccessListener(aVoid -> {
                            sessionManager.createLoginSession(userId, tempName, tempEmail, tempPhone, tempRole, tempPassword);
                            addBotMessage("Success! Welcome to FitLife. Redirecting... 🎉");
                            navigateToHome();
                        });
                } else {
                    addBotMessage("Error: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
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
