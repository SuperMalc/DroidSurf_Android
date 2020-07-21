package com.droidsurf.hostservice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static com.droidsurf.hostservice.CreditDialog.URL1;
import static com.droidsurf.hostservice.CreditDialog.URL2;

public class InfoActivity extends AppCompatActivity {

    TextView tv1, tv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Layout
        tv1 = (TextView) findViewById(R.id.textView_01);
        tv2 = (TextView) findViewById(R.id.textView_02);

        // Click Listeners
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL2));
                startActivity(browserIntent);
            }
        });

        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL1));
                startActivity(browserIntent);
            }
        });
    }
}