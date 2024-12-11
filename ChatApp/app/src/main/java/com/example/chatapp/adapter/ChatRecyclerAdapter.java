package com.example.chatapp.adapter;
import static com.example.chatapp.utils.YoutubeUtil.addYouTubeWebView;
import static com.example.chatapp.utils.YoutubeUtil.containsYouTubeLink;
import static com.example.chatapp.utils.YoutubeUtil.extractYouTubeId;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.models.ChatMessageModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {

    private final Context context;
    private final String currentUserId;  // Khai báo biến thành viên
    private OnFileClickListener fileClickListener;

    public interface OnFileClickListener {
        void onFileClick(String fileName);
    }

    // Constructor có thêm currentUserId
    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context, String currentUserId, OnFileClickListener listener) {
        super(options);
        this.context = context;
        this.currentUserId = currentUserId;  //000 Lưu giá trị vào biến thành viên
        this.fileClickListener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
        Log.d("chat apd", "setupChatRecyclerView: " + currentUserId);
        boolean isCurrentUser = model.getSenderId().equals(currentUserId);
        Log.d("ChatRecyclerAdapter", "message: " + model.getMessage());
        Log.d("ChatRecyclerAdapter", "message: " + model.getType());

        // Reset layouts to avoid duplicates
        holder.rightChatLayout.removeAllViews();
        holder.leftChatLayout.removeAllViews();

        if (isCurrentUser) {
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatLayout.setVisibility(View.GONE);


            if (model.getType() != null && model.getType().equals("image")) {
                holder.rightChatTextView.setVisibility(View.GONE);
                Log.d("ChatRecyclerAdapter", "Image message: " + model.getMessage());
                addImageToLayout(holder.rightChatLayout, model.getMessage(), context);
            } else {
               // Add message text
                TextView messageView = new TextView(context);
                messageView.setTextColor(ContextCompat.getColor(context, R.color.white_color));

                // If user send file
                if (model.getFileUrl() != null) {
                    holder.rightChatLayout.setVisibility(View.VISIBLE);
                    messageView.setText((model.getFileUrl()));
                    holder.rightChatLayout.setOnClickListener(v -> {
                        if (fileClickListener != null) {
                            fileClickListener.onFileClick(model.getFileUrl());
                        }
                    });
                }else {
                    messageView.setText(model.getMessage());
                }
                holder.rightChatLayout.addView(messageView);
            }

            // Check for YouTube link and add WebView if necessary
            if (containsYouTubeLink(model.getMessage())) {
                Log.d("ChatRecyclerAdapter", "YouTube message: " + model.getMessage());
                String videoId = extractYouTubeId(model.getMessage());
                if (videoId != null) {
                    addYouTubeWebView(holder.rightChatLayout, videoId, context);
                }
            }

        } else {
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatLayout.setVisibility(View.GONE);

            if (model.getType() != null &&  model.getType().equals("image")) {
                holder.leftChatTextView.setVisibility(View.GONE);
                addImageToLayout(holder.leftChatLayout, model.getMessage(), context);
            } else {
                // Add message text
                TextView messageView = new TextView(context);
                messageView.setTextColor(ContextCompat.getColor(context, R.color.dark));

                // If user send file
                if (model.getFileUrl() != null) {
                    holder.leftChatLayout.setVisibility(View.VISIBLE);
                    messageView.setText((model.getFileUrl()));
                    holder.leftChatLayout.setOnClickListener(v -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getFileUrl()));
                        context.startActivity(intent);
                    });
                }else {
                    messageView.setText(model.getMessage());
                }

                holder.leftChatLayout.addView(messageView);
            }

            // Check for YouTube link and add WebView if necessary
            if (containsYouTubeLink(model.getMessage())) {
                Log.d("ChatRecyclerAdapter", "YouTube message: " + model.getMessage());
                String videoId = extractYouTubeId(model.getMessage());
                if (videoId != null) {
                    addYouTubeWebView(holder.leftChatLayout, videoId, context);
                }
            }
        }

        // Handle seen/delivered text for the last message
        if (position == 0) {
            if (model.getSenderId().equals(currentUserId)) {
                if (model.isSeen()) {

                    holder.seenTextView.setVisibility(View.VISIBLE);
                    holder.seenTextView.setText("Seen");
                } else {
                    holder.seenTextView.setVisibility(View.VISIBLE);
                    holder.seenTextView.setText("Delivered");
                }
            } else {
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


    public static void addImageToLayout(LinearLayout parentLayout, String imageUrl, Context context) {
        int width = (int) (300 * context.getResources().getDisplayMetrics().density);
        int height = (int) (200 * context.getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                width,
                height
        );

        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.e("ImageView", "Invalid image URL.");
            return;
        }

        // Tạo một ImageView mới
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(layoutParams);

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.background_input)
                .error(R.drawable.background_icon)
                .into(imageView);

        parentLayout.addView(imageView);
    }



}
