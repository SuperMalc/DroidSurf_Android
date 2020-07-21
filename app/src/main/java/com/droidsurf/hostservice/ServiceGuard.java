package com.droidsurf.hostservice;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class ServiceGuard extends JobService {

    public static final String TAG = "ServiceGuard";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob: Check if service is running...");
        isMyServiceRunning(MainLoopIntentService.class, params);
        return true;
    }

    private boolean isMyServiceRunning(Class<MainLoopIntentService> serviceClass, JobParameters params) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "Il servizio è già in esecuzione");
                return true;
            }
        }
        Log.d(TAG, "Il servizio non è in esecuzione");
        restartMainLoopIntentService(params);
        return false;
    }

    private void restartMainLoopIntentService(JobParameters params) {
        Intent serviceIntent = new Intent(getApplicationContext(), MainLoopIntentService.class);
        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
        Log.d(TAG, "restartMainLoopIntentService: riuscito.");
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob: service cancelled");
        return false;
    }
}