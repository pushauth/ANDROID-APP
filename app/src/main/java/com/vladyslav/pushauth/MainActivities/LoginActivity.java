package com.vladyslav.pushauth.MainActivities;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vladyslav.pushauth.SupportActivity.PinCode;
import com.vladyslav.pushauth.R;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {


    private EditText mEditText1;
    Button btn;
    SharedPreferences sharedPreferences, sp, pref;
    JSONObject jsonObject = new JSONObject();
    boolean isAccess = false;
    String token, deviceId, androidRelease, deviceModel, deviceName;
    int check = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEditText1 = (EditText) findViewById(R.id.editText);
        btn = (Button) findViewById(R.id.button);

        deviceId = UUID.randomUUID().toString();
        Log.i("UUID", deviceId);
        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
        pref = getSharedPreferences("access", Context.MODE_PRIVATE);
        pref.edit().putBoolean("IsAccess", isAccess).apply();

        androidRelease = androidVer();
        deviceModel = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
        Log.d("device Name", deviceModel);


    }

    private String androidVer(){
        String release = Build.VERSION.RELEASE;
        int ver = Build.VERSION.SDK_INT;
        Log.d("Android Ver", release + "("+ String.valueOf(ver) +")");
        return release + "("+ String.valueOf(ver) +")";
    }

    public String phoneName() {
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        return myDevice.getName();
        //return Settings.System.getString(getContentResolver(), "device_name");
    }


    public void onClick(View view) {
        token = sp.getString("token", "1");
        Log.d("token", token);
        Log.d("deviceName", phoneName());
       if (!isAccess){
            if (mEditText1.getText().length() != 0) {
                try {
                    String email = String.valueOf(mEditText1.getText());
                    jsonObject.put("email", email);
                    jsonObject.put("device_uuid", deviceId);
                    jsonObject.put("device_token", token);
                    jsonObject.put("device_type", "android");
                    jsonObject.put("device_name", phoneName());
                    jsonObject.put("device_vendor", deviceModel);
                    jsonObject.put("device_os_detail", androidRelease);
                    Request request = new Request();
                    request.execute();
                        if (check ==0 ) {
                            mEditText1.setClickable(false);
                            btn.setVisibility(View.INVISIBLE);
                            Toast toast = Toast.makeText(getApplicationContext(), R.string.check_email, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM, 0, 0);
                            toast.show();

                        }
                }catch (JSONException e){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.error_request,
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.show();
                    btn.setVisibility(View.VISIBLE);
                    mEditText1.setClickable(true);
                    check = 0;
                }
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.email_cant_empty, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.show();
            }
       }
       else {
           Intent intent = new Intent(this, PinCode.class);
           startActivity(intent);
           finish();
       }

    }

    public void changeScreen(){
        Intent intent = new Intent(this, PinCode.class);
        startActivity(intent);
        finish();
    }


    private class Request extends AsyncTask<Void, Void, Void> {
        String message = "", code = "";
        StringBuffer result = new StringBuffer();



        @Override
        protected Void doInBackground(Void... params) {
            OutputStreamWriter wr;
            BufferedReader rd;
            String line;

            try {
                HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.pushauth.io/auth").openConnection();
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
            } catch (Exception e) {
                Log.d("Error req", message);
                Log.d("Err req code", code);
               // Toast toast = Toast.makeText(getApplicationContext(), "Error in request", Toast.LENGTH_LONG);
                //toast.setGravity(Gravity.BOTTOM, 0, 0);
                //toast.show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            unPackageJson(result.toString());
        }

        public void unPackageJson(String data) {
            String message = "";
            SharedPreferences.Editor edit = sharedPreferences.edit();
            try {
                JSONObject authorisation = new JSONObject(data);
                message = authorisation.getString("message");
                isAccess = authorisation.getBoolean("is_access");
                if (isAccess) {
                    edit.putString("publicKey", authorisation.getString("public_key")).apply();
                    Log.d("PK", authorisation.getString("public_key"));
                    changeScreen();
                }

            } catch (Exception e) {
                Log.d("Err json", message);
                //Toast toast = Toast.makeText(getApplicationContext(), "Error in unpackage JSON", Toast.LENGTH_LONG);
                //toast.setGravity(Gravity.BOTTOM, 0, 0);
                //toast.show();
            }

            Log.d("Message", message);
            if( check == 0){
            try {
                Thread.sleep(5000);
            }catch(Exception e){};
            btn.setVisibility(View.VISIBLE);
            mEditText1.setClickable(true);
            check ++;
            }
            Log.d("access login", String.valueOf(isAccess));
            if (isAccess){
                pref.edit().putBoolean("IsAccess", isAccess).apply();
                Toast toast = Toast.makeText(getApplicationContext(), R.string.auth_success, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.show();
            }
        }

    }
}


