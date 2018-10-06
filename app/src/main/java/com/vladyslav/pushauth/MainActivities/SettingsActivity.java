package com.vladyslav.pushauth.MainActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.vladyslav.pushauth.SupportActivity.PinCode;
import com.vladyslav.pushauth.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences, prefs, sp, pref;
    String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        

        sharedPreferences = getSharedPreferences("pincode", Context.MODE_PRIVATE);
        prefs = getSharedPreferences("launch", Context.MODE_PRIVATE);
        sp = getSharedPreferences("user", Context.MODE_PRIVATE);
        pref = getSharedPreferences("pausePass", Context.MODE_PRIVATE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (pref.getBoolean("pause", false)) {
            Intent intent = new Intent(this, PinCode.class);
            pref.edit().putString("var", "code").apply();
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pref.edit().putBoolean("pause", true).apply();
        if (pref.getString("var", "").equalsIgnoreCase("code")) {
            pref.edit().putBoolean("pause", false).apply();
            pref.edit().putString("var", "").apply();
        }

    }



    public void onClickBack(View view) {
        pref.edit().putString("var", "code").apply();
        this.finish();
    }

    public void onClickLogOut(View view) {
           Request request = new Request();
           request.execute();

    }

    public void Change(View view) {
        sharedPreferences.edit().putInt("start", 0).apply();
        Intent intent = new Intent(this, PinCode.class);
        startActivity(intent);
    }

    public void changeScreen(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private class Request extends AsyncTask<Void, Void, Void> {
        String message = "";//, code = "";
        StringBuffer result = new StringBuffer();
        JSONObject jsonObject = new JSONObject();


        @Override
        protected Void doInBackground(Void... params) {
            OutputStreamWriter wr;
            BufferedReader rd;
            String line;

            try {
                Log.d("publicKeyREq", sp.getString("publicKey", ""));
                jsonObject.put("pk",sp.getString("publicKey", ""));
                HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.pushauth.io/logout").openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content_Type", "application/json");
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setConnectTimeout(100000);
                con.connect();
                wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(String.valueOf(jsonObject));
                wr.flush();
                code = String.valueOf(con.getResponseCode());
                message = con.getResponseMessage();
                rd = new BufferedReader(new InputStreamReader(con.getInputStream()));

                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                wr.close();
                rd.close();
                Log.d("response log out", result.toString());
            } catch (Exception e) {
                //Toast toast = Toast.makeText(getApplicationContext(), "Error in request", Toast.LENGTH_LONG);
                //toast.setGravity(Gravity.BOTTOM, 0, 0);
                //toast.show();
                Log.d("response log out", message);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (code.equalsIgnoreCase("200")) {
                SharedPreferences.Editor e = sharedPreferences.edit();
                e.clear().apply();
                Log.d("pincode", sharedPreferences.getString("pass", ""));
                SharedPreferences.Editor ed = prefs.edit();
                ed.clear().apply();
                Log.d("launch", String.valueOf(prefs.getBoolean("is_start", false)));
                SharedPreferences.Editor editor = sp.edit();
                editor.clear().apply();
                Log.d("publicKey", sp.getString("publicKey", ""));
                changeScreen();
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), R.string.error_in_logout, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.show();
            }
        }

    }
}
