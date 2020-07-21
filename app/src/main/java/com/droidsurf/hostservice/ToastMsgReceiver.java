package com.droidsurf.hostservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.widget.Toast;

public class ToastMsgReceiver extends BroadcastReceiver {

    public static final String TAG = "ToastMsgReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String receivedText = intent.getStringExtra("com.example.com.droidsurf.hostservice.EXTRA_TOAST_MSG");
        Toast toast = Toast.makeText(context, receivedText, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }
}
