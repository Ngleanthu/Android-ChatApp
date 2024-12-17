package com.example.chatapp.activities;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.utils.MediaUtil;

public class FullScreenMediaActivity extends AppCompatActivity {

    private String videoUrl;
    private String imageUrl;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_media);
        videoUrl = getIntent().getStringExtra("VIDEO_URL");
        imageUrl = getIntent().getStringExtra("IMAGE_URL");
        fileName = getIntent().getStringExtra("FILE_NAME");

        if (videoUrl != null && !videoUrl.isEmpty()) {
            LinearLayout playerLayout = findViewById(R.id.player_full_screen_layout);
            MediaUtil.addVideoToLayout(playerLayout, videoUrl, fileName, this, true);
        }

        if(imageUrl != null && !imageUrl.isEmpty()){
            LinearLayout imageLayout = findViewById(R.id.player_full_screen_layout);
            MediaUtil.addImageToLayout(imageLayout, imageUrl, fileName, this, true);
        }
    }
}