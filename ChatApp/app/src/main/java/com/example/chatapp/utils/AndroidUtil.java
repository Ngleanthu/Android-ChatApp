package com.example.chatapp.utils;

import android.content.Intent;

import com.example.chatapp.models.UserModel;

public class AndroidUtil {
    public static void passUserModelAsIntent(Intent intent, UserModel userModel){
        intent.putExtra("name", userModel.getName());
        intent.putExtra("email", userModel.getEmail());
        intent.putExtra("userId", userModel.getUserId());
        intent.putExtra("fcmToken", userModel.getFcmToken());
    }
    public static UserModel getUserModelFromIntent(Intent intent) {
    UserModel userModel = new UserModel();
    userModel.setName(intent.getStringExtra("name"));
    userModel.setEmail(intent.getStringExtra("email"));
    userModel.setUserId(intent.getStringExtra("userId"));
    userModel.setFcmToken(intent.getStringExtra("fcmToken"));

    return userModel;
    }
}
