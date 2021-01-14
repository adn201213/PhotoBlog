package com.adnan.photoblog;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseInstanceIDSer";
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.i(TAG, "newToken:dromservice"+token);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
  //      String body=remoteMessage.getNotification().getBody();
        Log.i(TAG, "onMessageReceived:remoteMessage.getData().size() "+remoteMessage.getData().size());
        if (remoteMessage.getData().size() > 0) {
            sendNotification(remoteMessage);
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Bodyfromservice: " + remoteMessage.getNotification().getBody());
        }
        Log.i(TAG, "onMessageReceived: "+"hello");
    }
    private void sendNotification(RemoteMessage remoteMessage) {
      //  Map<String, String> data = remoteMessage.getData();
        String token  = remoteMessage.getTo();
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        String image = remoteMessage.getData().get("image");
        Log.i(TAG, "sendNotificationfromservice: "+token+title+message);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("user_id", token);
        PendingIntent contentIntent = PendingIntent.getActivity
                (this, 1,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "AdnanId";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel
                    = new NotificationChannel
                    (NOTIFICATION_CHANNEL_ID, "Adnan Channel", NotificationManager.IMPORTANCE_MAX);

            notificationChannel.setDescription("Adnan channel for test FCM");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Glide.with(getBaseContext())
                .asBitmap()
                .load(image)
                .into(new CustomTarget<Bitmap>() {
                          @Override
                          public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                              notificationBuilder
                                      .setAutoCancel(true)
                                      .setDefaults(Notification.DEFAULT_ALL)
                                      .setWhen(System.currentTimeMillis())
                                      .setShowWhen(true)
                                      .setSmallIcon(android.R.drawable.ic_notification_overlay)
                                      .setContentTitle(title)
                                      .setContentIntent(contentIntent)
                                      .setContentText(message)
                              .setLargeIcon(resource);
//             //   .setContentInfo("info");
                              int notificationId= (int) System.currentTimeMillis();
                              notificationManager.notify(notificationId, notificationBuilder.build());
                          }

                          @Override
                          public void onLoadCleared(@Nullable Drawable placeholder) {

                          }
                      });
    }

}
