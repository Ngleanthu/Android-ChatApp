package com.example.chatapp.activities;

import android.app.Activity;
import android.os.Bundle;

import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;


public class MainActivity extends Activity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
    }
    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
    }
}