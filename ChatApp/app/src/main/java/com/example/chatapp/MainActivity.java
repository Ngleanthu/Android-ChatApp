//package com.example.chatapp;
//
//import android.os.Bundle;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }
//}
package com.example.chatapp;

import android.media.Image;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;


public class MainActivity extends AppCompatActivity {
    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;
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

    }
}

