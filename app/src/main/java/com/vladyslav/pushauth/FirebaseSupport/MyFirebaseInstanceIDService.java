package com.vladyslav.pushauth.FirebaseSupport;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {


    SharedPreferences sharedPreferences;
    private static final String TAG = "MyFirebaseIIDService";


    @Override
    public void onTokenRefresh() {
        sharedPreferences = getSharedPreferences("ID", Context.MODE_PRIVATE);
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();
        sharedPreferences.edit().putString("token", token).apply();
        Log.d(TAG, "Refreshed token: " + token);
    }

}
