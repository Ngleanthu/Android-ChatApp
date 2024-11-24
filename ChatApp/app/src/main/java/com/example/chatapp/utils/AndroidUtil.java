package com.example.chatapp.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.models.UserModel;

public class AndroidUtil {
    public static void passUserModelAsIntent(Intent intent, UserModel userModel){
        intent.putExtra("name", userModel.getName());
        intent.putExtra("email", userModel.getEmail());
        intent.putExtra("userId", userModel.getUserId());
        intent.putExtra("fcmToken", userModel.getFcmToken());
        intent.putExtra("image", userModel.getImage());
    }
    public static UserModel getUserModelFromIntent(Intent intent) {
    UserModel userModel = new UserModel();
    userModel.setName(intent.getStringExtra("name"));
    userModel.setEmail(intent.getStringExtra("email"));
    userModel.setUserId(intent.getStringExtra("userId"));
    userModel.setFcmToken(intent.getStringExtra("fcmToken"));
    userModel.setImage(intent.getStringExtra("image"));

    return userModel;
    }


}
