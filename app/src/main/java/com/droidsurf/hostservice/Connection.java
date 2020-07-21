package com.droidsurf.hostservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static com.droidsurf.hostservice.ActivityMain.SHARED_PREFS;
import static com.droidsurf.hostservice.Options.PIN;
import static com.droidsurf.hostservice.Permission.PERMS;

public class Connection extends AppCompatActivity {

    // Shared prefs
    public SharedPreferences sharedPreferences;

    public static final String TEXT = "text";
    public static final String PORT = "port";

    // - Initialization vars -
    private String text;
    private String port;
    private String perms;

    // Layout
    private CardView cVwAlert;
    private EditText editTextAddress;
    private EditText editTextPort;
    private TextView tvConn;
    private TextView tvPr;
    private Button btPerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        // Fonts
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/Zekton_rg.ttf");

        // Shared preferences initialization
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        // Layout
        cVwAlert = (CardView) findViewById(R.id.card_connectionAlert);

        btPerms = (Button) findViewById(R.id.buttonP1);
        btPerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                Intent i = new Intent(getApplicationContext(), Permission.class);
                startActivity(i);
            }
        });


        tvConn = (TextView) findViewById(R.id.textView0);
        tvConn.setTypeface(tf);

        tvPr = (TextView) findViewById(R.id.textViewPrev);
        tvPr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                Intent i = new Intent(getApplicationContext(), ActivityMain.class);
                i.putExtra("PLEASE_NO_LOCK", "NO_LOCK");
                startActivity(i);
            }
        });

        editTextAddress = (EditText) findViewById(R.id.editText0);
        editTextPort = (EditText) findViewById(R.id.editText1);

        loadData();
        updateViews();
    }

    // Load data from shared prefs
    private void loadData() {
        text = sharedPreferences.getString(TEXT, "");
        port = sharedPreferences.getString(PORT,"");
        perms = sharedPreferences.getString(PERMS,"");
        if (perms.equals("YES")) {
            cVwAlert.setVisibility(View.INVISIBLE);
            cVwAlert.setClickable(false);
        }
    }

    // Update UI saved shared prefs
    private void updateViews() {
        editTextAddress.setText(text);
        editTextPort.setText(port);
    }

    // Save current text strings in shared prefs
    private void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEXT, editTextAddress.getText().toString());   // Save host ip address
        editor.putString(PORT, editTextPort.getText().toString());  // Save host port
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    public void onBackPressed() {
        super.onBackPressed();
        saveData();
    }
}