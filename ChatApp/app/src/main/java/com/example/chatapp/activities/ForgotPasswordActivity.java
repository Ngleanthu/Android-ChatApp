package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityForgotPasswordBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDatabase;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDatabase = FirebaseFirestore.getInstance();

        setListeners();
    }

    private void setListeners() {
        binding.buttonForgotPasswordBack.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        });

        binding.buttonReset.setOnClickListener(v -> {
            String email = binding.inputEmail.getText().toString().trim();
            if (isValidEmail(email)) {
                checkEmailExistence(email);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Kiểm tra xem email có hợp lệ không
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

    // Kiểm tra email có tồn tại trong cơ sở dữ liệu không
    private void checkEmailExistence(String email) {
        firestoreDatabase.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // Nếu email không tồn tại trong cơ sở dữ liệu
                            showToast("Email is not registered.");
                        } else {
                            // Nếu email đã tồn tại, gửi email xác thực
                            getPasswordByEmail(email);
                        }
                    } else {
                        showToast("Error checking email: " + task.getException().getMessage());
                    }
                });
    }
    private void getPasswordByEmail(String email) {
        firestoreDatabase.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String password = document.getString(Constants.KEY_BG);

                        String documentId = document.getId(); // Lấy document ID

                        if (password != null) {
                            // Lưu tất cả thông tin người dùng vào PreferenceManager
                            preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                            preferenceManager.putString(Constants.KEY_USER_ID, document.getString(Constants.KEY_USER_ID));
                            preferenceManager.putString(Constants.KEY_PASSWORD, document.getString(Constants.KEY_PASSWORD));
                            preferenceManager.putString(Constants.KEY_BG, document.getString(Constants.KEY_BG));
                            preferenceManager.putString(Constants.KEY_NAME, document.getString(Constants.KEY_NAME));
                            preferenceManager.putString(Constants.KEY_BIRTHDATE, document.getString(Constants.KEY_BIRTHDATE));
                            preferenceManager.putString(Constants.KEY_IMAGE, document.getString(Constants.KEY_IMAGE));
                            showToast("ua la gi v tr d bt lun");
                            showToast(password);
                            showToast(email);
                            firebaseAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            firebaseAuth.signOut();
                                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                            if (currentUser != null) {
                                                currentUser.delete()
                                                        .addOnCompleteListener(task2 -> {
                                                            if (task2.isSuccessful()) {
                                                                // Gửi lại email xác thực cho người dùng
                                                                sendEmailVerification(email);
                                                            } else {
                                                                showToast("Error deleting account: " + task2.getException().getMessage());
                                                            }
                                                        });
                                            }
                                        } else {
                                            showToast("Login failed: " + task1.getException().getMessage());
                                        }
                                    });
                        } else {
                            showToast("Password not found for this email.");
                        }
                    } else {
                        showToast("No user found with this email.");
                    }
                });
    }

    private void sendEmailVerification(String email) {
        // Gửi email xác thực
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Verification email sent. Please check your inbox.");
                        // Chuyển qua ResetPasswordActivity để người dùng nhập mật khẩu mới
                        Intent intent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                        startActivity(intent);
                    } else {
                        showToast("Failed to send verification email: " + task.getException().getMessage());
                    }
                });
    }




}