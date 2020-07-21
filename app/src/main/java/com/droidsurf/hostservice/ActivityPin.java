package com.droidsurf.hostservice;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.BlockingDeque;

import static com.droidsurf.hostservice.ActivityMain.SHARED_PREFS;
import static com.droidsurf.hostservice.Options.PIN;

public class ActivityPin extends AppCompatActivity {

    private static final String TAG = "ActivityPin";
    public static final String EXTRA_MESSAGE = "com.droidsurf.hostservice.PIN_CHECK";

    // Shared prefs
    SharedPreferences sharedPreferences;

    Button button0, button1, button2, button3, button4, button5, button6, button7, button8, button9;
    TextView textView0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        // Shared prefs
        sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        button0 = (Button) findViewById(R.id.buttonF0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);
        button7 = (Button) findViewById(R.id.button7);
        button8 = (Button) findViewById(R.id.button8);
        button9 = (Button) findViewById(R.id.button9);

        textView0 = (TextView) findViewById(R.id.textViewPsw);
    }

    @Override
    protected void onStart() {
        super.onStart();
        textView0.setText("");
    }

    public void setButtonOnClick0 (View view) {
        textView0.append("0");
    }
    public void setButtonOnClick1 (View view) {
        textView0.append("1");
    }
    public void setButtonOnClick2 (View view) {
        textView0.append("2");
    }
    public void setButtonOnClick3 (View view) {
        textView0.append("3");
    }
    public void setButtonOnClick4 (View view) {
        textView0.append("4");
    }
    public void setButtonOnClick5 (View view) {
        textView0.append("5");
    }
    public void setButtonOnClick6 (View view) {
        textView0.append("6");
    }
    public void setButtonOnClick7 (View view) {
        textView0.append("7");
    }
    public void setButtonOnClick8 (View view) {
        textView0.append("8");
    }
    public void setButtonOnClick9 (View view) {
        textView0.append("9");
    }
    public void setButtonOnClick10 (View view) {
        textView0.setText("");
    }

    public void setButtonOnClick11 (View view) {
        Log.d(TAG, "check pin...");
        String savedPinCode = sharedPreferences.getString(PIN,"");
        Log.d(TAG, "saved pin: " + savedPinCode);
        String insertPinCode = textView0.getText().toString();
        Log.d(TAG, "insert pin: " + insertPinCode);

        if (savedPinCode.equals(insertPinCode)) {
            Log.d(TAG, "pin correct");
            Intent intent = new Intent(this, ActivityMain.class);
            intent.putExtra(EXTRA_MESSAGE, savedPinCode);
            startActivity(intent);

        } else {
            Log.d(TAG, "pin not correct");
            Toast.makeText(this, R.string.wrong_pin, Toast.LENGTH_SHORT).show();
            textView0.setText("");
        }
    }

    public void onBackPressed() {
        this.finishAffinity();
        System.exit(0);
    }
}