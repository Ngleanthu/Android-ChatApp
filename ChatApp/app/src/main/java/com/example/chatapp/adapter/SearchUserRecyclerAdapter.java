package com.example.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {

    Context context;

    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context){
    super(options);
    this.context=context;
}
@Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
    holder.usernameText.setText(model.getUsername());
    holder.phoneText.setText(model.getPhone());

}

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.search_user_recycle_row,parent,false);
        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView phoneText;
ImageView profilePic;
        UserModelViewHolder(@NonNull View itemView) {
    super(itemView);
    usernameText = itemView.findViewById(R.id.user_name_text);
    phoneText = itemView.findViewById(R.id.phone_text);
    profilePic = itemView.findViewById(R.id.profile_pic_image_view);
}
    }
}
