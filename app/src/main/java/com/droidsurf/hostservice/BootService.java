package com.droidsurf.hostservice;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;



import androidx.core.content.ContextCompat;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.droidsurf.hostservice.ActivityMain.SHARED_PREFS;
import static com.droidsurf.hostservice.Options.SWITCH4;

public class BootService extends BroadcastReceiver {

    private static final String TAG = "BootService";

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        // - Recupera lo stato dello [switch 4] settato nella main activity -
        Boolean switchStatus = sharedPreferences.getBoolean(SWITCH4, false);

        // - Verifico se lo stato dello switch è impostato su attivo -
        if (switchStatus) {
            // - Avvio del servizio principale
            Intent serviceIntent = new Intent(context, com.droidsurf.hostservice.MainLoopIntentService.class);
            ContextCompat.startForegroundService(context, serviceIntent);
            Log.d(TAG, "servizio di base avviato");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // - Avvio del servizio di supporto ServiceGuard
                ComponentName componentName = new ComponentName(context, com.droidsurf.hostservice.ServiceGuard.class);
                JobInfo info = new JobInfo.Builder(123,componentName)
                        .setRequiresCharging(false)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setPersisted(true)
                        .setPeriodic(15 * 60 * 1000)
                        .build();

                JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
                assert scheduler != null;
                int resultCode = scheduler.schedule(info);
                if (resultCode == JobScheduler.RESULT_SUCCESS) {
                    Log.d(TAG,"Job schedulato");
                } else {
                    Log.d(TAG,"Schedulazione fallita");
                }
            }

        } else {
            Log.d(TAG, "SWITCH NON ATTIVO, non faccio nulla");
        }
    }
}