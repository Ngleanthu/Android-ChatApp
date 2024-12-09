package com.example.chatapp.adapter;
import static com.example.chatapp.utils.YoutubeUtil.addYouTubeWebView;
import static com.example.chatapp.utils.YoutubeUtil.containsYouTubeLink;
import static com.example.chatapp.utils.YoutubeUtil.extractYouTubeId;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.models.ChatMessageModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {

    private final Context context;
    private final String currentUserId;  // Khai báo biến thành viên

    // Constructor có thêm currentUserId
    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context, String currentUserId) {
        super(options);
        this.context = context;
        this.currentUserId = currentUserId;  //000 Lưu giá trị vào biến thành viên
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
        Log.d("chat apd", "setupChatRecyclerView: " + currentUserId );
        boolean isCurrentUser = model.getSenderId().equals(currentUserId);
        if (isCurrentUser) {
            // Nếu là tin nhắn của người dùng hiện tại
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatTextView.setText(model.getMessage());
        } else {
            // Nếu là tin nhắn của người khác
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatTextView.setText(model.getMessage());
        }
        if (containsYouTubeLink(model.getMessage())) {
            String videoId = extractYouTubeId(model.getMessage());
            if (videoId != null) {
                Log.d("ChatRecyclerAdapter", "Video ID: " + videoId);

                LinearLayout layout= isCurrentUser ? holder.rightChatLayout : holder.leftChatLayout;
                addYouTubeWebView(layout, videoId, holder.itemView.getContext());
            }
        }

        // Chỉ cập nhật đã xem với tin nhắn mới nhất
        if (position == 0) {
            if (model.getSenderId().equals(currentUserId)) {
                if(model.isSeen()) {
                    // Nếu tin nhắn được gửi bởi người dùng hiện tại và đã được xem
                    holder.seenTextView.setVisibility(View.VISIBLE);
                    holder.seenTextView.setText("Seen");
                }
                else {
                    holder.seenTextView.setVisibility(View.VISIBLE);
                    holder.seenTextView.setText("Delivered");
                }
            }
            else {
                holder.seenTextView.setVisibility(View.GONE);
            }
        } else {
            holder.seenTextView.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatModelViewHolder(view);
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatLayout, rightChatLayout;
        TextView leftChatTextView, rightChatTextView, seenTextView;
        RelativeLayout mainLayout;

        ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            mainLayout = itemView.findViewById(R.id.main); // Tham chiếu đến mainLayout
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextView = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextView = itemView.findViewById(R.id.right_chat_textview);
            seenTextView = itemView.findViewById(R.id.seen_textview);
        }
    }
}
