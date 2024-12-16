package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityResetPasswordBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.HashUtil;
import com.example.chatapp.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
//
//public class ResetPasswordActivity extends AppCompatActivity {
//    private ActivityResetPasswordBinding binding;
//    private FirebaseFirestore firestoreDatabase;
//    private PreferenceManager preferenceManager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//        preferenceManager = new PreferenceManager(getApplicationContext());
//        firestoreDatabase = FirebaseFirestore.getInstance();
//
//        setListeners();
//    }
//
//    private void setListeners() {
//        binding.buttonForgotPasswordBack.setOnClickListener(v -> {
//            startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
//            finish();
//        });
//
//        binding.buttonReset.setOnClickListener(v -> {
//            if (isValidPassword()) {
//                String newPassword = binding.inputPassword.getText().toString().trim();
//                updatePasswordInFirestore(newPassword);
//            }
//        });
//    }
//
//    private void showToast(String message) {
//        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//    }
//
//    private boolean isValidPassword() {
//        String password = binding.inputPassword.getText().toString().trim();
//        String confirmPassword = binding.inputConfirmPassword.getText().toString().trim();
//
//        if (password.isEmpty()) {
//            showToast("Enter password");
//            return false;
//        } else if (confirmPassword.isEmpty()) {
//            showToast("Confirm your password");
//            return false;
//        } else if (!password.equals(confirmPassword)) {
//            showToast("Password & confirm password must be same");
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    private void updatePasswordInFirestore(String newPassword) {
//        loading(true);
//        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
//
//        if (userId == null || userId.isEmpty()) {
//            showToast("User ID not found.");
//            return;
//        }
//
//        // Hash the new password
//        String hashedPassword = HashUtil.hashPassword(newPassword);
//
//        // Update user data
//        firestoreDatabase.collection(Constants.KEY_COLLECTION_USERS)
//                .document(userId)
//                .update(Constants.KEY_PASSWORD, hashedPassword)
//                .addOnSuccessListener(unused -> {
//                    showToast("Password updated successfully.");
//                    signOut();
//                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                })
//                .addOnFailureListener(exception -> {
//                    loading(false);
//                    showToast("Failed to update password: " + exception.getMessage());
//                });
//    }
//
//
//
//
//
//
//
//
//
//
//    private void loading(Boolean isLoading){
//        if(isLoading){
//            binding.buttonReset.setVisibility(View.INVISIBLE);
//            binding.progressBar.setVisibility(View.VISIBLE);
//        }
//        else {
//            binding.progressBar.setVisibility(View.INVISIBLE);
//            binding.buttonReset.setVisibility(View.VISIBLE);
//        }
//    }
//
//
//
//    public void signOut(){
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
//                preferenceManager.getString(Constants.KEY_USER_ID)
//        );
//        HashMap<String, Object> updates = new HashMap<>();
//        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
//        documentReference.update(updates).addOnSuccessListener(unused -> {
//            showToast("Reset password successfully");
//            preferenceManager.clear();
//            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
//            finish();
//        }).addOnFailureListener(e -> showToast("Unable to reset password"));
//
//    }
//}
public class ResetPasswordActivity extends AppCompatActivity {
    private ActivityResetPasswordBinding binding;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseFirestore.getInstance();

        // Lấy email từ Intent
        String email = getIntent().getStringExtra(Constants.KEY_EMAIL);

        setListeners(email);
    }

    private void setListeners(String email) {
        binding.buttonReset.setOnClickListener(v -> {
            String newPassword = binding.inputPassword.getText().toString().trim();
            if (isValidPassword(newPassword)) {
                resetPassword(email, newPassword);
            }
        });
    }

    private boolean isValidPassword(String password) {
        if (password.isEmpty()) {
            showToast("Please enter a new password.");
            return false;
        } else if (password.length() < 6) {
            showToast("Password must be at least 6 characters.");
            return false;
        }
        return true;
    }

    private void resetPassword(String email, String newPassword) {
        loading(true);

        // Hash mật khẩu mới
        String hashedPassword = HashUtil.hashPassword(newPassword);

        // Cập nhật mật khẩu trong Firestore
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        // Lấy user document ID
                        String userId = task.getResult().getDocuments().get(0).getId();

                        // Cập nhật mật khẩu
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(userId)
                                .update(Constants.KEY_PASSWORD, hashedPassword)
                                .addOnSuccessListener(unused -> {
                                    showToast("Password reset successfully!");
                                    navigateToSignIn();
                                })
                                .addOnFailureListener(e -> showToast("Failed to reset password."));
                    } else {
                        showToast("Failed to find user. Please try again.");
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonReset.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonReset.setVisibility(View.VISIBLE);
        }
    }
}
