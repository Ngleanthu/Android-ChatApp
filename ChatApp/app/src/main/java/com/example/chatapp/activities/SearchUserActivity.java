package com.example.chatapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.example.chatapp.R;
import com.example.chatapp.adapter.SearchUserRecyclerAdapter;
import com.example.chatapp.models.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchUserActivity extends AppCompatActivity {
    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;
    TextView emptyMessage;
    SearchUserRecyclerAdapter adapter;
    Index algoliaIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_user);

        // Initialize Algolia client and index
        Client client = new Client("BDDQ7K86N2", "6784c8e2b2e489805d64565abd2db081");
        algoliaIndex = client.getIndex("users_index");
        emptyMessage = findViewById(R.id.empty_message);
        searchInput = findViewById(R.id.search_username_input);
        searchButton = findViewById(R.id.search_user_btn);
        backButton = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.search_user_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchUserRecyclerAdapter(this);
        recyclerView.setAdapter(adapter);

        searchInput.requestFocus();

        backButton.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString();
            if (searchTerm.isEmpty()) {
                searchInput.setError("Invalid username");
                return;
            }
            performSearch(searchTerm);
        });
    }

    private void performSearch(String searchTerm) {
        Query query = new Query(searchTerm)
                .setAttributesToRetrieve( "fcmToken", "userId", "name", "email", "image") // Add required attributes
                .setHitsPerPage(50); // Limit the results

        algoliaIndex.searchAsync(query, (jsonObject, exception) -> {
            if (exception != null) {
                Log.e("SearchUser", "Error fetching search results", exception);
                return;
            }

            try {
                Log.d("SearchUser", "Raw JSON response: " + (jsonObject != null ? jsonObject.toString() : "null"));
                List<UserModel> userList = parseResults(jsonObject);

                if (userList == null || userList.isEmpty()) {
                    emptyMessage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyMessage.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setData(userList);
                }
            } catch (JSONException e) {
                Log.e("SearchUser", "Error parsing search results", e);
            }
        });
    }

    private List<UserModel> parseResults(JSONObject jsonObject) throws JSONException {
        List<UserModel> results = new ArrayList<>();
        JSONArray hits = jsonObject.getJSONArray("hits");

        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i);

            UserModel user = new UserModel();
            user.setName(hit.optString("name"));
            user.setEmail(hit.optString("email"));
            user.setImage(hit.optString("image"));
            user.setUserId(hit.optString("userId"));
            user.setFcmToken(hit.optString("fcmToken"));

            results.add(user);
        }
        return results;
    }
}
