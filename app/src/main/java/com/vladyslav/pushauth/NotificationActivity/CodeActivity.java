package com.vladyslav.pushauth.NotificationActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vladyslav.pushauth.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

public class CodeActivity extends AppCompatActivity {

    private TextView timer;
    private ProgressBar progressBar;
    private String privateKey;
    private String publicKey;
    private String hash;
    SharedPreferences sharedPreferences;
    Response response = new Response();
    CountDownTimer waitTimer;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        pref = getSharedPreferences("pausePass", Context.MODE_PRIVATE);
      //  pref.edit().putString("var", "code").apply();

        TextView app = (TextView) findViewById(R.id.app);

        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);

        timer = (TextView) findViewById(R.id.progress);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        TextView code = (TextView) findViewById(R.id.code);


        if(sharedPreferences.contains("publicKey")) {
            publicKey = sharedPreferences.getString("publicKey", "");
        }

        if(sharedPreferences.contains("privateKey")) {
            privateKey = sharedPreferences.getString("privateKey", "");
        }

               hash = getIntent().getExtras().getString("hash");
        String codes = getIntent().getExtras().getString("code");
        String appName = getIntent().getExtras().getString("appName");

        Log.d("Notif code", codes);
        Log.d("Notif code", appName);

        code.setText(codes);
        app.setText(appName);

        response.execute();

       waitTimer = new CountDownTimer(30000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText("" + millisUntilFinished / 1000);
                progressBar.setProgress((int) millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                pref.edit().putBoolean("push", true).apply();
                //pref.edit().putString("var", "code").apply();
                Intent intent = new Intent();
                intent.putExtra("answerPush", true);
                setResult(RESULT_OK, intent);
                finish();
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pref.edit().putString("var", "code").apply();
    }

    public void onClickOk(View view) {
        waitTimer.cancel();
        pref.edit().putBoolean("push", true).apply();
        //pref.edit().putString("var", "code").apply();
        Intent intent = new Intent();
        intent.putExtra("answerPush", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    private class Response extends AsyncTask<Void, Void, Void> {
        String code, message;
        StringBuffer result = new StringBuffer();

        @Override
        protected Void doInBackground(Void... params) {
            OutputStreamWriter wr;
            BufferedReader rd;
            String line;

            try {
                JSONObject jobj = new JSONObject();
                jobj.put("hash", hash);
                jobj.put("answer", true);
                Log.d("Response", jobj.toString());


                Log.d("Response private", privateKey);
                Log.d("Response public", publicKey);
                String body = base64_encode(jobj.toString());
                String sign = hash_hmac(jobj.toString(), privateKey);
                String data = body + "." + sign;
                data = data.replaceAll("\n", "");

                Log.d("Response data", data);

                JSONObject jsobj = new JSONObject();
                jsobj.put("pk", publicKey);
                jsobj.put("data", data);
                Log.d("Response", jsobj.toString());

                HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.pushauth.io/push/answer").openConnection();
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
                Log.d("Response mesaage", message);

                rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                wr.close();
                rd.close();
                Log.d("Response result", result.toString());
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private String base64_encode(String str){
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
                retVal = Base64.encodeToString(digest,Base64.NO_WRAP);

                Log.i(TAG, "String in Base64: " + base_string_base64);
                Log.i(TAG, "result: " + retVal);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return retVal;
        }
    }
}
