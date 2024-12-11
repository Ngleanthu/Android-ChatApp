package com.example.chatapp.models;

import com.google.firebase.Timestamp;

public class ChatMessageModel {
private String message;
private String senderId;
private String fileUrl;
private Timestamp timestamp;
private boolean seen;

    public ChatMessageModel() {
    }

    public ChatMessageModel(String message, String senderId, Timestamp timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.seen = false;
    }

    public ChatMessageModel(String message, String senderId, String fileUrl, Timestamp timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.fileUrl = fileUrl;
        this.timestamp = timestamp;
        this.seen = false;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
