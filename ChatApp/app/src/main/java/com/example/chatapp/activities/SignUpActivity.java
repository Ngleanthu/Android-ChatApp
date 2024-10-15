package com.example.chatapp.activities;

import android.app.Activity;
import android.os.Bundle;
import com.example.chatapp.databinding.ActivitySignUpBinding;

public class SignUpActivity extends Activity {

    private ActivitySignUpBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }
    private void setListeners(){
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
    }
}