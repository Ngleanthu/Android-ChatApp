package com.example.chatapp.models;

import com.google.firebase.Timestamp;

public class UserModel {
    private String email;
    private String name;
    private String userId;
    private String image; // Thêm trường hình ảnh đại diện
    private String birthdate;  // Thêm trường ngày sinh
    private String password;   // Thêm trường mật khẩu
    private Timestamp createdTimestamp;
    private String fcmToken;

    public UserModel() {
    }

    public UserModel(String email, String name, String userId,String image, String birthdate, String password ,Timestamp createdTimestamp) {
        this.email = email;
        this.name = name;
        this.userId = userId;
        this.image = image;
        this.birthdate = birthdate;
        this.password = password;
        this.createdTimestamp = createdTimestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String phone) {
        this.email = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void setImage(String image) {
        this.image = image; // Phương thức setter cho image
    }

    public String getImage() {
        return image; // Phương thức getter cho image
    }

    // Getter và Setter cho password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }
}
