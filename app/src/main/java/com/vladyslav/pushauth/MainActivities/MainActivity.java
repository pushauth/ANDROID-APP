package com.vladyslav.pushauth.MainActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.vladyslav.pushauth.NotificationActivity.CodeActivity;
import com.vladyslav.pushauth.NotificationActivity.QuestionActivity;
import com.vladyslav.pushauth.SupportActivity.PinCode;
import com.vladyslav.pushauth.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    SharedPreferences pref, sharedPreferences, sp;
    String appName, mode, codes, hash;
    boolean answerPush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("pausePass", Context.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        sp = getSharedPreferences("qr", Context.MODE_PRIVATE);

        {
            pref.edit().putString("var", "code").apply();
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 50);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("question pref", pref.getString("var", ""));

        if (pref.getBoolean("pause", false)) {
            if (pref.getBoolean("push", false)) {
                pref.edit().putString("var", "").apply();
            }

            else {
                Intent intent = new Intent(this, PinCode.class);
                pref.edit().putString("var", "code").apply();
                pref.edit().putBoolean("push", false).apply();
                startActivity(intent);
            }
        }
        if (pref.getString("response", "").equalsIgnoreCase("pin")){
            Log.d("onResume", "startrequest");
            Response response = new Response();
            response.execute();
            pref.edit().putString("response", "").apply();
        }

        if (!sp.getString("qrCode", "1").equalsIgnoreCase("")) {
            Log.d("qrCode", "WORK!!");
            Request request = new Request();
            request.execute();
            sp.edit().putString("qrCode", "").apply();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        pref.edit().putBoolean("pause", true).apply();
        pref.edit().putBoolean("push", false).apply();
        if (pref.getString("var", "").equalsIgnoreCase("code")) {
            pref.edit().putBoolean("pause", false).apply();
            pref.edit().putString("response", "pin").apply();
            pref.edit().putString("var", "").apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pref.edit().putString("var", "").apply();
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        pref.edit().putString("var", "code").apply();
    }

    public void Scan(View view) {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
        pref.edit().putString("var", "code").apply();
    }

    private class Response extends AsyncTask<Void, Void, Void> {
        String code, message;
        StringBuffer result = new StringBuffer();
        private String publicKey = sharedPreferences.getString("publicKey", "");
        private String privateKey = sharedPreferences.getString("privateKey", "");


        @Override
        protected Void doInBackground(Void... params) {
            OutputStreamWriter wr;
            BufferedReader rd;
            String line;


            try {

                Log.d("Response private", privateKey);
                Log.d("Response public", publicKey);
                String body = base64_encode("bsOq71KHbzLug70MzqIdJx76SavmEyUF");
                String sign = hash_hmac("bsOq71KHbzLug70MzqIdJx76SavmEyUF", privateKey);
                String data = body + "." + sign;
                data = data.replaceAll("\n", "");

                Log.d("Response data", data);

                JSONObject jsobj = new JSONObject();
                jsobj.put("pk", publicKey);
                jsobj.put("data", data);
                Log.d("Response", jsobj.toString());

                HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.pushauth.io/push/index").openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content_Type", "application/json");
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setConnectTimeout(100000);
                con.connect();

                wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(jsobj.toString());
                wr.flush();

                code = String.valueOf(con.getResponseCode());
                message = con.getResponseMessage();
                Log.d("Response code", code);
                Log.d("Response message", message);

                rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                wr.close();
                rd.close();
                Log.d("Response result", result.toString());
                unPackedResponse();
            } catch (Exception e) {
                Log.d("Response error", "error");

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private String base64_encode(String str) {
            return Base64.encodeToString(str.getBytes(), 0);
        }

        private String hash_hmac(String base_string, String key) {
            final String TAG = "hash_hmac";
            String retVal = "";
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secret = new SecretKeySpec(key.getBytes("UTF-8"), mac.getAlgorithm());
                mac.init(secret);

                String base_string_base64 = Base64.encodeToString(base_string.getBytes(), Base64.NO_WRAP);
                byte[] digest = mac.doFinal(base_string_base64.getBytes());
                retVal = Base64.encodeToString(digest, Base64.DEFAULT);

                Log.i(TAG, "String in Base64: " + base_string_base64);
                Log.i(TAG, "result: " + retVal);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return retVal;
        }

        private void unPackedResponse() {
            try {
                JSONObject json = new JSONObject(result.toString());
                String jdata = json.getString("data");
                String[] data = jdata.split("\\.");
                String body = data[0];
                String str = new String(base64_decode(body), "UTF-8");
                Log.d("data response", str);
                JSONObject list = new JSONObject(str);
                int total = list.getInt("total");
                Log.d("total", String.valueOf(total));
                if (total != 0) {
                    JSONArray index =  list.getJSONArray("index");
                    Log.d("Array", index.toString());

                    for (int n = 0; n < total;) {
                        JSONObject push = index.getJSONObject(n);
                        Log.d("push", push.toString());
                        appName = push.getString("app_name");
                        Log.d("appName", appName);
                        hash = push.getString("req_hash");
                        Log.d("hash", hash);
                        mode = push.getString("mode");
                        Log.d("mode", mode);
                        if (mode.equalsIgnoreCase("code")) {
                            codes = push.getString("code");
                        }
                        Thread.sleep(1000);
                        sendNotification(mode);
                                
                        do {
                            Thread.sleep(1000);
                        }while(!answerPush);

                        Log.d("push", "true");
                        index.remove(n);
                        total--;
                        answerPush = false;
                        Log.d("array", index.toString());
                        Log.d("total", String.valueOf(total));
                    }

                }
            } catch (Exception e) {
                Log.d("error", "error");
            }
        }

        private byte[] base64_decode(String str) {
            return Base64.decode(str, Base64.NO_WRAP);
        }

    }
        private void sendNotification(String mode) {
            Log.d("sendNotification", "Sending...");

            if (mode.equalsIgnoreCase("push")) {
                Intent intent = new Intent(this, QuestionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("appName", appName);
                intent.putExtra("hash", hash);
                startActivityForResult(intent, 1);
                Log.d("push", "send");
            } else {
                Intent intent = new Intent(this, CodeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("code", codes);
                intent.putExtra("appName", appName);
                intent.putExtra("hash", hash);
                startActivityForResult(intent, 1);
                Log.d("code", "send");
            }
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null){return;}
        answerPush = data.getBooleanExtra("answerPush", false);
    }

    private void sendToast(String num){
        if (num.equalsIgnoreCase("200")){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.auth_success, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.show();
        }
        else if (num.equalsIgnoreCase("403")) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.access_denied, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.show();
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.error_try_again, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.show();
        }
    }

    private class Request extends AsyncTask<Void, Void, Void> {
        String code, message;
        StringBuffer result = new StringBuffer();
        private String publicKey = sharedPreferences.getString("publicKey", "");
        private String privateKey = sharedPreferences.getString("privateKey", "");
        private String hash = sp.getString("qrCode", "");


        @Override
        protected Void doInBackground(Void... params) {
            OutputStreamWriter wr;
            BufferedReader rd;
            String line;


            try {
                Log.d("hash qr", hash);
                JSONObject json = new JSONObject();
                json.put("hash", hash);

                Log.d("hash qr json", json.toString());
                Log.d("Request qr private", privateKey);
                Log.d("Request qr public", publicKey);
                String body = base64_encode(json.toString());
                String sign = hash_hmac(json.toString(), privateKey);
                String data = body + "." + sign;
                data = data.replaceAll("\n", "");

                Log.d("Request qr data", data);

                JSONObject jsobj = new JSONObject();
                jsobj.put("pk", publicKey);
                jsobj.put("data", data);
                Log.d("Request", jsobj.toString());

                HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.pushauth.io/qr/store").openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content_Type", "application/json");
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setConnectTimeout(100000);
                con.connect();

                wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(jsobj.toString());
                wr.flush();

                code = String.valueOf(con.getResponseCode());
                message = con.getResponseMessage();
                Log.d("Request code", code);
                Log.d("Request message", message);

                rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                wr.close();
                rd.close();
                sendToast(code);
                Log.d("Request result", result.toString());
                sp.edit().putString("qrCode", "").apply();
            } catch (Exception e) {
                Log.d("Request error", "error");

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private String base64_encode(String str) {
            return Base64.encodeToString(str.getBytes(), 0);
        }

        private String hash_hmac(String base_string, String key){
            final String TAG = "hash_hmac";
            String retVal = "";
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secret = new SecretKeySpec(key.getBytes("UTF-8"), mac.getAlgorithm());
                mac.init(secret);

                String base_string_base64 = Base64.encodeToString(base_string.getBytes(), Base64.NO_WRAP);
                byte[] digest = mac.doFinal(base_string_base64.getBytes());
                retVal = Base64.encodeToString(digest,Base64.DEFAULT);

                Log.i(TAG, "String in Base64: " + base_string_base64);
                Log.i(TAG, "result: " + retVal);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return retVal;
        }


    }

}
