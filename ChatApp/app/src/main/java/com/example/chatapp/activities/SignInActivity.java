package com.example.chatapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.chatapp.databinding.ActivitySignInBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

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

        binding.buttonSignIn.setOnClickListener(v -> addDataToFirestore());
    }

    private void addDataToFirestore(){

        // Tạo đối tượng Firestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Tạo dữ liệu muốn thêm
        HashMap<String, Object> data = new HashMap<>();
        data.put("first_name", "Anthu");
        data.put("last_name", "Ngle");

        // Thêm dữ liệu vào Firestore
        database.collection("users")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(exception -> {
                    Log.e("FirestoreError", "Error adding document", exception);
                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
//        FirebaseFirestore.getInstance().collection("test").add(data);
    }
}
