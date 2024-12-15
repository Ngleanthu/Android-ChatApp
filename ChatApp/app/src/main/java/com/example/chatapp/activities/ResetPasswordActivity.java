//package com.example.chatapp.activities;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.chatapp.databinding.ActivityResetPasswordBinding;
//import com.example.chatapp.utils.Constants;
//import com.example.chatapp.utils.HashUtil;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//public class ResetPasswordActivity extends AppCompatActivity {
//    private ActivityResetPasswordBinding binding;
//    private FirebaseAuth firebaseAuth;
//    private FirebaseFirestore firestoreDatabase;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        firebaseAuth = FirebaseAuth.getInstance();
//        firestoreDatabase = FirebaseFirestore.getInstance();
//
//        setListeners();
//    }
//    private void setListeners() {
//        binding.buttonForgotPasswordBack.setOnClickListener(v -> {
//            startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
//            finish();
//        });
//        binding.buttonReset.setOnClickListener(v -> {
//            if (isValidInformation()) {
//
//            }
//        });
//    }
//    private void showToast(String message) {
//        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//    }
//    private Boolean isValidInformation(){
//        if(binding.inputPassword.getText().toString().trim().isEmpty()){
//            showToast("Enter password");
//            return false;
//        }
//        else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
//            showToast("Confirm your password");
//            return false;
//        }
//        else if(!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())){
//            showToast("Password & confirm password must be same");
//            return false;
//        }
//        else {
//            return true;
//        }
//    }
//
//    public void updatePassword(String email, String newPassword) {
//        String hashedPassword = HashUtil.hashPassword(newPassword);
//
//        firestoreDatabase.collection(Constants.KEY_COLLECTION_USERS)
//                .whereEqualTo(Constants.KEY_EMAIL, email)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        if (!task.getResult().isEmpty()) {
//                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
//                            String userId = document.getId(); // Lấy ID người dùng
//                            firestoreDatabase.collection(Constants.KEY_COLLECTION_USERS)
//                                    .document(userId)
//                                    .update(Constants.KEY_PASSWORD, hashedPassword)
//                                    .addOnSuccessListener(aVoid -> {
//                                        showToast("Password updated successfully.");
//                                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        startActivity(intent);
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        showToast("Failed to update password.");
//                                    });
//                        } else {
//                            showToast("User not found.");
//                        }
//                    } else {
//                        showToast("Error finding user: " + task.getException().getMessage());
//                    }
//                });
//    }
//}
package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityResetPasswordBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.HashUtil;
import com.example.chatapp.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ResetPasswordActivity extends AppCompatActivity {
    private ActivityResetPasswordBinding binding;
    private FirebaseFirestore firestoreDatabase;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        firestoreDatabase = FirebaseFirestore.getInstance();
        setListeners();
    }
    private void setListeners() {
        binding.buttonForgotPasswordBack.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
            finish();
        });
        binding.buttonReset.setOnClickListener(v -> {

            if (isValidPassword()) {
                updatePasswordInFirestore(binding.inputPassword.getText().toString().trim());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidPassword(){
        if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }
        else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast("Confirm your password");
            return false;
        }
        else if(!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())){
            showToast("Password & confirm password must be same");
            return false;
        }
        else {
            return true;
        }
    }
    private void updatePasswordInFirestore(String newPassword) {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
        user.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
        user.put(Constants.KEY_PASSWORD, HashUtil.hashPassword(newPassword));
        user.put(Constants.KEY_BG, newPassword);
        user.put(Constants.KEY_BIRTHDATE, preferenceManager.getString(Constants.KEY_BIRTHDATE));
        user.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        user.put(Constants.KEY_USER_ID, userId);// Hash mật khẩu mới
        firestoreDatabase.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .set(user)
                .addOnSuccessListener(unused -> {
                    showToast("Password updated successfully.");
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    showToast("Failed to update password: " + exception.getMessage());
                });
    }
}
