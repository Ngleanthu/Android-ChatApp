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
    }
    public static UserModel getUserModelFromIntent(Intent intent) {
    UserModel userModel = new UserModel();
    userModel.setName(intent.getStringExtra("name"));
    userModel.setEmail(intent.getStringExtra("email"));
    userModel.setUserId(intent.getStringExtra("userId"));
    return userModel;
    }

    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView){
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }

}
