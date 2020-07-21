package com.droidsurf.hostservice;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.Objects;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.droidsurf.hostservice.ActivityMain.SHARED_PREFS;

public class Permission extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, View.OnClickListener {

    public static final String TAG = "Permission";

    // Shared preferences
    public SharedPreferences sharedPreferences;
    public static final String PERMS = "perms";

    // Layouts
    Switch s0, s1, s2, s3, s4;
    TextView tvPermsSettings, tvPerms;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        // Shared preferences initialization
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        // Fonts
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/Zekton_rg.ttf");
        // Layouts
        tvPerms = (TextView) findViewById(R.id.textViewP1);
        tvPerms.setTypeface(tf);
        tvPermsSettings = (TextView) findViewById(R.id.textViewP5);

        s0 = (Switch) findViewById(R.id.switchP0); // Grant all perms
        s1 = (Switch) findViewById(R.id.switchP1); // Write external storage
        s2 = (Switch) findViewById(R.id.switchP2); // Access location
        s3 = (Switch) findViewById(R.id.switchP3); // Manage phone
        s4 = (Switch) findViewById(R.id.switchP4); // Read contacts

        s0.setOnClickListener(this);
        s1.setOnClickListener(this);
        s2.setOnClickListener(this);
        s3.setOnClickListener(this);
        s4.setOnClickListener(this);
        
        ViewSelectedPermissionsCheck();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switchP0:
                // Request all permissions
                checkPermissions();

            case R.id.switchP1:
                if (s1.isChecked()) {
                    Log.d(TAG, "onClick: S1 CHECKED ON");
                    ActivityCompat.requestPermissions(Permission.this, new String[] { WRITE_EXTERNAL_STORAGE }, 456);
                }
                break;

            case R.id.switchP2:
                if (s2.isChecked()) {
                    Log.d(TAG, "onClick: S2 CHECKED ON");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ActivityCompat.requestPermissions(Permission.this, new String[] { ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION }, 456);
                    } else {
                        ActivityCompat.requestPermissions(Permission.this, new String[] { ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION }, 456);
                    }

                }
                break;

            case R.id.switchP3:
                Log.d(TAG, "onClick: S3 CHECKED ON");
                if (s3.isChecked()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ActivityCompat.requestPermissions(Permission.this, new String[] { READ_PHONE_STATE, READ_PHONE_NUMBERS }, 456);
                    } else {
                        ActivityCompat.requestPermissions(Permission.this, new String[] { READ_PHONE_STATE }, 456);
                    }
                }
                break;

            case R.id.switchP4:
                Log.d(TAG, "onClick: S4");
                if (s4.isChecked()) {
                    Log.d(TAG, "onClick: S3 CHECKED ON");
                    ActivityCompat.requestPermissions(Permission.this, new String[] { READ_CONTACTS }, 456);
                }
                break;

            case R.id.textViewP5:
                Log.d(TAG, "onClick: Go To Settings");
                goToSettings();

            default:
                break;
        }
    }

    private void goToSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", Objects.requireNonNull(this).getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    // Check selected permission activity reload itself
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void ViewSelectedPermissionsCheck() {
        int count = 0;

        // Write external storage
        if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            s1.setChecked(false);
        } else {
            s1.setChecked(true);
            count++;
        }
        
        // Access location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {   // Background location permission for android Q
            if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                s2.setChecked(false);
            } else {
                s2.setChecked(true);
                count++;
            }
        } else {
            if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                s2.setChecked(false);
            } else {
                s2.setChecked(true);
                count++;
            }
        }
        
        // Manage phone calls
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            s3.setChecked(false);
        } else {
            s3.setChecked(true);
            count++;
        }
        
        // Read contacts
        // Manage phone calls
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            s4.setChecked(false);
        } else {
            s4.setChecked(true);
            count++;
        }

        // All permissions are granted
        if (count==4) {
            s0.setChecked(true);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PERMS, "YES");  // Save that all permissions are granted
            editor.apply();
            Log.d(TAG, "ViewSelectedPermissionsCheck: All permissions are granted (SAVED)");
        } else {
            s0.setChecked(false);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PERMS, "NO");  // Save that all permissions are granted
            editor.apply();
            Log.d(TAG, "ViewSelectedPermissionsCheck: Some permission is not granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @AfterPermissionGranted(123)
    public void checkPermissions() {
        String[] perms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            perms = new String[]{WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION};

        } else {
            perms = new String[]{WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};
        }

        if (EasyPermissions.hasPermissions(this, perms)) {
            Log.d(TAG,"Permissions are granted");
            s0.setChecked(true);

            ViewSelectedPermissionsCheck();

        } else {
            EasyPermissions.requestPermissions(this, "Permessi necessari per funzionare correttamente", 123, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG,"Permissions granted");
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG,"Permissions denied");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart: ");
        ViewSelectedPermissionsCheck();
        super.onRestart();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBackPressed() {
        ViewSelectedPermissionsCheck();
        super.onBackPressed();
    }
}