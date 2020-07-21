package com.droidsurf.hostservice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "LocationUpdatesBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String receivedText = intent.getStringExtra("com.example.com.droidsurf.hostservice.EXTRA_POSITION");
        Toast.makeText(context, receivedText, Toast.LENGTH_SHORT).show();
    }
}