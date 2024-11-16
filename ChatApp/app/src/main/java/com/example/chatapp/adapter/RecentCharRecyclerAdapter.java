
package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
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

public class RecentCharRecyclerAdapter extends FirestoreRecyclerAdapter<ChatRoomModel, RecentCharRecyclerAdapter.ChatRoomModelViewHolder> {

    Context context;

    public RecentCharRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatRoomModel> options, Context context){
        super(options);
        this.context=context;
    }
    @Override
    protected void onBindViewHolder(@NonNull ChatRoomModelViewHolder holder, int position, @NonNull ChatRoomModel model) {

        PreferenceManager preferenceManager = new PreferenceManager(context.getApplicationContext());
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);


        FirebaseUtil.getOtherUserFromChatroom(model.getUserId(),currentUserId )
                .get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        boolean lastMessageSendByMe = model.getLastMessageSenderId().equals(currentUserId);
                        Toast.makeText(context, "getOtherUserFromChatroom thanh cong", Toast.LENGTH_SHORT).show();
                        UserModel otherUserModel = task.getResult().toObject(UserModel.class);

//                        FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl()
//                                        .addOnCompleteListener(t -> {
//                                            if(t.isSuccessful()){
//                                                Uri uri = t.getResult();
//////                                                holder.profilePic.setImageURI(uri);
//                                                Toast.makeText(context, "lây uri thành công", Toast.LENGTH_SHORT).show();
////                                                AndroidUtil.setProfilePic(context, uri,holder.profilePic);
//
//                                                if (uri != null) {
//                                                    AndroidUtil.setProfilePic(context, uri, holder.profilePic);
//                                                } else {
//                                                    Toast.makeText(context, "URI không hợp lệ", Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        });


                        holder.usernameText.setText(otherUserModel.getName());
                        String lastMessage = model.getLastMessage();
                        String formatedLastMessage = FirebaseUtil.formatLastMessage(lastMessage);
                        if(lastMessageSendByMe){
                            holder.lastMessageText.setText("You: " + formatedLastMessage );
                        }else{
                            holder.lastMessageText.setText(formatedLastMessage);
                        }

                        holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

                        holder.itemView.setOnClickListener(v -> {
                            Intent intent = new Intent(context, ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        });
                    }
                } );
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
            profilePic = itemView.findViewById(R.id.profileImageView);
        }
    }
}



