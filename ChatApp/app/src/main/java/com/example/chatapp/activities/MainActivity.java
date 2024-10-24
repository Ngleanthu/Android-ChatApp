package com.example.chatapp.activities;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.ImageUtil;
import com.example.chatapp.utils.PreferenceManager;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOError;
import java.io.IOException;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    public static final int MY_REQUEST_CODE = 100;


//    ChatFragment chatFragment;

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();

//        chatFragment = new ChatFragment();
//
//        // Hiển thị ChatFragment mặc định
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, chatFragment)
//                    .commit();
//        }

        getToken();
        setListeners();

//        ImageUtil.setImageProfileFromPreferences(this, binding.imageProfile, preferenceManager);

    }


    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        String userAvatarUrl = preferenceManager.getString(Constants.KEY_IMAGE);
        if (userAvatarUrl != null) {
            // Thiết lập hình ảnh đại diện nếu có URL
            Glide.with(this)
                    .load(userAvatarUrl)
                    .placeholder(R.mipmap.ic_default_profile) // Hình ảnh mặc định
                    .into(binding.imageProfile);
        }
    }


    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }


    private void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> showToast("Token updated successfully"))
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }


    private void signOut(){
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        }).addOnFailureListener(e -> showToast("Unable to sign out"));

    }
    private void setListeners(){
        binding.imageSignOut.setOnClickListener(x -> signOut());
        binding.tabNewChat.setOnClickListener(v -> {
            // Chuyển hướng sang trang SearchActivity
            Intent intent = new Intent(MainActivity.this, SearchUserActivity.class);
            startActivity(intent);
        });

        binding.imageProfile.setOnClickListener(v -> {
            // Chuyển hướng sang trang ProfileActivity
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }


//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        ImageUtil.setImageProfileFromPreferences(this, binding.imageProfile, preferenceManager);
//    }

}