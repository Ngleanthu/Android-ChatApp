package com.example.chatapp.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.chatapp.models.ChatMessageModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import kotlin.text.UStringsKt;

public class FirebaseUtil {


    // Lấy FCM token và tìm ID người dùng từ Firestore
    public static void currentUserId(String fcmToken, CurrentUserIdCallback callback) {
        Log.d("ChatActivity", "Retrieving user ID for FCM Token: " + fcmToken);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference usersCollection = database.collection("users");

        Query query = usersCollection.whereEqualTo("fcmToken", fcmToken);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Gọi callback với ID người dùng
                    callback.onCallback(document.getId());
                    return;
                }
            }
            callback.onCallback(null);
        });
    }

    public static DocumentReference currentUserDetails(String userId){
        return FirebaseFirestore.getInstance().collection("users").document(userId);
    }
    public static CollectionReference allUserCollectionReference() {
        return FirebaseFirestore.getInstance().collection("users");
    }

    public static DocumentReference getChatroomReference(String chatroomId) {
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }
    public static CollectionReference getChatroomMessagesReference(String chatroomId) {
        return getChatroomReference(chatroomId).collection("chats");
    }
    public static String getChatroomId(String userId1, String userId2) {
        Log.d("ChatActivity", "User Name atgetchat: " + userId1);

        if (userId1.hashCode() < userId2.hashCode()) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    // Callback interface để lấy ID người dùng
    public interface CurrentUserIdCallback {
        void onCallback(String userId);
    }
    // Lấy User ID dựa trên email của người dùng
    public static void getUserIdByEmail(String email, UserIdCallback callback) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference usersCollection = database.collection("users");

        // Truy vấn để tìm người dùng theo email
        Query query = usersCollection.whereEqualTo("email", email);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Gọi callback với userId khi tìm thấy
                    String userId = document.getString("userId"); // Sửa lại để lấy userId từ trường
                    callback.onCallback(userId);
                    return;
                }
            }
            callback.onCallback(null);
        });
    }
    public static Task<String> getEmailByFcmToken(String fcmToken) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

        // Tìm kiếm user bằng FCM token
        db.collection("users")
                .whereEqualTo("fcmToken", fcmToken)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Lấy email từ kết quả
                        String email = task.getResult().getDocuments().get(0).getString("email");
                        taskCompletionSource.setResult(email);
                    } else {
                        Log.d("FirebaseUtil", "No user found for FCM token.");
                        taskCompletionSource.setResult(null);  // Trả về null nếu không tìm thấy
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseUtil", "Error getting email by FCM token", e);
                    taskCompletionSource.setException(e);  // Trả về lỗi nếu truy vấn thất bại
                });

        return taskCompletionSource.getTask();  // Trả về task để lắng nghe kết quả
    }
    // Interface callback để lấy User ID
    public interface UserIdCallback {
        void onCallback(String userId);
    }

    public static CollectionReference allChatroomCollectionReference(){
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    public static  DocumentReference getOtherUserFromChatroom (List<String> userIds, String currentUserId){
        // Kiểm tra xem danh sách có đủ người dùng không
        if (userIds == null || userIds.size() < 2) {
            throw new IllegalArgumentException("userIds must contain at least two user IDs.");
        }

        // Tìm người dùng khác
        for (String userId : userIds) {
            if (!userId.equals(currentUserId)) {
                return allUserCollectionReference().document(userId);
            }
        }

        // Nếu không tìm thấy, có thể ném ngoại lệ hoặc trả về null
        return allUserCollectionReference().document(currentUserId);
    }

    public static String timestampToString (Timestamp timestamp){
        // Lấy thời gian hiện tại
        Calendar currentCalendar = Calendar.getInstance();
        Calendar timestampCalendar = Calendar.getInstance();
        timestampCalendar.setTime(timestamp.toDate());

        // So sánh ngày, tháng, năm
        boolean isToday = currentCalendar.get(Calendar.YEAR) == timestampCalendar.get(Calendar.YEAR)
                && currentCalendar.get(Calendar.DAY_OF_YEAR) == timestampCalendar.get(Calendar.DAY_OF_YEAR);

        if (isToday) {
            // Chỉ trả về giờ:phút nếu là ngày hiện tại
            return new SimpleDateFormat("HH:mm").format(timestamp.toDate());
        } else {
            // Trả về định dạng 2 dòng: giờ:phút và ngày/tháng
            String time = new SimpleDateFormat("HH:mm").format(timestamp.toDate());
            String date = new SimpleDateFormat("dd/MM").format(timestamp.toDate());
            return time + "\n" + date;
        }

    }

    public static StorageReference getOtherProfilePicStorageRef(String otherUserId){
        return FirebaseStorage.getInstance().getReference().child("users")
                .child(otherUserId).child("profile.jpg");
    }

    public static String formatLastMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        String[] lines = message.split("\\n");

        String firstLine = lines[0];

        if (firstLine.length() > 30) {
            return firstLine.substring(0, 30) + "…";
        } else {
            return firstLine;
        }
    }
}
