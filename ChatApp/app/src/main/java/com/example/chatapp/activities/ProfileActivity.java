package com.example.chatapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;

public class ProfileActivity extends AppCompatActivity {
    TextView textUsername, textEmail, textBirthdate;
    Button btnUpdateProfile;
    ImageButton btnBack;
    PreferenceManager preferenceManager;
    ImageView imageProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_profile_user);

        // Khởi tạo PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        textUsername = findViewById(R.id.textName);
        textEmail = findViewById(R.id.textEmail);
        textBirthdate = findViewById(R.id.textBirthdate);
        btnUpdateProfile = findViewById(R.id.buttonUpdateProfile);
        btnBack = findViewById(R.id.buttonBack);
        imageProfile = findViewById(R.id.imageProfile);

       getCurrentUserDetails();

        // Thiết lập sự kiện click cho nút Update Profile
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
                // Chuyển đến UpdateProfileActivity
                startActivity(intent);
                getCurrentUserDetails();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getCurrentUserDetails() {
        String name = preferenceManager.getString(Constants.KEY_NAME); // Tên người dùng
        String email = preferenceManager.getString(Constants.KEY_EMAIL); // Email người dùng
        String birthdate = preferenceManager.getString(Constants.KEY_BIRTHDATE); // Ngày sinh người dùng

        String userAvatarUrl = preferenceManager.getString(Constants.KEY_IMAGE);
        Glide.with(this)
                .load(userAvatarUrl)
                .placeholder(R.drawable.ic_default_profile_foreground) // Hình ảnh placeholder khi đang tải ảnh
                .into(imageProfile); // ImageView để hiển thị ảnh


        // Hiển thị thông tin lên các TextView
        textUsername.setText(name != null ? name : "N/A");
        textEmail.setText(email != null ? email : "N/A");
        textBirthdate.setText(birthdate != null ? birthdate : "N/A");
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentUserDetails();
    }

}



