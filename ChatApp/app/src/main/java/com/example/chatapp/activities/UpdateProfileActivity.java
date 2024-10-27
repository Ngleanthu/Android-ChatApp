package com.example.chatapp.activities;

import static com.example.chatapp.activities.MainActivity.MY_REQUEST_CODE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableRow;
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
import com.makeramen.roundedimageview.RoundedImageView;
import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class UpdateProfileActivity extends AppCompatActivity {
    private EditText profileName, profileBirthdate, profileNewPassword, profileConfirmPassword, profileRecentPassword;
    private ImageButton btnBack;
    private PreferenceManager preferenceManager;
    private RoundedImageView imageProfile;
    private MaterialButton buttonUpdateProfile;
    private ProgressBar progressBar;
    private TextView profileEmail;
    private TableRow confirmPasswordRow;
    private TableRow recentPasswordRow;

    // code mới 24/10 ===========
    private Uri imageUri;
    private String userId;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToast("onCreate");
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
        progressBar = findViewById(R.id.progressBar);
        profileEmail = findViewById(R.id.profileEmail);
        confirmPasswordRow = findViewById(R.id.confirmPasswordRow);
        recentPasswordRow = findViewById(R.id.recentPasswordRow);


        // code mới ===========
        userId = preferenceManager.getString(Constants.KEY_USER_ID);
        //=================

        getInfoUser();
        initListener();


        // Khởi tạo ActivityResultLauncher
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            if (uri != null) {
                                // Hiển thị ảnh từ Uri trực tiếp lên ImageView
                                imageProfile.setImageURI(uri);
                                // lưu lại uri
                                imageUri = uri;

                                // Lưu Uri vào SharedPreferences (hoặc nơi nào khác)
                                //preferenceManager.putString(Constants.KEY_IMAGE, uri.toString());

                                // Toast.makeText(getApplicationContext(), "Image URI saved to Preferences", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );


        profileNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() >0){
                    confirmPasswordRow.setVisibility(View.VISIBLE);
                    recentPasswordRow.setVisibility(View.VISIBLE);
                }else{
                    confirmPasswordRow.setVisibility(View.GONE);
                    recentPasswordRow.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void getInfoUser() {
        showToast("vào hàm getUserinfo");
        // Lấy dữ liệu từ PreferenceManager

        profileName.setText(preferenceManager.getString(Constants.KEY_NAME));
        profileBirthdate.setText(preferenceManager.getString(Constants.KEY_BIRTHDATE));
        profileEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        String userAvatarUrl = preferenceManager.getString(Constants.KEY_IMAGE);
        if (userAvatarUrl != null) {
            showToast("lấy được url image");
            // Thiết lập hình ảnh đại diện nếu có URL
            Glide.with(this)
                    .load(userAvatarUrl)
                    .placeholder(R.drawable.ic_default_profile_foreground) // Hình ảnh placeholder khi đang tải ảnh
                    .into(imageProfile); // ImageView để hiển thị ảnh
        }

    }

    private void initListener() {
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

        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Click vào button savechange", Toast.LENGTH_SHORT).show();

                if(isValidProfileDetails()){

                    String name = profileName.getText().toString();
                    String birthdate = profileBirthdate.getText().toString();
                    String newPassword = profileNewPassword.getText().toString();

                    loading(true);
                    if (imageUri != null) {
                        uploadImageAndSaveToFirestore(name, birthdate, newPassword);
                    } else {
                        updateUserInFirestore(name, birthdate, null, newPassword);
                    }
                }
            }
        });
    }

    private void onClickRequestPermission() {

        // từ android 6 trở xuống thì không cần request permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            String[] permission = {android.Manifest.permission.READ_EXTERNAL_STORAGE};
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

    // code ngày 24/10 ===============================================

    private void uploadImageAndSaveToFirestore(String name, String birthdate, String newPassword) {

        // Firebase Storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        Toast.makeText(getApplicationContext(), " vào hàm  uploadImageAndSaveToFirestore", Toast.LENGTH_SHORT).show();


        // Kiểm tra giá trị của imageUri
        if (imageUri == null) {
            Log.e("Upload", "Image URI is null");
            Toast.makeText(getApplicationContext(), "Image URI is null", Toast.LENGTH_SHORT).show();
            return; // Ngừng thực hiện nếu imageUri là null
        } else {
            Log.d("Upload", "Image URI: " + imageUri.toString());
        }
        // Đường dẫn cho file ảnh trong Firebase Storage
        StorageReference imageRef = storageReference.child("users/" + preferenceManager.getString(Constants.KEY_USER_ID) + "/profile.jpg");
        // Tải ảnh từ imageUri lên Firebase Storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        showToast("vào hàm uploadImageAndSaveToFirestore");
                        // cập nhật thông tin người dùng vào Firestore
                        updateUserInFirestore(name, birthdate, imageUrl, newPassword);
                    });
                })
                .addOnFailureListener(e -> {
                    // Xử lý khi tải ảnh thất bại
                    Log.e("Upload", "Upload failed", e);
                    Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInFirestore(String name, String birthdate, String imageUrl, String newPassword) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        // Kiểm tra userId đã được lấy đúng chưa
        if (userId == null || userId.isEmpty()) {
            Log.e("Firestore", "User ID is null or empty");
            return; // Ngừng thực thi nếu userId rỗng
        }
        // Tạo một bản ghi mới chứa thông tin cập nhật
        Map<String, Object> updatedUser = new HashMap<>();
        if (!name.isEmpty()) {
            updatedUser.put("name", name);
        }
        if (!birthdate.isEmpty()) {
            updatedUser.put("birthdate", birthdate);
        }
        if (!newPassword.isEmpty()) {
            updatedUser.put("password", newPassword);
        }

        if (imageUrl != null) {
            updatedUser.put("image", imageUrl); // Cập nhật URL ảnh nếu có
        }

        // Cập nhật thông tin người dùng trong Firestore
        userRef.update(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    // Xử lý khi cập nhật thành công
                    Log.d("Firestore", "User profile updated successfully");
                    Toast.makeText(getApplicationContext(), "User profile updated successfully", Toast.LENGTH_SHORT).show();

                    updateInfoUserToPreferenceManger(name, birthdate, imageUrl ,newPassword);
                    finish(); // Đóng Activity hiện tại, trở về Activity trước đó
                })
                .addOnFailureListener(e -> {
                    // Xử lý khi cập nhật thất bại
                    Log.e("Firestore", "Error updating profile", e);
                    Toast.makeText(getApplicationContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
                });

        // Cập nhật mật khẩu mới (nếu có) mà không cần xác thực lại
//        if (!newPassword.isEmpty()) {
//            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//            if (user != null) {
//                // Cập nhật mật khẩu
//                user.updatePassword(newPassword)
//                        .addOnCompleteListener(task -> {
//                            if (task.isSuccessful()) {
//                                Log.d("PasswordUpdate", "Password updated successfully");
//                            } else {
//                                Log.e("PasswordUpdate", "Password update failed", task.getException());
//                            }
//                        });
//            }
//        }
    }

    private void updateInfoUserToPreferenceManger(String name, String birthdate,String imageUrl, String newPassword) {

        showToast("vào hàm cập nhật preference");
        if (!name.isEmpty()) {
            preferenceManager.putString(Constants.KEY_NAME, name);
            showToast("cập nhật name");
        }
        if (!birthdate.isEmpty()) {
            preferenceManager.putString(Constants.KEY_BIRTHDATE, birthdate);
            showToast("cập nhật birthday");
        }
        if (!newPassword.isEmpty()) {
            preferenceManager.putString(Constants.KEY_PASSWORD, newPassword);
            showToast("cập nhật password");
        }

        if (! imageUrl.isEmpty()) {
            preferenceManager.putString(Constants.KEY_IMAGE, imageUrl);
            showToast("cập nhật url ảnh");
        }
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private Boolean isValidProfileDetails() {
        if (profileName.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (profileBirthdate.getText().toString().trim().isEmpty()) {
            showToast("Enter birthdate");
            return false;
        } else if (!isValidBirthdate(profileBirthdate.getText().toString())) {
            showToast("Enter valid birthdate in format dd/MM/yyyy");
            return false;
        } else if (!profileNewPassword.getText().toString().isEmpty()) {
            if (profileNewPassword.getText().toString().trim().isEmpty()) {
                showToast("Enter password");
                return false;
            } else if (profileConfirmPassword.getText().toString().trim().isEmpty()) {
                showToast("Enter your new password");
                return false;
            } else if (profileRecentPassword.getText().toString().trim().isEmpty()) {
                showToast("Enter recent your password");
                return false;
            } else if (!profileNewPassword.getText().toString().equals(profileConfirmPassword.getText().toString())) {
                showToast("Password & confirm password must be same");
                return false;
            } else if (!profileRecentPassword.getText().toString().equals(preferenceManager.getString(Constants.KEY_PASSWORD))) {
                showToast("Password & confirm password must be same");
                return false;
            }
        }
        return true;
    }

    private boolean isValidBirthdate(String birthdate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(birthdate);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            buttonUpdateProfile.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
            buttonUpdateProfile.setVisibility(View.VISIBLE);
        }
    }

}