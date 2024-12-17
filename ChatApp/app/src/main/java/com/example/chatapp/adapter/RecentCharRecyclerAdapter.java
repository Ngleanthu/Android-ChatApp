
package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.activities.ChatActivity;
import com.example.chatapp.models.ChatRoomModel;
import com.example.chatapp.models.UserModel;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.FirebaseUtil;
import com.example.chatapp.utils.PreferenceManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class RecentCharRecyclerAdapter extends FirestoreRecyclerAdapter<ChatRoomModel, RecentCharRecyclerAdapter.ChatRoomModelViewHolder> {

    Context context;

    public RecentCharRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatRoomModel> options, Context context){
        super(options);
        this.context=context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatRoomModelViewHolder holder, int position, @NonNull ChatRoomModel model) {

        if (model.getLastMessage() == null || model.getLastMessage().isEmpty()) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return; // Dừng xử lý
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }

        holder.usernameText.setText("");
        holder.lastMessageText.setText("");
        holder.lastMessageTime.setText("");

        Log.d("Recent chat", "enter");
        PreferenceManager preferenceManager = new PreferenceManager(context.getApplicationContext());
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

        FirebaseUtil.getOtherUserFromChatroom(model.getUserId(), currentUserId)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            UserModel otherUserModel = document.toObject(UserModel.class);

                            if (otherUserModel != null) {
                                // Thiết lập giao diện với UserModel
                                holder.usernameText.setText(otherUserModel.getName());
                                String lastMessage = model.getLastMessage();
                                String formattedLastMessage;
                                formattedLastMessage = FirebaseUtil.formatLastMessage(lastMessage);
                                boolean lastMessageSendByMe = model.getLastMessageSenderId().equals(currentUserId);

                                // Notify user if they haven't seen latest messages
                                try {
                                    Log.d("ChatRecyclerAdapter", model.toString());
                                    boolean isLastMessageSeen = model.getLastMessageSeen();
                                    Log.d("ChatRecyclerAdaper", "isLastMessageSeen: " + isLastMessageSeen);
                                    if (!isLastMessageSeen && !lastMessageSendByMe) {

                                        holder.usernameText.setTypeface(null, Typeface.BOLD);
                                        holder.lastMessageText.setTypeface(null, Typeface.BOLD);
                                        holder.lastMessageText.setTextColor(ContextCompat.getColor(context, R.color.dark));

                                        Log.d("Lastmessage", "Set bold message: " + lastMessage);
                                    } else {
                                        holder.usernameText.setTypeface(null, Typeface.NORMAL);
                                        holder.lastMessageText.setTypeface(null, Typeface.NORMAL);
                                        holder.lastMessageText.setTextColor(ContextCompat.getColor(context, R.color.secondary_text));
                                    }
                                }catch (Exception e){
                                    Log.e("ChatRecyclerAdapter", e.getMessage());
                                }

                                holder.lastMessageText.setText(lastMessageSendByMe ? "You: " + formattedLastMessage : formattedLastMessage);
                                holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

                                String imageUrl = otherUserModel.getImage();
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    Glide.with(context)
                                            .load(imageUrl)
                                            .placeholder(R.drawable.ic_default_profile_foreground) // Ảnh tạm
                                            .into(holder.profilePic);
                                }
                                holder.itemView.setOnClickListener(v -> {
                                    Intent intent = new Intent(context, ChatActivity.class);
                                    AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                });
                            } else {
                                Log.e("FirebaseError", "UserModel is null after mapping.");
                            }
                        } else {
                            Log.e("FirebaseError", "Document does not exist.");
                        }
                    } else {
                        Log.e("FirebaseError", "Error fetching document: ", task.getException());
                    }
                });
    }

    // Ghi đè phương thức onDataChanged
    @Override
    public void onDataChanged() {
        super.onDataChanged();
        // Cập nhật adapter khi có thay đổi dữ liệu
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatRoomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row,parent,false);
        return new ChatRoomModelViewHolder(view);
    }

    class ChatRoomModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;
        ChatRoomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.image_other_chat_profile);
        }
    }


}



