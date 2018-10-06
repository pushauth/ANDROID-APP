package com.vladyslav.pushauth.FirebaseSupport;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vladyslav.pushauth.NotificationActivity.CodeActivity;
import com.vladyslav.pushauth.NotificationActivity.QuestionActivity;
import com.vladyslav.pushauth.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private String reqHash;
    private String code;
    private String appName;

    SharedPreferences sharedPreferences;



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        try {

            if (remoteMessage.getNotification() != null) {
                if (remoteMessage.getData().get("mode").equalsIgnoreCase("auth")){
                    edit.putString("privateKey", remoteMessage.getData().get("private_key")).apply();
                Log.d("Notif private key", remoteMessage.getData().get("private_key"));
                }

                String mode = remoteMessage.getData().get("mode");
                //Log.d("Notif", user.getPrivateKey());
                //Log.d("HMAC", hash_hmac("TestHmac", privateKey));
                Log.d("Notif", "Notif != null");
                Log.d("Notification", remoteMessage.getNotification().toString());
                Log.d("Notification", remoteMessage.getNotification().getBody());
               // Log.d("Notification", remoteMessage.getNotification().getTitle());
                Log.d("Notification", remoteMessage.getData().toString());
                Log.d("Notification", mode);
                Log.d("Notification", sharedPreferences.getString("privateKey", ""));
                if (!mode.equalsIgnoreCase("auth")){
                    reqHash = remoteMessage.getData().get("req_hash");
                    code = remoteMessage.getData().get("code");
                    appName = remoteMessage.getData().get("app_name");
                    Log.d("Notif", appName);
                    sendNotification(remoteMessage.getNotification().getBody(), mode);
                }

            }
        }catch (Exception e){Log.d("Error", "Error in push");}
    }

    private void sendNotification(String messageBody,  String mode) {
        if (mode.equalsIgnoreCase("push")) {
            Intent intent = new Intent(this, QuestionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("appName", appName);
            intent.putExtra("hash", reqHash);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(this, CodeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("code", code);
            intent.putExtra("appName", appName);
            intent.putExtra("hash", reqHash);
            startActivity(intent);
        }
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo_new)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_logo_new))
                .setContentTitle("Push Auth")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

}
