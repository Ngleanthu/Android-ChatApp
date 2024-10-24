package com.example.chatapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.ChatRecyclerAdapter;
import com.example.chatapp.adapter.SearchUserRecyclerAdapter;
import com.example.chatapp.models.ChatMessageModel;
import com.example.chatapp.models.ChatRoomModel;
import com.example.chatapp.models.ChatMessageModel;
import com.example.chatapp.models.UserModel;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.List;

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
    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
        ;
    }
}
