package com.droidsurf.hostservice;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class GlobalVariables extends Application {

    // ***** notification channel *****
    public static final String CHANNEL_ID = "serviceChannel1";
    public static final String CHANNEL_ID_2 = "serviceChannel2";

    // ***** shared prefs *****
    private String localizationData;
    private String filebrowserData;
    private String workingPath;


    // ***** notification channel *****
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    // ***** notification channel *****
    private void createNotificationChannel() {

        String ch1 = getResources().getString(R.string.Channel_1);
        String ch2 = getResources().getString(R.string.Channel_2);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel_1 = new NotificationChannel(
                    CHANNEL_ID,
                    ch1,
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel_1.setDescription("This is Channel 1");

            NotificationChannel serviceChannel_2 = new NotificationChannel(
                    CHANNEL_ID_2,
                    ch2,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel_2.setDescription("This is Channel 2");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel_1);
            manager.createNotificationChannel(serviceChannel_2);
        }
    }

    // ***** gps location global vars *****
    public String getLocalizationData() {
        return localizationData;
    }

    public void setLocalizationData(String localizationData) {
        this.localizationData = localizationData;
    }

    // ***** file browser global vars *****
    // ROOT PATH
    public String getFilebrowserData() {
        return filebrowserData;
    }

    public void setFilebrowserData(String filebrowserData) {
        this.filebrowserData = filebrowserData;
    }

    // WORKING PATH LEVEL
    public String getWorkingPath() {
        return workingPath;
    }

    public void setWorkingPath(String workingPath) {
        this.workingPath = workingPath;
    }
}