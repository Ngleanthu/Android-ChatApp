package com.example.chatapp.firebase;

import android.content.Context;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AccessToken {
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = {MESSAGING_SCOPE};
    public String getAccessToken(Context context) {
        try {
//            
            InputStream serviceAccount = context.getAssets().open("service-account.json");
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccount)
                    .createScoped(Arrays.asList(SCOPES));

            googleCredentials.refresh();

            return googleCredentials.getAccessToken().getTokenValue();
        }catch(IOException e) {
            Log.e("getAccessToken", "Get access token failed: "+ e.getMessage());
            return null;
        }
    }
}
