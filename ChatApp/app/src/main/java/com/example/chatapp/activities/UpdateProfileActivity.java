package com.example.chatapp.activities;

import static com.example.chatapp.activities.MainActivity.MY_REQUEST_CODE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import com.example.chatapp.utils.ImageUtil;


public class UpdateProfileActivity extends AppCompatActivity {
    private EditText profileName, profileBirthdate, profileNewPassword, profileConfirmPassword, profileRecentPassword;
    private ImageButton btnBack;
    private PreferenceManager preferenceManager;
    private RoundedImageView imageProfile;
    private MaterialButton buttonUpdateProfile;
    private TextView profileEmail;

    private Bitmap selectedBitmap; // Khai báo biến để lưu Bitmap được chọn

    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        // Khởi tạo PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        // Khởi tạo các view
        profileName = findViewById(R.id.profileName);
        profileBirthdate = findViewById(R.id.profileBirthdate);
        profileNewPassword = findViewById(R.id.profileNewPassword);
        profileConfirmPassword = findViewById(R.id.profileConfirmPassword);
        profileRecentPassword = findViewById(R.id.profileRecentPassword);
        btnBack = findViewById(R.id.buttonBack);
        imageProfile = findViewById(R.id.imageProfile);
        buttonUpdateProfile = findViewById(R.id.buttonUpdateProfile);
        profileEmail = findViewById(R.id.profileEmail);

        getInfoUser();
        initListener();
        setImageProfileFromPreferences();


        // Khởi tạo ActivityResultLauncher
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            if (uri != null) {
                                try {
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                    imageProfile.setImageBitmap(bitmap);
                                    // Lưu đường dẫn hình ảnh hoặc thực hiện xử lý khác nếu cần
                                    String imagePath = preferenceManager.saveBitmapToCache(bitmap, getApplicationContext());
                                    preferenceManager.putImagePath("profile_image_path", imagePath); // Lưu đường dẫn vào SharedPreferences
                                    Toast.makeText(getApplicationContext(), "Image saved to Preferences", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
        );



    }

    private  void getInfoUser(){
        // Lấy dữ liệu từ PreferenceManager
        profileName.setText(preferenceManager.getString(Constants.KEY_NAME));
        profileBirthdate.setText(preferenceManager.getString(Constants.KEY_BIRTHDATE));
        profileEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        String userAvatarUrl = preferenceManager.getString(Constants.KEY_IMAGE);
        if (userAvatarUrl != null) {
            // Thiết lập hình ảnh đại diện nếu có URL
            Glide.with(this)
                    .load(userAvatarUrl)
                    .placeholder(R.mipmap.ic_default_profile) // Hình ảnh mặc định
                    .into(imageProfile);
        }
    }

    private void initListener(){
        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRequestPermission();
            }
        });

        // Thiết lập sự kiện click cho nút Back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Thiết lập sự kiện click cho nút Save change
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy thông tin mật khẩu từ các ô nhập liệu
                String recentPassword = profileRecentPassword.getText().toString();
                String newPassword = profileNewPassword.getText().toString();
                String confirmPassword = profileConfirmPassword.getText().toString();

                // Kiểm tra nếu ảnh đã được chọn
                if (selectedBitmap != null) {
                    // Nếu có ảnh, tải ảnh lên Firebase
                    uploadImageToFirebaseStorage(selectedBitmap);
                }

            }
        });
    }

    private void onClickRequestPermission() {

        // từ android 6 trở xuống thì không cần request permission
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            openGallery();
            return;
        }

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            openGallery();
        }else{
            String [] permission = {android.Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permission, MY_REQUEST_CODE);
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MainActivity.MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            Bitmap bitmap = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bitmap != null) {
                selectedBitmap = bitmap; // Lưu Bitmap vào biến instance
                setBitmapImageView(bitmap); // Hiển thị ảnh trên ImageView
            }
        }
    }

    public void setBitmapImageView(Bitmap bitmap) {
        if (imageProfile != null) {
            imageProfile.setImageBitmap(bitmap); // Thiết lập hình ảnh cho ImageView
        }
    }

    public void setImageProfileFromPreferences() {
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        String imagePath = preferenceManager.getImagePath("profile_image_path"); // Lấy đường dẫn từ SharedPreferences

        if (imagePath != null) {
            // Giải mã đường dẫn thành Bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            if (bitmap != null) {
                // Gọi phương thức để thiết lập Bitmap vào ImageView
                setBitmapImageView(bitmap);
            } else {
                // Xử lý khi không thể giải mã hình ảnh
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Xử lý khi không có đường dẫn hình ảnh
            Toast.makeText(this, "No image found in preferences", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebaseStorage(Bitmap bitmap) {
        // Thêm logic để tải ảnh lên Firebase Storage
        // Tạo một tên file duy nhất cho ảnh (ví dụ: dựa trên thời gian)
        String fileName = "profile_images/" + System.currentTimeMillis() + ".jpg";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(fileName);

        // Chuyển đổi Bitmap thành byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Tải lên thành công
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                // Cập nhật URL hình ảnh trong Firestore
                updateImageUrlInFirestore(imageUrl);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(UpdateProfileActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateImageUrlInFirestore(String imageUrl) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_IMAGE, imageUrl); // Cập nhật URL hình ảnh

        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(UpdateProfileActivity.this, "Image URL updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UpdateProfileActivity.this, "Failed to update image URL", Toast.LENGTH_SHORT).show();
                });
    }

//    private void setImageProfileFromPreferences() {
//        ImageUtil.setImageProfileFromPreferences(this, imageProfile, preferenceManager);
//    }
//
//
//    private void onClickUpdateProfile(){
//
//    }

}
