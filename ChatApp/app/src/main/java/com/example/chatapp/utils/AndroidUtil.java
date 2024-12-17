package com.example.chatapp.utils;

import android.content.Context;
import android.content.Intent;
import com.example.chatapp.models.UserModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static boolean containsLink(String message) {
        String urlRegex = "((http|https)://|www\\.)[a-zA-Z0-9\\-._~:/?#\\\\@!$&'()*+,;=]+";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(message);
        return matcher.find();
    }




}
