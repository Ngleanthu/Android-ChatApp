package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityResetPasswordBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.HashUtil;
import com.google.firebase.firestore.FirebaseFirestore;

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
        String email = getIntent().getStringExtra("EMAIL");
        showToast("Email: " + email);
        setListeners(email);
    }

    private void setListeners(String email) {
        binding.buttonReset.setOnClickListener(v -> {
            String newPassword = binding.inputPassword.getText().toString().trim();
            if (isValidPassword(newPassword)) {
                resetPassword(email, newPassword);
            }
        });
        binding.buttonResetPasswordBack.setOnClickListener(v -> {
            finish();
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
        showToast("Email: " + email);
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
