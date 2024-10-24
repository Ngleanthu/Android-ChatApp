package com.example.chatapp.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.ChatRecyclerAdapter;
import com.example.chatapp.adapter.SearchUserRecyclerAdapter;
import com.example.chatapp.firebase.AccessToken;
import com.example.chatapp.models.ChatMessageModel;
import com.example.chatapp.models.ChatRoomModel;
import com.example.chatapp.models.ChatMessageModel;
import com.example.chatapp.models.UserModel;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    UserModel otherUser;
    String chatroomId;
    ChatRoomModel chatRoomModel;
    String currentUserId; // Thêm biến lưu currentUserId


    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ChatRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitty_chat);

        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());

        // Lấy FCM token trước để xác định người dùng hiện tại
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(fcmToken -> {
            FirebaseUtil.getEmailByFcmToken(fcmToken).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String currentUserEmail = task.getResult();
                    FirebaseUtil.getUserIdByEmail(currentUserEmail, userId -> {
                        if (userId != null) {
                            currentUserId = userId; // Lưu currentUserId
                            chatroomId = FirebaseUtil.getChatroomId(currentUserId, otherUser.getUserId());
                            getOrCreateChatroomModel();
                            setupChatRecyclerView();

                        }
                    });
                }
            });
        });


        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_button);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);

        backBtn.setOnClickListener((v) -> getOnBackPressedDispatcher().onBackPressed());
        otherUsername.setText(otherUser.getName());

        sendMessageBtn.setOnClickListener((v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessageToUser(message); // Gửi tin nhắn
            }
        }));
    }
void setupChatRecyclerView(){
    Log.d("chatactivity", "setupChatRecyclerView: " + currentUserId );

    Query query = FirebaseUtil.getChatroomMessagesReference(chatroomId).orderBy("timestamp", Query.Direction.DESCENDING);


    FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel.class).build();
    Log.d("chatactivity", "setupChatRecyclerView: " + currentUserId );
    adapter = new ChatRecyclerAdapter(options, getApplicationContext(),currentUserId);
    LinearLayoutManager manager=new LinearLayoutManager(this);
    manager.setReverseLayout(true);

    recyclerView.setLayoutManager(manager);

    recyclerView.setAdapter(adapter);
    adapter.startListening();
}
    void sendMessageToUser(String message) {
        if (currentUserId != null) { // Đảm bảo currentUserId đã có giá trị
            chatRoomModel.setLastMessageSenderId(currentUserId);
            chatRoomModel.setLastMessageTimestamp(Timestamp.now());
            FirebaseUtil.getChatroomReference(chatroomId).set(chatRoomModel);

            ChatMessageModel chatMessageModel = new ChatMessageModel(message, currentUserId, Timestamp.now());
            FirebaseUtil.getChatroomMessagesReference(chatroomId).add(chatMessageModel)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            messageInput.setText(""); // Clear input
                            sendNotification(message);
                        } else {
                            Log.e("ChatActivity", "Failed to send message.");
                        }
                    });
        }
    }

    void getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                chatRoomModel = task.getResult().toObject(ChatRoomModel.class);
                if (chatRoomModel == null) {
                    List<String> userIds = Arrays.asList(currentUserId, otherUser.getUserId());
                    chatRoomModel = new ChatRoomModel(chatroomId, userIds, Timestamp.now(), "");

                    FirebaseUtil.getChatroomReference(chatroomId).set(chatRoomModel);
                }
            }
        });
    }

    void sendNotification(String message) {
        FirebaseUtil.currentUserDetails(getApplicationContext()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                UserModel currentUser = task.getResult().toObject(UserModel.class);
                if (currentUser != null && otherUser != null && otherUser.getFcmToken() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject();

                        JSONObject messageObject = new JSONObject();

                        // Notification payload
                        JSONObject notificationObj = new JSONObject();
                        notificationObj.put("title", currentUser.getName());
                        notificationObj.put("body", message);

                        // Data payload
                        JSONObject dataObj = new JSONObject();
                        dataObj.put("userId", currentUser.getUserId());

                        messageObject.put("notification", notificationObj);
                        messageObject.put("data", dataObj);
                        messageObject.put("token", otherUser.getFcmToken());

                        jsonObject.put("message", messageObject);
                        // Send API request
                        callApi(jsonObject);

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Call Api: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Log.e("sendNotification", "User details or FCM token is null");
                }
            } else {
                Log.e("sendNotification", "Failed to get current user details: " + task.getException().getMessage());
            }
        });
    }

    void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/v1/projects/chatapp-hcmus/messages:send";

        new Thread(() -> {
            AccessToken accessToken = new AccessToken();

            final String token = accessToken.getAccessToken(this);

            new Handler(Looper.getMainLooper()).post(() -> {
                if(token != null){
                    Log.e("Access Token: ", "Successfully obtain access token");
                }else{
                    Log.e("Access Token: ", "Failed to obtain access token");

                }
            });

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .build();


            // Use enqueue for asynchronous call
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("callApi", "Failed to send notification: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Failed to send notification", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d("callApi", "Notification sent successfully");
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Notification sent successfully", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Log.e("callApi", "Failed to send notification. Response: " + response.message());
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Failed to send notification", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }).start();

    }


}
