package com.example.chatapp.activities;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    ChatFragment chatFragment;
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        loadUserDetails();

        chatFragment = new ChatFragment();

        // Hiển thị ChatFragment mặc định
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, chatFragment)
                    .commit();
        }

        getToken();
        setListeners();

        if (getIntent().getBooleanExtra("signOut", false)) {
            signOut();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        boolean isUpdateRun = false;

        if (!isUpdateRun) {
            db.collection("chats")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("chats").document(document.getId())
                                        .update("type", "text") // Giá trị mặc định
                                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Document updated: " + document.getId()))
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error updating document", e));
                            }
                            Log.d("Firestore", "Update complete!");
                        } else {
                            Log.e("Firestore", "Error fetching documents", task.getException());
                        }
                    });
            isUpdateRun = true;
        }



    }


    private void loadUserDetails(){

        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        String userAvatarUrl = preferenceManager.getString(Constants.KEY_IMAGE);
        Glide.with(this)
                .load(userAvatarUrl)
                .placeholder(R.drawable.ic_default_profile_foreground) // Hình ảnh placeholder khi đang tải ảnh
                .into(binding.imageProfile); // ImageView để hiển thị ảnh

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


    public void signOut(){
        showToast("Signing out...");
        FirebaseAuth.getInstance().signOut();

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            FirebaseAuth.getInstance().signOut();
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


    @Override
    protected void onResume() {
        super.onResume();
        loadUserDetails();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        loadUserDetails();
    }

}