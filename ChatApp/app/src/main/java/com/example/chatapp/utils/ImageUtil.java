package com.example.chatapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageUtil {

    public static void setImageProfileFromPreferences(Context context, ImageView imageView, PreferenceManager preferenceManager) {
        String imagePath = preferenceManager.getImagePath("profile_image_path"); // Lấy đường dẫn từ SharedPreferences

        if (imagePath != null) {
            // Giải mã đường dẫn thành Bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            if (bitmap != null) {
                // Thiết lập hình ảnh cho ImageView
                imageView.setImageBitmap(bitmap);
            } else {
                // Xử lý khi không thể giải mã hình ảnh
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Xử lý khi không có đường dẫn hình ảnh
            Toast.makeText(context, "No image found in preferences", Toast.LENGTH_SHORT).show();
        }
    }
}
