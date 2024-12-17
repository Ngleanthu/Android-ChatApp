package com.example.chatapp.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapp.R;
import com.example.chatapp.adapter.RecentCharRecyclerAdapter;
import com.example.chatapp.models.ChatRoomModel;
import com.example.chatapp.utils.Constants;
import com.example.chatapp.utils.FirebaseUtil;
import com.example.chatapp.utils.PreferenceManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class ChatFragment extends Fragment {
  PreferenceManager preferenceManager;

    RecentCharRecyclerAdapter adapter;
    RecyclerView recyclerView;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
       setupRecyclerView();

        return view;
    }


    void setupRecyclerView() {
        Query query = FirebaseUtil.allChatroomCollectionReference()
                        .whereArrayContains("userId", preferenceManager.getString(Constants.KEY_USER_ID))
                                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatRoomModel> options = new FirestoreRecyclerOptions.Builder<ChatRoomModel>()
                .setQuery(query, ChatRoomModel.class).build();

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    ChatRoomModel chatRoom = document.toObject(ChatRoomModel.class);
                    Log.d("FirestoreData", "ChatRoom: " + chatRoom.toString());
                }
            } else {
                Log.e("FirestoreData", "Error getting documents: ", task.getException());
            }
        });

        adapter = new RecentCharRecyclerAdapter(options, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.notifyDataSetChanged();
    }


}