package com.example.chatapp.activities;

import android.app.DatePickerDialog;
import android.net.Uri;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableRow;


import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.FileHelper;
import com.example.chatapp.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import com.makeramen.roundedimageview.RoundedImageView;
import com.bumptech.glide.Glide;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import java.util.HashMap;




public class UpdateProfileActivity extends AppCompatActivity {
    private FileHelper fileHelper;
    private EditText profileName, profileBirthdate, profileNewPassword, profileConfirmPassword, profileRecentPassword;
    private ImageButton btnBack;
    private PreferenceManager preferenceManager;
    private RoundedImageView imageProfile;
    private MaterialButton buttonUpdateProfile;
    private ProgressBar progressBar;
    private TextView profileEmail;
    private TableRow confirmPasswordRow;
    private TableRow recentPasswordRow;

    private Uri imageUri;
    private String userId;


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
        progressBar = findViewById(R.id.progressBar);
        profileEmail = findViewById(R.id.profileEmail);
        confirmPasswordRow = findViewById(R.id.confirmPasswordRow);
        recentPasswordRow = findViewById(R.id.recentPasswordRow);

        userId = preferenceManager.getString(Constants.KEY_USER_ID);

        getInfoUser();
        initListener();
        profileEmail = findViewById(R.id.profileEmail);
        profileBirthdate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    UpdateProfileActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        profileBirthdate.setText(selectedDate);
                    },
                    year, month, day
            );
            datePicker.show();
        });


        fileHelper = new FileHelper(
                this,
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            imageProfile.setImageURI(fileUri);
                            // lưu lại uri
                            imageUri = fileUri;
                        }
                    }
                }),
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    boolean allGranted = result.values().stream().allMatch(granted -> granted);
                    if (!allGranted) {
                        Toast.makeText(this, "Permission required to access storage!", Toast.LENGTH_SHORT).show();
                    }
                }),
                new FileHelper.FileHelperCallback() {
                    @Override
                    public void onFileSelected(Uri fileUri) {
                        Toast.makeText(UpdateProfileActivity.this, "File selected: " + fileUri, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(String type) {
                        Toast.makeText(UpdateProfileActivity.this,
                                "Permission required to select " + type + "!", Toast.LENGTH_SHORT).show();
                    }
                });

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
        // Lấy dữ liệu từ PreferenceManager
        profileName.setText(preferenceManager.getString(Constants.KEY_NAME));
        profileBirthdate.setText(preferenceManager.getString(Constants.KEY_BIRTHDATE));
        profileEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        String userAvatarUrl = preferenceManager.getString(Constants.KEY_IMAGE);
        if (userAvatarUrl != null && !userAvatarUrl.isEmpty()) {
            // Thiết lập hình ảnh đại diện nếu có URL
            Glide.with(this)
                    .load(userAvatarUrl)
                    .placeholder(R.drawable.ic_default_profile_foreground) // Hình ảnh placeholder khi đang tải ảnh
                    .into(imageProfile); // ImageView để hiển thị ảnh
        }

    }

    private void initListener() {
        imageProfile.setOnClickListener(v-> fileHelper.selectFile("image"));

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
                if(isValidProfileDetails()){
                    String name = profileName.getText().toString();
                    String birthdate = profileBirthdate.getText().toString();
                    String newPassword = profileNewPassword.getText().toString();

                    loading(true);
                    if (imageUri != null) {
                        uploadImageAndSaveToFirestore(name, birthdate, newPassword);
                    } else {
                        updateUserInFirestore(name, birthdate, "", newPassword);
                    }
                }
            }
        });
    }

    private void uploadImageAndSaveToFirestore(String name, String birthdate, String newPassword) {

        // Firebase Storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();



        // Kiểm tra giá trị của imageUri
        if (imageUri == null) {
            Log.e("Upload", "Image URI is null");
            return; // Ngừng thực hiện nếu imageUri là null
        } else {
            Log.d("Upload", "Image URI: " + imageUri.toString());
        }
        // Đường dẫn cho file ảnh trong Firebase Storage
//        StorageReference imageRef = storageReference.child("users/" + preferenceManager.getString(Constants.KEY_USER_ID) + "/profile.jpg");
        StorageReference imageRef = storageReference.child("users/" + preferenceManager.getString(Constants.KEY_USER_ID) + "/profile.jpg");
        // Tải ảnh từ imageUri lên Firebase Storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
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

        if (imageUrl != "") {
            updatedUser.put("image", imageUrl); // Cập nhật URL ảnh nếu có
        }

        // Cập nhật thông tin người dùng trong Firestore
        userRef.update(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    // Xử lý khi cập nhật thành công
                    Log.d("Firestore", "User profile updated successfully");
                    updateInfoUserToPreferenceManger(name, birthdate, imageUrl ,newPassword);
                    finish(); // Đóng Activity hiện tại, trở về Activity trước đó
                })
                .addOnFailureListener(e -> {
                    // Xử lý khi cập nhật thất bại
                    Log.e("Firestore", "Error updating profile", e);
                    Toast.makeText(getApplicationContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
                });

    }

    private void updateInfoUserToPreferenceManger(String name, String birthdate,String imageUrl, String newPassword) {
        if (!name.isEmpty()) {
            preferenceManager.putString(Constants.KEY_NAME, name);
        }
        if (!birthdate.isEmpty()) {
            preferenceManager.putString(Constants.KEY_BIRTHDATE, birthdate);
        }
        if (!newPassword.isEmpty()) {
            preferenceManager.putString(Constants.KEY_PASSWORD, newPassword);
        }

        if (!imageUrl.isEmpty()) {
            preferenceManager.putString(Constants.KEY_IMAGE, imageUrl);
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
            if (profileConfirmPassword.getText().toString().trim().isEmpty()) {
                showToast("Enter confirm password");
                return false;
            } else if (profileRecentPassword.getText().toString().trim().isEmpty()) {
                showToast("Enter your recent password");
                return false;
            } else if (!profileNewPassword.getText().toString().equals(profileConfirmPassword.getText().toString())) {
                showToast("New password & confirm password must be same");
                return false;
            } else if (!profileRecentPassword.getText().toString().equals(preferenceManager.getString(Constants.KEY_PASSWORD))) {
                showToast("Recent password is incorrect!");
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

