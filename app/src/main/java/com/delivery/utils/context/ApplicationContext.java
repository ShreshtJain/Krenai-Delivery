package com.delivery.utils.context;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.google.firebase.FirebaseApp;

public class ApplicationContext extends Application {

    private static ApplicationContext sInstance;
    public static final String CHANNEL_ID = "location_chanel";

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        FirebaseApp.initializeApp(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    "Location_Cannel",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public static ApplicationContext getsInstance() {

        return sInstance;
    }

    public static Context getAppContext() {

        return sInstance.getApplicationContext();
    }
}