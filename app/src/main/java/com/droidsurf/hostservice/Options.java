package com.droidsurf.hostservice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static com.droidsurf.hostservice.Connection.PORT;
import static com.droidsurf.hostservice.Connection.TEXT;
import static com.droidsurf.hostservice.ActivityMain.SHARED_PREFS;

public class Options extends AppCompatActivity implements PinDialog.DialogListener{

    public static final String TAG = "Options";

    // Layout
    private Switch switch1;
    private Switch switch2;
    private Switch switch3;
    private Switch switch4;

    // Shared prefs
    SharedPreferences sharedPreferences;

    public static final String SWITCH1 = "switch1";
    public static final String SWITCH2 = "switch2";
    public static final String SWITCH3 = "switch3";
    public static final String SWITCH4 = "switch4";

    public static final String PIN = "pin";
    private String pin_code;

    private boolean switchOnOff1;
    private boolean switchOnOff2;
    private boolean switchOnOff3;
    private boolean switchOnOff4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        Log.d(TAG, "onCreate: Options");

        // Fonts
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/Zekton_rg.ttf");

        // Shared prefs
        sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        // TextView
        TextView tvOptions = (TextView) findViewById(R.id.textViewOptions);
        tvOptions.setTypeface(tf);

        // Switches
        switch1 = (Switch) findViewById(R.id.switch1);
        switch2 = (Switch) findViewById(R.id.switch2);
        switch3 = (Switch) findViewById(R.id.switch3);
        switch4 = (Switch) findViewById(R.id.switch4);

        switch4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                // Check if address and port are empty
                if (isChecked) {
                    String ConnectionText = sharedPreferences.getString(TEXT, "");
                    String ConnectionPort = sharedPreferences.getString(PORT,"");

                    if (ConnectionText.equals("") || ConnectionPort.equals("")) {
                        // Empty addresses - no save
                        switch4.setChecked(false);

                        Intent intent = new Intent(getApplicationContext(), Connection.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), R.string.usr_has_saved, Toast.LENGTH_SHORT).show();

                    } else {
                        // Save switch state
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(SWITCH4, switch4.isChecked());
                        editor.apply();
                    }

                } else {
                    // Save switch state
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(SWITCH4, switch4.isChecked());
                    editor.apply();
                }
            }
        });

        loadData();
        updateViews();
    }

    public void openStartOnBoot(View view) {
        if (switch4.isChecked()) {
            Log.d(TAG, "openStartOnBoot: checked");
            try {
                Intent intent = new Intent();
                String manufacturer = android.os.Build.MANUFACTURER;

                if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
                } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                } else if ("huawei".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                } else if ("asus".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(android.net.Uri.parse("mobilemanager://function/entry/AutoStart"));
                } else if ("iqoo".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"));
                } else if ("meizu".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.security.SHOW_APPSEC")).addCategory(Intent.CATEGORY_DEFAULT).putExtra("packageName", BuildConfig.APPLICATION_ID);
                }

                List<ResolveInfo> list = this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if  (list.size() > 0) {
                    this.startActivity(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeBatteryLimitations(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            String packageName = getPackageName();
            PowerManager PM = (PowerManager) getSystemService(POWER_SERVICE);

            if (switch3.isChecked()) {

                Log.d(TAG, "switch checked ON");

                assert PM != null;
                if (!PM.isIgnoringBatteryOptimizations(packageName)) {
                    Intent batteryIntent = new Intent();
                    batteryIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(batteryIntent);
                }
            } else {
                Log.d(TAG, "switch checked OFF");

                assert PM != null;
                if (PM.isIgnoringBatteryOptimizations(packageName)) {
                    Intent batteryIntent = new Intent();
                    batteryIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(batteryIntent);
                }
            }
        } else {
            Log.d(TAG, "removeBatteryLimitations: this API level don't manage battery optimization");
        }
    }

    public void batteryOptimizationCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager PM = (PowerManager) getSystemService(POWER_SERVICE);

            assert PM != null;
            if (!PM.isIgnoringBatteryOptimizations(packageName)) {
                switch3.setChecked(false);

            } else {
                switch3.setChecked(true);
            }

        } else {
            switch3.setEnabled(false);
        }
    }

    public void lockScreen(View view) {
        if (switch2.isChecked()) {
            Log.d(TAG, "lockScreen: checked");
            PinDialog pinDialog = new PinDialog();
            pinDialog.show(getSupportFragmentManager(),"PinDialog");
        } else {
            Log.d(TAG, "lockScreen: checked to off");
            // save switch2 status to off
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PIN, "");
            editor.putBoolean(SWITCH2, switch2.isChecked());
            editor.apply();
        }
    }

    @Override
    public void applyTexts(String pin) {
        pin_code = pin;

        if (pin.equals("")) {
            // save switch status in OFF mode
            switch2.setChecked(false);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(SWITCH2, switch2.isChecked());
            editor.apply();
            Log.d(TAG, "pin value is empty");

        } else {
            // save switch status in ON mode + save PIN code
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PIN, pin_code);                    // pin code save-
            editor.putBoolean(SWITCH2, switch2.isChecked());    // pin switch status save-
            editor.apply();
            Log.d(TAG, "pin saved");
        }
    }

    private void loadData() {
        switchOnOff1 = sharedPreferences.getBoolean(SWITCH1, false);
        switchOnOff2 = sharedPreferences.getBoolean(SWITCH2,false);
        switchOnOff3 = sharedPreferences.getBoolean(SWITCH3, false);
        switchOnOff4 = sharedPreferences.getBoolean(SWITCH4, false);

        pin_code = sharedPreferences.getString(PIN,"");
    }

    private void updateViews() {
        switch1.setChecked(switchOnOff1);
        switch2.setChecked(switchOnOff2);
        switch3.setChecked(switchOnOff3);
        switch4.setChecked(switchOnOff4);

        batteryOptimizationCheck();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        batteryOptimizationCheck();
    }
}