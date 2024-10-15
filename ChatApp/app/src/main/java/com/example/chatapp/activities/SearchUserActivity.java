package com.example.chatapp.activities;

import android.media.Image;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.SearchUserRecyclerAdapter;


public class SearchUserActivity extends AppCompatActivity {
    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;

    SearchUserRecyclerAdapter adapter
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_user);

        searchInput = findViewById(R.id.search_username_input);
        searchButton = findViewById(R.id.search_user_btn);
        backButton = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.search_user_recycler_view);
        searchInput.requestFocus();
        backButton.setOnClickListener(v->{
            onBackPressed();
        });
        searchButton.setOnClickListener(v->{
            String searchTerm = searchInput.getText().toString();
            if(searchTerm.isEmpty()||searchTerm.length()<3){
                searchInput.setError("Invalid username");
                return;
            }
            setupSearchReyclerView(searchTerm);
        });
    }
    void setupSearchReyclerView(String searchTerm){
adapter=new SearchUserRecyclerAdapter(,getApplicationContext());
recyclerView.setLayoutManager(new LinearLayoutManager((this)));
recyclerView.setAdapter(adapter);
adapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.startListening();
    }
}

