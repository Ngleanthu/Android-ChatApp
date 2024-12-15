package com.example.chatapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.databinding.ActivitySignUpBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.HashUtil;
import com.example.chatapp.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;


public class SignUpActivity extends Activity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;

    private FirebaseAuth auth;
    private FirebaseFirestore database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }
    private void setListeners(){
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

//    private void signUp(){
//        loading(true);
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        String userId = database.collection(Constants.KEY_COLLECTION_USERS).document().getId();
//        HashMap<String, Object> user = new HashMap<>();
//        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
//        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
//        user.put(Constants.KEY_PASSWORD, HashUtil.hashPassword(binding.inputPassword.getText().toString()));
//        user.put(Constants.KEY_BIRTHDATE, binding.inputBirthdate.getText().toString());
//        user.put(Constants.KEY_IMAGE, "");
//        user.put(Constants.KEY_USER_ID, userId);
//
//        database.collection(Constants.KEY_COLLECTION_USERS)
//                .document(userId)
//                .set(user)
//                .addOnSuccessListener(documentReference -> {
//                    loading(false);
//                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
//
//                    preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
//                    preferenceManager.putString(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
//                    preferenceManager.putString(Constants.KEY_USER_ID, userId);
//                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
//                    preferenceManager.putString(Constants.KEY_BIRTHDATE, binding.inputBirthdate.getText().toString());
//                    preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString()); // Lưu email
//                    preferenceManager.putString(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString()); // Lưu password
//
//                    preferenceManager.putString(Constants.KEY_IMAGE, "image"); // Lưu password
//                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//
//                })
//                .addOnFailureListener(exception -> {
//                    loading(false);
//                    showToast(exception.getMessage());
//                });
//    }

    private void signUp() {
        loading(true);
        String email = binding.inputEmail.getText().toString();
        String password = binding.inputPassword.getText().toString();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            showToast("Verification email sent! Please verify before logging in.");
                                        } else {
                                            showToast("Failed to send verification email: " + emailTask.getException().getMessage());
                                        }
                                    });
                        }
                    } else {
                        showToast("Sign Up failed: " + task.getException().getMessage());
                    }
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null) {
            firebaseUser.reload().addOnCompleteListener(task -> {
                if (firebaseUser.isEmailVerified()) {
                    // Email đã xác minh
                    showToast("Email verified! Saving user data and redirecting to Main Activity...");
                    String userId = database.collection(Constants.KEY_COLLECTION_USERS).document().getId();
                    HashMap<String, Object> user = new HashMap<>();
                    user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
                    user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                    user.put(Constants.KEY_BG, binding.inputPassword.getText().toString());
                    user.put(Constants.KEY_PASSWORD, HashUtil.hashPassword(binding.inputPassword.getText().toString()));
                    user.put(Constants.KEY_BIRTHDATE, binding.inputBirthdate.getText().toString());
                    user.put(Constants.KEY_IMAGE, "");
                    user.put(Constants.KEY_USER_ID, userId);
                    // Tạo userId từ Firestore

                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener(documentReference -> {
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);

                                preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                                preferenceManager.putString(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
                                preferenceManager.putString(Constants.KEY_BG, binding.inputPassword.getText().toString());
                                preferenceManager.putString(Constants.KEY_USER_ID, userId);
                                preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                                preferenceManager.putString(Constants.KEY_BIRTHDATE, binding.inputBirthdate.getText().toString());

                                preferenceManager.putString(Constants.KEY_IMAGE, "image"); // Lưu password
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            })
                            .addOnFailureListener(exception -> {
                                showToast("Failed to save user data: " + exception.getMessage());
                            });
                }
            });
        }
    }


    private boolean isValidBirthdate(String birthdate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(birthdate);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    private Boolean isValidSignUpDetails(){
        if(binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Enter name");
            return false;
        }
        else if(binding.inputBirthdate.getText().toString().trim().isEmpty()){
            showToast("Enter birthdate");
            return false;
        }
        else if(!isValidBirthdate(binding.inputBirthdate.getText().toString())){
            showToast("Enter valid birthdate in format dd/MM/yyyy");
            return false;
        }
        else if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        }
        else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }
        else if(binding.inputComfirmPassword.getText().toString().trim().isEmpty()){
            showToast("Confirm your password");
            return false;
        }
        else if(!binding.inputPassword.getText().toString().equals(binding.inputComfirmPassword.getText().toString())){
            showToast("Password & confirm password must be same");
            return false;
        }
        else {
            return true;
        }
    }

    private void loading(Boolean isLoading){
        if(isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}