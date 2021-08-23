package com.delivery.utils.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.delivery.R;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = FirebaseMessagingService.class.getSimpleName();
    private NotificationUtil notificationUtil;
    @Override
    public void onNewToken(String s) {

        super.onNewToken(s);
        Log.i("NEW_TOKEN", s);
        //sendToken(s)
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage == null)
            return;
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String click_action = remoteMessage.getNotification().getClickAction();
            handleNotification(remoteMessage.getNotification().getBody(),title);
        }
        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            } catch (Exception e) {
            }
        }
    }

    private void handleNotification(String message,String title) {
        if (!NotificationUtil.isAppIsInBackground(getApplicationContext())) {
            String channelId = "Default";
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this, channelId)
                            .setContentTitle(title)
                            .setContentText(message).setAutoCancel(true)
                            .setAutoCancel(true);

            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Default channel",
                        NotificationManager.IMPORTANCE_HIGH
                );
                manager.createNotificationChannel(channel);
                builder.setColor(ContextCompat.getColor(this, R.color.colorAccent));
                builder.setSmallIcon(R.mipmap.ic_launcher);
            }

            manager.notify(0, builder.build());


            NotificationUtil notificationUtils = new NotificationUtil(getApplicationContext());
            notificationUtils.playNotificationSound();
        } else {
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            NotificationUtil notificationUtils = new NotificationUtil(getApplicationContext());
            notificationUtils.playNotificationSound();
        }
    }

    private void handleDataMessage(JSONObject data) {

        try {
            String title = data.getString("title");
            String message = data.getString("message");
            String timestamp = data.getString("timestamp");
            String imageUrl = null;

            if (!NotificationUtil.isAppIsInBackground(getApplicationContext())) {
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                Intent resultIntent = null;

                // check for image attachment

                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);


            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = null;


                // check for image attachment

                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);

            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtil = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtil.showNotificationMessage(title, message, timeStamp, intent);
    }

    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtil = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtil.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}

