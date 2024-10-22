package com.example.chatapp.utils;

import android.content.ContentProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences;
    public PreferenceManager(Context context){
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }
    public void putBoolean(String key, Boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    public Boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key, false);
    }
    public void putString(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
    public String getString(String key){
        return sharedPreferences.getString(key, null);
    }
    public void clear(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }



    // Lưu đường dẫn hình ảnh vào SharedPreferences
    public void putImagePath(String key, String path){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, path);
        editor.apply();
    }

    // Lấy đường dẫn hình ảnh từ SharedPreferences
    public String getImagePath(String key){
        return sharedPreferences.getString(key, null);
    }

    // Lưu Bitmap vào bộ nhớ cache và trả về đường dẫn
    public String saveBitmapToCache(Bitmap bitmap, Context context){
        File cacheDir = context.getCacheDir();
        File imageFile = new File(cacheDir, "profile_image.png");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
            return imageFile.getAbsolutePath();  // Trả về đường dẫn của file
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Lấy Bitmap từ đường dẫn
    public Bitmap getBitmapFromPath(String key){
        String path = getImagePath(key);
        if (path != null) {
            return BitmapFactory.decodeFile(path);  // Giải mã file thành Bitmap
        }
        return null;
    }
}
