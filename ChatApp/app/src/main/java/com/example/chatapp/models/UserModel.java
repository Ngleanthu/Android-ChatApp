package com.example.chatapp.models;

import com.google.firebase.Timestamp;

public class UserModel {
    private String email;
    private String name;
    private String userId;
    private Timestamp createdTimestamp;

    public UserModel() {
    }

    public UserModel(String email, String name, String userId, Timestamp createdTimestamp) {
        this.email = email;
        this.name = name;
        this.userId = userId;
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
}
