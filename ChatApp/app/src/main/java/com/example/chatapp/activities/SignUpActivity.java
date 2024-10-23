package com.example.chatapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.databinding.ActivitySignUpBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;


public class SignUpActivity extends Activity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
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

    private void signUp(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = database.collection(Constants.KEY_COLLECTION_USERS).document().getId();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_BIRTHDATE, binding.inputBirthdate.getText().toString());

        user.put(Constants.KEY_IMAGE, "");

        user.put(Constants.KEY_USER_ID, userId);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .set(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                  
                    preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                    preferenceManager.putString(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());

                    preferenceManager.putString(Constants.KEY_USER_ID, userId);
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_BIRTHDATE, binding.inputBirthdate.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString()); // Lưu email
                    preferenceManager.putString(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString()); // Lưu password
                    preferenceManager.putString(Constants.KEY_IMAGE, ""); 
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
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