package com.example.chatapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.chatapp.databinding.ActivitySignInBinding;
import com.google.firebase.FirebaseApp;

import com.google.firebase.firestore.FirebaseFirestore;


public class SignInActivity extends Activity {

    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }


    private void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
    }


}
