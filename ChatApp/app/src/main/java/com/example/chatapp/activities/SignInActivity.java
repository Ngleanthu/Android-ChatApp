package com.example.chatapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.databinding.ActivitySignInBinding;
import com.example.chatapp.models.UserModel;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.FirebaseUtil;
import com.example.chatapp.utils.PreferenceManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignInActivity extends Activity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().getExtras() != null){

            // from notification
            String userId = getIntent().getExtras().getString(Constants.KEY_USER_ID);
            FirebaseUtil.allUserCollectionReference().document(userId).get().addOnCompleteListener(
                    task -> {

                        UserModel model = task.getResult().toObject(UserModel.class);
                        //navigate to chat room
                        Intent intent = new Intent(this, ChatActivity.class);
                        AndroidUtil.passUserModelAsIntent(intent,model);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
            );

        }else{
        preferenceManager = new PreferenceManager(getApplicationContext());

        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        }
    }


    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()){
                signIn();
            }
        });
    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                        preferenceManager.putString(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_BIRTHDATE, documentSnapshot.getString(Constants.KEY_BIRTHDATE));
                        preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString()); // Lưu email
                        preferenceManager.putString(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString()); // Lưu password

                        // Lấy URL ảnh từ Firestore và lưu vào PreferenceManager
                        String profileImageUrl = documentSnapshot.getString(Constants.KEY_IMAGE);
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            // Lưu URL của ảnh vào PreferenceManager
                            preferenceManager.putString(Constants.KEY_IMAGE, profileImageUrl);
                        }

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }
                    else{
                        loading(false);
                        showToast("Unable to sign in");
                    }
                });
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private Boolean isValidSignInDetails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else {
            return true;
        }
    }
}
