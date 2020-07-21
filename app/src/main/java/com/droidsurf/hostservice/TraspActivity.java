package com.droidsurf.hostservice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class TraspActivity extends AppCompatActivity {
    private static final String TAG = "TraspActivity";
    Button bt0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trasp);

        bt0 = (Button) findViewById(R.id.enable_click_bt);

        bt0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load_settings();
                Log.d(TAG, "onClick: bt clicked");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        load_settings();
    }

    private void load_settings() {
        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }
}