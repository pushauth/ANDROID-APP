package com.vladyslav.pushauth.SupportActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vladyslav.pushauth.MainActivities.MainActivity;
import com.vladyslav.pushauth.R;

public class PinCode extends AppCompatActivity {

    ImageView  fcircle1, fcircle2, fcircle3, fcircle4;

    Button buttonNum1, buttonNum2, buttonNum3, buttonNum4, buttonNum5,
            buttonNum6, buttonNum7, buttonNum8, buttonNum9, buttonNum0;

    TextView createPin, enterPin;

    SharedPreferences sharedPreferences, pref;

    StringBuilder mBuilder;
    int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_code);

        createPin = (TextView) findViewById(R.id.createPin);
        enterPin = (TextView) findViewById(R.id.enterPin);

        fcircle1 = (ImageView) findViewById(R.id.fcircle1);
        fcircle2 = (ImageView) findViewById(R.id.fcircle2);
        fcircle3 = (ImageView) findViewById(R.id.fcircle3);
        fcircle4 = (ImageView) findViewById(R.id.fcircle4);

        buttonNum1 = (Button) findViewById(R.id.buttonNum1);
        buttonNum2 = (Button) findViewById(R.id.buttonNum2);
        buttonNum3 = (Button) findViewById(R.id.buttonNum3);
        buttonNum4 = (Button) findViewById(R.id.buttonNum4);
        buttonNum5 = (Button) findViewById(R.id.buttonNum5);
        buttonNum6 = (Button) findViewById(R.id.buttonNum6);
        buttonNum7 = (Button) findViewById(R.id.buttonNum7);
        buttonNum8 = (Button) findViewById(R.id.buttonNum8);
        buttonNum9 = (Button) findViewById(R.id.buttonNum9);
        buttonNum0 = (Button) findViewById(R.id.buttonNum0);

        mBuilder = new StringBuilder();

        sharedPreferences = getSharedPreferences("pincode", Context.MODE_PRIVATE);
        pref = getSharedPreferences("pausePass", Context.MODE_PRIVATE);

        Log.d("start ", String.valueOf(sharedPreferences.getInt("start", 0)));

       if (sharedPreferences.getInt("start", 0) == 1){
           Log.d("start ", String.valueOf(sharedPreferences.getInt("start", 0)));
           createPin.setVisibility(View.INVISIBLE);
           enterPin.setVisibility(View.VISIBLE);
       }
       else {
           Log.d("start ", String.valueOf(sharedPreferences.getInt("start", 0)));
           createPin.setVisibility(View.VISIBLE);
           enterPin.setVisibility(View.INVISIBLE);
       }

    }

    @Override
    protected void onResume() {
        super.onResume();
        pref.edit().putBoolean("pause", false).apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pref.edit().putBoolean("pause", false).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pref.edit().putBoolean("pause", true).apply();
        pref.edit().putString("var", "").apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        pref.edit().putBoolean("pause", true).apply();
        pref.edit().putString("var", "").apply();
    }

    @Override
    public void onBackPressed() { }

    String add = "ADD ";

    private void init(String pin){
       if (sharedPreferences.getString("pass", "").equalsIgnoreCase(pin)){
           Log.d("LOGIN ","OK" );
           Intent intent = new Intent();
           intent.putExtra("answerPinCode", true);
           setResult(RESULT_OK, intent);
           this.finish();
       }
       else {
           Log.d("LOGIN", "ERROR");
           mBuilder.delete(0,4);
           Log.d("mBuilder ", mBuilder.toString());
           fcircle1.setVisibility(View.INVISIBLE);
           fcircle2.setVisibility(View.INVISIBLE);
           fcircle3.setVisibility(View.INVISIBLE);
           fcircle4.setVisibility(View.INVISIBLE);
           count = 0;
       }

    }

    public void onClick(View view) {
        int id = view.getId();
        if (count != 4){
        if (id == R.id.buttonNum0){
            mBuilder.append("0");
            Log.d(add, "0");}
        else if (id == R.id.buttonNum1) {
            mBuilder.append("1");
            Log.d(add, "1");
        }
        else if (id == R.id.buttonNum2) {
            mBuilder.append("2");
            Log.d(add, "2");
        }
        else if (id == R.id.buttonNum3) {
            mBuilder.append("3");
            Log.d(add, "3");
        }
        else if (id == R.id.buttonNum4) {
            mBuilder.append("4");
            Log.d(add, "4");
        }
        else if (id == R.id.buttonNum5) {
            mBuilder.append("5");
            Log.d(add, "5");
        }
        else if (id == R.id.buttonNum6) {
            mBuilder.append("6");
            Log.d(add, "6");
        }
        else if (id == R.id.buttonNum7) {
            mBuilder.append("7");
            Log.d(add, "7");
        }
        else if (id == R.id.buttonNum8) {
            mBuilder.append("8");
            Log.d(add, "8");
        }
        else if (id == R.id.buttonNum9) {
            mBuilder.append("9");
            Log.d(add, "9");
        }
        Log.d("pass ", mBuilder.toString());
        count = mBuilder.length();
            Log.d("Count ", String.valueOf(count));
            visible();
            Log.d("start ", String.valueOf(sharedPreferences.getInt("start", 0)));

        if (count == 4) {
            if (sharedPreferences.getInt("start", 0) == 0) {
                sharedPreferences.edit().putString("pass", mBuilder.toString()).apply();
                sharedPreferences.edit().putInt("start", 1).apply();
                Log.d("start ", String.valueOf(sharedPreferences.getInt("start", 0)));
                Intent intent = new Intent(this, MainActivity.class );
                //this.finish();
                pref.edit().putString("var", "code").apply();
                pref.edit().putBoolean("pause", false).apply();
                startActivity(intent);
            } else
                init(mBuilder.toString());
        }

        }
    }

    private void visible(){
        if (count == 1)
            fcircle1.setVisibility(View.VISIBLE);
        if (count == 2)
            fcircle2.setVisibility(View.VISIBLE);
        if (count == 3)
            fcircle3.setVisibility(View.VISIBLE);
        if (count == 4)
         fcircle4.setVisibility(View.VISIBLE);
    }

    public void delete(View view) {
        mBuilder.deleteCharAt(mBuilder.length()-1);
        Log.d("pass ", mBuilder.toString());
        count = mBuilder.length();
        Log.d("Count ", String.valueOf(count));
        invisible();
    }

    private void invisible(){
        if (count == 3)
            fcircle4.setVisibility(View.INVISIBLE);
        if (count == 2)
            fcircle3.setVisibility(View.INVISIBLE);
        if (count == 1)
            fcircle2.setVisibility(View.INVISIBLE);
        if (count == 0)
            fcircle1.setVisibility(View.INVISIBLE);
    }
}
