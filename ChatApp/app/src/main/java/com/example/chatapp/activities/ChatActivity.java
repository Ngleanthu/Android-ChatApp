package com.example.chatapp.activities;


import static com.example.chatapp.utils.YoutubeUtil.containsYouTubeLink;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

import static com.example.chatapp.activities.MainActivity.MY_REQUEST_CODE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.emoji2.emojipicker.EmojiPickerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.ChatRecyclerAdapter;
import com.example.chatapp.firebase.AccessToken;
import com.example.chatapp.models.ChatMessageModel;
import com.example.chatapp.models.ChatRoomModel;
import com.example.chatapp.models.UserModel;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.EmojiConverter;
import com.example.chatapp.utils.FileHelper;
import com.example.chatapp.utils.FirebaseUtil;
import com.example.chatapp.utils.PreferenceManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    private FileHelper fileHelper;
    private PreferenceManager preferenceManager;

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(ChatActivity.class);

    UserModel otherUser;
    String chatroomId;
    ChatRoomModel chatRoomModel;
    String currentUserId; // Thêm biến lưu currentUserId
    private ActivityResultLauncher<Intent> activityResultLauncher;


    FrameLayout viewSendImage;
    String imageUrlSend;
    Uri imageUriSend;
    Button sendImageBtn;
    Button cancelSenImageBtn;
    ImageView imageSelected;
    private ProgressBar progressBar;


    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    ImageButton emojiBtn;
    ImageButton fileBtn;
    EmojiPickerView emojiPicker;
    RelativeLayout bottomLayout;
    TextView otherUsername;
    RecyclerView recyclerView;
    ChatRecyclerAdapter adapter;
    ImageView imageProfile;
    ImageButton cameraBtn;

    private ListenerRegistration messageListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitty_chat);
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_button);
        emojiBtn = findViewById(R.id.emoji_button);
        emojiPicker = findViewById(R.id.emoji_picker);
        cameraBtn = findViewById(R.id.attachment_button);
        bottomLayout = findViewById(R.id.bottom_layout);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imageProfile = findViewById(R.id.profile_pic_layout);
        viewSendImage = findViewById(R.id.view_send_image);
        sendImageBtn = findViewById(R.id.send_image_button);
        cancelSenImageBtn = findViewById(R.id.cancel_button);
        imageSelected = findViewById(R.id.selected_image_view);
        progressBar = findViewById(R.id.progressBarImage);
        fileBtn = findViewById(R.id.file_button);

        preferenceManager = new PreferenceManager(getApplicationContext());

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImageUri = data.getData(); // Lấy URI của ảnh được chọn
                            if (selectedImageUri != null) {

                                viewSendImage.setVisibility(View.VISIBLE);
                                Glide.with(this)
                                        .load(selectedImageUri)
                                        .placeholder(R.drawable.ic_default_profile_foreground) // Ảnh tạm
                                        .into(imageSelected);
                                imageUriSend = selectedImageUri; // Lưu URI ảnh
                            }
                        }
                    } else {

                        viewSendImage.setVisibility(View.GONE);
                    }
                }
        );

        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());

        currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        chatroomId = FirebaseUtil.getChatroomId(currentUserId, otherUser.getUserId());
        getOrCreateChatroomModel();
        setupChatRecyclerView();
        try {
            initializeChatListener();
        }catch (Exception e) {
            Log.e("initializeChatListener", "Error: " + e.getMessage());
        }

        backBtn.setOnClickListener((v) -> getOnBackPressedDispatcher().onBackPressed());
        otherUsername.setText(otherUser.getName());
        String imageUrl = otherUser.getImage();
        Log.d("CHATACTIVITY", "UserModel: " + otherUser.getUserId() + "  Image: "+ otherUser.getImage());
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_default_profile_foreground) // Ảnh tạm
                    .into(imageProfile);
        }

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Replace symbol patterns with emojis
                String input = s.toString();
                String updatedInput = input;
                if(!containsYouTubeLink(input)) {
                    updatedInput = EmojiConverter.replaceWithEmojis(input);
                }
                // Avoid infinite loop by checking if text actually changed
                if (!input.equals(updatedInput)) {
                    messageInput.removeTextChangedListener(this); // Temporarily remove watcher
                    messageInput.setText(updatedInput);
                    messageInput.setSelection(updatedInput.length()); // Move cursor to end
                    messageInput.addTextChangedListener(this); // Reattach watcher
                }
            }
        });

        sendMessageBtn.setOnClickListener((v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessageToUser(message, "text"); // Gửi tin nhắn
            }
        }));

        cancelSenImageBtn.setOnClickListener(v -> {
            viewSendImage.setVisibility(View.GONE);
        });

        sendImageBtn.setOnClickListener((v -> {
            loading(true);
            uploadImageAndSaveToFirestore(new OnImageUploadCompleteListener() {
                @Override
                public void onImageUploadComplete(boolean success, String imageUrl) {
                    if (success) {
                        sendMessageToUser(imageUrl, "image");
                        viewSendImage.setVisibility(View.GONE);
                    } else {
                        Log.e("ChatActivity Upload", "Failed to upload image");
                    }
                }
            });
        }));


        emojiBtn.setOnClickListener(v -> toggleEmojiPicker());

        emojiPicker.setOnEmojiPickedListener(emoji -> {
            messageInput.append(emoji.getEmoji());
            messageInput.requestFocus();
        });

        fileHelper = new FileHelper(
                this,
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            handleFileSelection(fileUri);
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
                        Toast.makeText(ChatActivity.this, "File selected: " + fileUri, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(String type) {
                        Toast.makeText(ChatActivity.this,
                                "Permission required to select " + type + "!", Toast.LENGTH_SHORT).show();
                    }
                });
        fileBtn.setOnClickListener(v -> fileHelper.selectFile("file"));
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRequestPermission();
            }
        });
    }
    void setupChatRecyclerView(){
        Log.d("chatactivity", "setupChatRecyclerView: " + currentUserId );

        Query query = FirebaseUtil.getChatroomMessagesReference(chatroomId).orderBy("timestamp", Query.Direction.DESCENDING);


        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();
        Log.d("chatactivity", "setupChatRecyclerView: " + currentUserId );
        adapter = new ChatRecyclerAdapter(options, getApplicationContext(),currentUserId, this::onFileClick);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setReverseLayout(true);

        recyclerView.setLayoutManager(manager);

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }
    void sendMessageToUser(String message, String type) {
        if (currentUserId != null) { // Đảm bảo currentUserId đã có giá trị
            chatRoomModel.setLastMessageSenderId(currentUserId);
            chatRoomModel.setLastMessageTimestamp(Timestamp.now());
            chatRoomModel.setLastMessage(message);
            chatRoomModel.setLastMessageSeen(false);
            chatRoomModel.setType(type);
            FirebaseUtil.getChatroomReference(chatroomId).set(chatRoomModel);
            ChatMessageModel chatMessageModel = new ChatMessageModel(message, currentUserId, Timestamp.now(), type);
            FirebaseUtil.getChatroomMessagesReference(chatroomId).add(chatMessageModel)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            messageInput.setText(""); // Clear input
                            sendNotification(message);
                        } else {
                            Log.e("ChatActivity send message", "Failed to send message.");
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
                    chatRoomModel = new ChatRoomModel(chatroomId, userIds, Timestamp.now(), "", "text");

                    FirebaseUtil.getChatroomReference(chatroomId).set(chatRoomModel);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (messageListener == null) {
            initializeChatListener();
        }
        if (adapter != null) {
            adapter.startListening();
        }
    }

    void sendNotification(String message) {
        if (currentUserId != null && otherUser != null && otherUser.getFcmToken() != null) {
            try {
                JSONObject jsonObject = new JSONObject();
                JSONObject messageObject = new JSONObject();

                // Notification payload
                JSONObject notificationObj = new JSONObject();
                notificationObj.put("title", preferenceManager.getString(Constants.KEY_NAME));
                notificationObj.put("body", message);

                // Data payload
                JSONObject dataObj = new JSONObject();
                dataObj.put("userId", currentUserId);

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
            Log.e("sendNotification", "FCM token: " + otherUser.getFcmToken());
        }
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
                    Log.d("Access Token: ", "Successfully obtain access token");
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

    public void listenForIncomingMessages(String chatroomId, String currentUserId) {

        CollectionReference messagesRef = FirebaseUtil.getChatroomMessagesReference(chatroomId);

        Log.d("listenForIncomingMessage", "Enter");
        messageListener = messagesRef
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("ChatActivity", "Error listening for messages: ", error);
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // Update UI when seen status changed or new messages arrive
                        adapter.notifyDataSetChanged();

                        // Scroll screen automatically to the latest message
                        recyclerView.scrollToPosition(0);

                        for (DocumentChange docChange : querySnapshot.getDocumentChanges()) {
                            if (docChange.getType() == DocumentChange.Type.ADDED) {
                                ChatMessageModel message = docChange.getDocument().toObject(ChatMessageModel.class);

                                // Update seen property if the message is for the current user
                                if (!message.getSenderId().equals(currentUserId) && !message.isSeen()) {
                                    docChange.getDocument().getReference().update("seen", true)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("ChatActivity", "Message marked as seen");

                                                // Check if this is the latest message
                                                messagesRef
                                                        .orderBy("timestamp", Query.Direction.DESCENDING)
                                                        .limit(1)
                                                        .get()
                                                        .addOnSuccessListener(querySnapshot1 -> {
                                                            if (!querySnapshot1.isEmpty()) {
                                                                DocumentSnapshot latestMessageDoc = querySnapshot1.getDocuments().get(0);
                                                                String latestMessageId = latestMessageDoc.getId();

                                                                // Compare IDs to determine if this is the latest message
                                                                if (docChange.getDocument().getId().equals(latestMessageId)) {
                                                                    // Update isLastMessageSeen in ChatRoomModel
                                                                    FirebaseUtil.getChatroomReference(chatroomId)
                                                                            .update("lastMessageSeen", true)
                                                                            .addOnSuccessListener(aVoid1 -> Log.d("ChatActivity", "isLastMessageSeen updated successfully"))
                                                                            .addOnFailureListener(e -> Log.e("ChatActivity", "Failed to update isLastMessageSeen", e));
                                                                }
                                                            }
                                                        });
                                            })
                                            .addOnFailureListener(e -> Log.e("ChatActivity", "Failed to update seen status", e));
                                }
                            }
                        }
                    }
                });
    }

    private void initializeChatListener() {
        if (chatroomId != null && currentUserId != null) {
            listenForIncomingMessages(chatroomId, currentUserId);
        } else {
            Log.e("ChatActivity", "Chatroom ID or Current User ID is not ready yet.");
        }
    }

    private void toggleEmojiPicker() {
        if (emojiPicker.getVisibility() == View.GONE) {
            showEmojiPicker();
        } else {
            hideEmojiPicker();
        }
    }

    private void showEmojiPicker() {
        emojiPicker.setVisibility(View.VISIBLE);

        // Adjust bottom_layout position
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bottomLayout.getLayoutParams();
        params.addRule(RelativeLayout.ABOVE, R.id.emoji_picker);
        params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bottomLayout.setLayoutParams(params);

        scrollChatToBottom();
    }

    private void hideEmojiPicker() {
        emojiPicker.setVisibility(View.GONE);

        // Reset bottom_layout position
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bottomLayout.getLayoutParams();
        params.addRule(RelativeLayout.ABOVE, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bottomLayout.setLayoutParams(params);
    }

    private void scrollChatToBottom() {
        recyclerView.post(() -> {
            if (recyclerView.getAdapter() != null) {
                recyclerView.scrollToPosition(0);
            }
        });
    }

    private void handleFileSelection(Uri fileUri) {
        String fileName = FileHelper.getFileName(this, fileUri);
        Log.d("FileSelection", "File Selected: " + fileUri);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = storageReference.child("chatrooms/" + chatroomId + "/files/" + fileName);

        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Get the file URL
                    sendMessageWithFile(fileName);
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "File upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendMessageWithFile(String fileName) {
        String message = preferenceManager.getString(Constants.KEY_NAME) + " sent an attachment";
        if (currentUserId != null) { // Đảm bảo currentUserId đã có giá trị
            chatRoomModel.setLastMessageSenderId(currentUserId);
            chatRoomModel.setLastMessageTimestamp(Timestamp.now());
            chatRoomModel.setLastMessage(message);
            chatRoomModel.setLastMessageSeen(false);
            FirebaseUtil.getChatroomReference(chatroomId).set(chatRoomModel);

            ChatMessageModel chatMessageModel = new ChatMessageModel(message, currentUserId, fileName, Timestamp.now());
            FirebaseUtil.getChatroomMessagesReference(chatroomId).add(chatMessageModel)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            sendNotification(message);
                        } else {
                            Log.e("ChatActivity", "Failed to send message.");
                        }
                    });
        }
    }

    private void onFileClick(String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download File")
                .setMessage("Do you want to download this file?")
                .setPositiveButton("Download", (dialog, which) -> downloadFile(fileName))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void downloadFile(String fileName) {
        if (fileName == null) return;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = storageReference.child("chatrooms/" + chatroomId + "/files/" + fileName);

        // Get the download URL
        fileRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // Use DownloadManager to download the file
                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    if (downloadManager == null) {
                        Toast.makeText(this, "Download Manager not available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DownloadManager.Request request = new DownloadManager.Request(uri);
                    request.setTitle("Downloading " + fileName);
                    request.setDescription("File is being downloaded...");
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName); // Save to Downloads folder
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                    // Enqueue the download
                    downloadManager.enqueue(request);
                    Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // choose image form gallery

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

    private void uploadImageAndSaveToFirestore(final OnImageUploadCompleteListener listener) {
        // Firebase Storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        // Kiểm tra giá trị của imageUri
        if (imageUriSend == null) {
            return; // Không làm gì thêm nếu imageUriSend là null
        } else {
            Log.d("Upload", "Image URI: " + imageUriSend.toString());
        }

        // Tạo tên file duy nhất dựa trên timestamp hoặc UUID
        String fileName = "chatrooms/" + chatroomId + "/images/image_" + System.currentTimeMillis();
        StorageReference imageRef = storageReference.child(fileName);

        // Tải ảnh từ imageUriSend lên Firebase Storage
        imageRef.putFile(imageUriSend)
                .addOnSuccessListener(taskSnapshot -> {
                    // Sau khi upload thành công, lấy URL của hình ảnh
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Log.d("Upload IMAGE LEN STORE", "Image URL: " + imageUrl);
                        imageUrlSend = imageUrl;

                        // Gọi listener để thông báo hoàn tất
                        if (listener != null) {
                            listener.onImageUploadComplete(true, imageUrl);  // Thông báo upload thành công
                        }

                        // Gửi thông báo thành công
                        Toast.makeText(getApplicationContext(), "Upload success", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    // Xử lý khi tải ảnh thất bại
                    Log.e("Upload", "Upload failed", e);
                    if (listener != null) {
                        listener.onImageUploadComplete(false, null);  // Thông báo upload thất bại
                    }
                    Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    // Định nghĩa interface cho callback
    public interface OnImageUploadCompleteListener {
        void onImageUploadComplete(boolean success, String imageUrl);
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            cancelSenImageBtn.setVisibility(View.INVISIBLE);
            sendImageBtn.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}