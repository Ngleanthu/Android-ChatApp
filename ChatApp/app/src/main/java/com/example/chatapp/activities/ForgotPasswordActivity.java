package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityForgotPasswordBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {

        binding.buttonForgotPasswordBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        binding.buttonReset.setOnClickListener(v -> {
            String email = binding.inputEmail.getText().toString().trim();
            if (isValidEmail(email)) {
                handlePasswordReset();
            }
        });

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidEmail(String email) {
        if (email.isEmpty()) {
            showToast("Please enter an email.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address.");
            return false;
        }
        return true;
    }

    private void handlePasswordReset() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String email = binding.inputEmail.getText().toString().trim();

        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        // Email tồn tại trong hệ thống
                        showToast("Email verified. Proceed to reset your password.");
                        Intent intent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                        intent.putExtra("EMAIL", email);
                        startActivity(intent);
                        loading(false);
                    } else {
                        // Email không tồn tại
                        showToast("No account found with this email.");
                        loading(false);
                    }
                });
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
