package com.example.chatapp.adapter;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.activities.ChatActivity;
import com.example.chatapp.models.UserModel;
import com.example.chatapp.utils.AndroidUtil;

import java.util.ArrayList;
import java.util.List;

public class SearchUserRecyclerAdapter extends RecyclerView.Adapter<SearchUserRecyclerAdapter.UserModelViewHolder> {

    private List<UserModel> userList = new ArrayList<>();
    private Context context;

    public SearchUserRecyclerAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<UserModel> userList) {
        this.userList = userList;

        for (UserModel user : userList) {
            String name = user.getName();
            String userId = user.getUserId();
            Log.d("SearchUserRecyclerAdapter", name + " name " + userId + "userId");
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_recycle_row, parent, false);
        return new UserModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserModelViewHolder holder, int position) {
        UserModel model = userList.get(position);
        holder.usernameText.setText(model.getName());
        holder.phoneText.setText(model.getEmail());
        String imageUrl = model.getImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_default_profile_foreground) // Ảnh tạm
                    .into(holder.profilePic);
        }

        holder.itemView.setOnClickListener(v -> {
            Log.d("SearchUserRecyclerAdapter", "Item clicked: " + model.getUserId() + "=======" + model.getName());
            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView phoneText;
        ImageView profilePic;
        UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            phoneText = itemView.findViewById(R.id.phone_text);
            profilePic = itemView.findViewById(R.id.image_search_other_profile);
        }
    }
}
