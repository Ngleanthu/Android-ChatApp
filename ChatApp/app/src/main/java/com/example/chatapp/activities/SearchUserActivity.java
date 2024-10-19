package com.example.chatapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.SearchUserRecyclerAdapter;
import com.example.chatapp.models.UserModel;
import com.example.chatapp.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;


public class SearchUserActivity extends AppCompatActivity {
    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;

    SearchUserRecyclerAdapter adapter;
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
            getOnBackPressedDispatcher().onBackPressed();
        });
        searchButton.setOnClickListener(v->{
            String searchTerm = searchInput.getText().toString();
            if(searchTerm.isEmpty()){
                searchInput.setError("Invalid username");
                return;
            }
            setupSearchRecylerView(searchTerm);
        });
    }
    void setupSearchRecylerView(String searchTerm) {
        Log.d("SearchUser", "Searching for: " + searchTerm);

        Query query = FirebaseUtil.allUserCollectionReference()
                .orderBy("name")  // Tìm kiếm theo trường 'name'
                .startAt(searchTerm)
                .endAt(searchTerm + "\uf8ff");

        // Kiểm tra query trả về kết quả gì
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                // Nếu có kết quả
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    // Lấy từng tài liệu và log nó ra
                    UserModel user = documentSnapshot.toObject(UserModel.class);
                    if (user != null) {
                        Log.d("SearchUser", "Found user: " + user.getName() + " with email: " + user.getEmail());
                    }
                }
            } else {
                Log.d("SearchUser", "No users found for search term: " + searchTerm);
            }
        }).addOnFailureListener(e -> {
            // Xử lý lỗi nếu có
            Log.e("SearchUser", "Error fetching search results", e);
        });

        // Tiếp tục khởi tạo RecyclerView Adapter như bình thường
        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        adapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

