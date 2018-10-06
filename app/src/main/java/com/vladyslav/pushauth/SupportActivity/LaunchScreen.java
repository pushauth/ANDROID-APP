package com.vladyslav.pushauth.SupportActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.vladyslav.pushauth.MainActivities.LoginActivity;
import com.vladyslav.pushauth.MainActivities.MainActivity;

public class LaunchScreen extends AppCompatActivity {

    SharedPreferences prefs, sp;
    boolean IsAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("launch", Context.MODE_PRIVATE);
        sp = getSharedPreferences("access", Context.MODE_PRIVATE);
        IsAccess = sp.getBoolean("IsAccess", false);
        Log.d("access", String.valueOf(IsAccess));

        if (!prefs.getBoolean("is_start", false) || !IsAccess) {
            prefs.edit().putBoolean("is_start", true).apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();

        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

}
