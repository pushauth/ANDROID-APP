package com.vladyslav.pushauth.MainActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.vladyslav.pushauth.SupportActivity.PinCode;
import com.vladyslav.pushauth.R;

public class ScanActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    private QRCodeReaderView qrCodeReaderView;
    SharedPreferences sp, sharedPreferences;

    ImageView flashOn, flashOff, camera;
    int cam = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        sp = getSharedPreferences("qr", Context.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("pausePass", Context.MODE_PRIVATE);


        flashOn = (ImageView) findViewById(R.id.flashOn);
        flashOff = (ImageView) findViewById(R.id.flashOff);
        //camera = (ImageView) findViewById(R.id.camera);

        qrCodeReaderView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        qrCodeReaderView.setOnQRCodeReadListener(this);

        qrCodeReaderView.setQRDecodingEnabled(true);
        qrCodeReaderView.setAutofocusInterval(5000L);
        // Use this function to enable/disable Torch
        //qrCodeReaderView.setTorchEnabled(true);
        // Use this function to set front camera preview
        //qrCodeReaderView.setFrontCamera();
        // Use this function to set back camera preview
        //qrCodeReaderView.setBackCamera();

    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        Log.d("Result", text);
        if (!text.equalsIgnoreCase("")){

            sp.edit().putString("qrCode", text).apply();
            sharedPreferences.edit().putString("var", "code").apply();
            sharedPreferences.edit().putBoolean("pause", false).apply();
            //this.onPause();
            this.finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPreferences.getBoolean("pause", false)) {
            Intent intent = new Intent(this, PinCode.class);
            sharedPreferences.edit().putString("var", "code").apply();
            startActivity(intent);
        }
        qrCodeReaderView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrCodeReaderView.stopCamera();
        sharedPreferences.edit().putBoolean("pause", true).apply();
        if (sharedPreferences.getString("var", "").equalsIgnoreCase("code")) {
            sharedPreferences.edit().putBoolean("pause", false).apply();
            sharedPreferences.edit().putString("var", "").apply();
        }
    }


    public void back(View view) {
        sharedPreferences.edit().putBoolean("pause", false).apply();
        sharedPreferences.edit().putString("var", "code").apply();
        this.finish();
    }

    public void changeCamera(View view) {
        if (cam == 0)
            cam = 1;
        else cam = 0;

        if (cam == 0) {
            qrCodeReaderView.setBackCamera();
            flashOn.setVisibility(View.VISIBLE);
        }
        else {
            qrCodeReaderView.setFrontCamera();
            if (flashOn.getVisibility() == View.VISIBLE)
                flashOn.setVisibility(View.INVISIBLE);
            else {
                flashOff.setVisibility(View.INVISIBLE);
                qrCodeReaderView.setTorchEnabled(false);
            }
        }

    }

    public void flashOn(View view) {
        qrCodeReaderView.setTorchEnabled(true);
        flashOff.setVisibility(View.VISIBLE);
        flashOn.setVisibility(View.INVISIBLE);
    }

    public void flashOff(View view) {
        qrCodeReaderView.setTorchEnabled(false);
        flashOff.setVisibility(View.INVISIBLE);
        flashOn.setVisibility(View.VISIBLE);
    }
}

