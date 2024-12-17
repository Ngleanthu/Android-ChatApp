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
import com.example.chatapp.utils.HashUtil;
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
    TextView errorRecentPW, errorConfirmPW, errorPW, errorDOB, errorName;
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
        confirmPasswordRow = findViewById(R.id.confirmPasswordRow);
        recentPasswordRow = findViewById(R.id.recentPasswordRow);
        errorName = findViewById(R.id.errorName);
        errorDOB = findViewById(R.id.errorDOB);
        errorPW = findViewById(R.id.errorPW);
        errorConfirmPW = findViewById(R.id.errorConfirmPW);
        errorRecentPW = findViewById(R.id.errorRecentPW);

        userId = preferenceManager.getString(Constants.KEY_USER_ID);

        getInfoUser();
        initListener();
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

        profileBirthdate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String inputDate = profileBirthdate.getText().toString();
                    if (!isValidDate(inputDate)) {
                        Toast.makeText(UpdateProfileActivity.this, "Invalid Date Format", Toast.LENGTH_SHORT).show();
                    }
                }
            }
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
                        Log.d("File Selected", "File selected: " + fileUri);
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
        String userAvatarUrl = preferenceManager.getString(Constants.KEY_IMAGE);
        if (userAvatarUrl != null && !userAvatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(userAvatarUrl)
                    .placeholder(R.mipmap.ic_default_profile)
                    .into(imageProfile);
        }

    }

    private void initListener() {
        imageProfile.setOnClickListener(v-> fileHelper.selectFile("image"));

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

        if (imageUri == null) {
            Log.e("Upload", "Image URI is null");
            return; // Ngừng thực hiện nếu imageUri là null
        } else {
            Log.d("Upload", "Image URI: " + imageUri.toString());
        }
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
            return;
        }
        Map<String, Object> updatedUser = new HashMap<>();
        if (!name.isEmpty()) {
            updatedUser.put("name", name);
        }
        if (!birthdate.isEmpty()) {
            updatedUser.put("birthdate", birthdate);
        }
        String passwordNoHash, bg;
        if (!newPassword.isEmpty()) {
            passwordNoHash = newPassword;
            bg = newPassword;
            newPassword = HashUtil.hashPassword(newPassword.toString());
            updatedUser.put("password", newPassword);
            updatedUser.put("bg", bg);
        } else {
            passwordNoHash = "";
            bg = "";
        }

        if (imageUrl != "") {
            updatedUser.put("image", imageUrl);
        }
        // Cập nhật thông tin người dùng trong Firestore
        userRef.update(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User profile updated successfully");
                    updateInfoUserToPreferenceManger(name, birthdate, imageUrl ,passwordNoHash,bg);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Xử lý khi cập nhật thất bại
                    Log.e("Firestore", "Error updating profile", e);
                    Toast.makeText(getApplicationContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
                });

    }

    private void updateInfoUserToPreferenceManger(String name, String birthdate,String imageUrl, String newPassword, String bg) {
        if (!name.isEmpty()) {
            preferenceManager.putString(Constants.KEY_NAME, name);
        }
        if (!birthdate.isEmpty()) {
            preferenceManager.putString(Constants.KEY_BIRTHDATE, birthdate);
        }
        if (!newPassword.isEmpty()) {
            preferenceManager.putString(Constants.KEY_PASSWORD, newPassword);
        }
        if (!bg.isEmpty()) {
            preferenceManager.putString(Constants.KEY_BG, bg);
        }
        if (!imageUrl.isEmpty()) {
            preferenceManager.putString(Constants.KEY_IMAGE, imageUrl);
        }
    }

    private Boolean isValidProfileDetails() {
        Boolean res = true;
        if (profileName.getText().toString().trim().isEmpty()) {
            errorName.setText("Enter name");
            errorName.setVisibility(View.VISIBLE);
            res =  false;
        }
        if (profileBirthdate.getText().toString().trim().isEmpty()) {
            errorDOB.setText("Enter birthdate");
            errorDOB.setVisibility(View.VISIBLE);
            res =  false;
        }
        if (!isValidBirthdate(profileBirthdate.getText().toString())) {
            errorDOB.setText("Enter valid birthdate in format dd/MM/yyyy");
            errorDOB.setVisibility(View.VISIBLE);
            res =  false;
        }
        if (!profileNewPassword.getText().toString().isEmpty()) {
            if (profileConfirmPassword.getText().toString().trim().isEmpty()) {
                errorConfirmPW.setText("Enter confirm password");
                errorConfirmPW.setVisibility(View.VISIBLE);
                res =  false;
            }else if (!profileNewPassword.getText().toString().equals(profileConfirmPassword.getText().toString())) {
                errorConfirmPW.setText("New password & confirm password\n must be same");
                errorConfirmPW.setVisibility(View.VISIBLE);
                res =  false;
            }


            if (profileRecentPassword.getText().toString().trim().isEmpty()) {
                errorRecentPW.setText("Enter your recent password");
                errorRecentPW.setVisibility(View.VISIBLE);
                res =  false;
            }  else if (!profileRecentPassword.getText().toString().equals(preferenceManager.getString(Constants.KEY_PASSWORD))) {
                errorRecentPW.setText("Recent password is incorrect!");
                errorRecentPW.setVisibility(View.VISIBLE);
                res =  false;
            }
        }
        return res;
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
            btnBack.setVisibility(View.INVISIBLE);
            buttonUpdateProfile.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            btnBack.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            buttonUpdateProfile.setVisibility(View.VISIBLE);
        }
    }

    private void  clearError(){
        errorName.setText("");
        errorName.setVisibility(View.GONE);
        errorDOB.setText("");
        errorDOB.setVisibility(View.GONE);
        errorPW.setText("");
        errorPW.setVisibility(View.GONE);
        errorConfirmPW.setText("");
        errorConfirmPW.setVisibility(View.GONE);
        errorRecentPW.setText("");
        errorRecentPW.setVisibility(View.GONE);
    }

    private boolean isValidDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

