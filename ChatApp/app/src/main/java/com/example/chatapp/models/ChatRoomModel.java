package com.example.chatapp.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class ChatRoomModel {
    String chatroomId;
    List<String> userId;
    Timestamp lastMessageTimestamp;
    String lastMessageSenderId;
    Boolean lastMessageSeen;
    String type;

    String lastMessage;

    public ChatRoomModel() {
    }

    public ChatRoomModel(String chatroomId, List<String> userId, Timestamp lastMessageTimestamp, String lastMessageSenderId, String type) {
        this.chatroomId = chatroomId;
        this.userId = userId;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
        this.type = type;
    }

    public String getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public List<String> getUserId() {
        return userId;
    }

    public void setUserId(List<String> userId) {
        this.userId = userId;
    }

    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public Boolean getLastMessageSeen() {
        return lastMessageSeen;
    }

    public void setLastMessageSeen(Boolean lastMessageSeen) {
        this.lastMessageSeen = lastMessageSeen;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public  String getType(){return type;}
    public  void setType(String type) {this.type = type;}
}
