package com.droidsurf.hostservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;


public class GPSJobIntentService extends JobIntentService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    public static final String TAG = "GPSJobIntentService";
    public static String POSITION = "unknown";

    // GPS Service
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;



    /**
    // Message Handler for location service
    static final int MSG_REQUEST_LOCATION = 1;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    // - Incoming message Handler class -
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: ");
            switch (msg.what) {
                case MSG_REQUEST_LOCATION:
                    //Toast.makeText(getApplicationContext(), "ciao stronzo!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "handleMessage: Received msg!");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
     */

    // Callback for location results
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            onLocationChanged(locationResult.getLastLocation());
        }
    };

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 4000; /* 2 sec */

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, GPSJobIntentService.class, 123, work);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startLocationUpdates();
        Log.d(TAG, "onCreate: GPSJobIntentService");
    }

    private void startLocationUpdates() {
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

        Log.d(TAG, "startLocationUpdates: STARTED");
    }

    @Override
    protected void onHandleWork(Intent intent) {
        Log.d(TAG, "onHandleWork: service active for 1 min");
        for (int i = 0; i < 60; i++) {
            Log.d(TAG, "time "+i);
            SystemClock.sleep(1000);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
    }

    @Override
    public void onLocationChanged(Location location) {
        // New location has now been determined
        POSITION = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());

        Log.d(TAG, "onLocationChanged: " + POSITION);
        // SET GlobalVariable position
        ((GlobalVariables)this.getApplication()).setLocalizationData(POSITION);

        //sendGPSPosition(position);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        //mFusedLocationClient.removeLocationUpdates()
        Log.d(TAG, "onDestroy: LOCATION UPDATES REMOVED");
        //Log.d(TAG, "onDestroy: not removed");
    }
}