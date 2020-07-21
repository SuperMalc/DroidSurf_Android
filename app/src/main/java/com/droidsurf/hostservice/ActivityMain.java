package com.droidsurf.hostservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import java.lang.reflect.Field;
import java.text.DecimalFormat;

import static com.droidsurf.hostservice.Connection.PORT;
import static com.droidsurf.hostservice.Connection.TEXT;
import static com.droidsurf.hostservice.Options.PIN;
import static com.droidsurf.hostservice.Permission.PERMS;

public class ActivityMain extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "Main_New_Activity";

    private CardView connection, permission, option, shell, info;
    private TextView tv1, tv2, tv3, tv4, tv5;
    private Switch mainSwitch;
    private ImageView imgView0;
    private String perms;

    // Shared prefs
    public static final String SHARED_PREFS = "sharedPrefs";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        // Shared prefs
        sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        // Layouts
        imgView0 = (ImageView) findViewById(R.id.imageViewAlert);
        mainSwitch = (Switch) findViewById(R.id.switchMain);

        tv1 = (TextView) findViewById(R.id.tvCollapse1);
        tv2 = (TextView) findViewById(R.id.tvCollapse2);
        tv3 = (TextView) findViewById(R.id.tvCollapse3);
        tv4 = (TextView) findViewById(R.id.tvCollapse4);
        tv5 = (TextView) findViewById(R.id.tvCollapseService1);

        String device0 = Build.MODEL;
        String device1 = Build.MANUFACTURER;
        String codename = androidCodeName();
        String memory = getMemorySizeHumanized();

        tv1.setText(device0);
        tv2.setText(device1);
        tv3.setText(codename);
        tv4.setText(memory);

        // Card id
        connection = (CardView) findViewById(R.id.card_connection);
        permission = (CardView) findViewById(R.id.card_permission);
        option = (CardView) findViewById(R.id.card_option);
        info = (CardView) findViewById(R.id.card_info);

        // !!! --
        shell = (CardView) findViewById(R.id.card_shell);
        shell.setVisibility(View.INVISIBLE);

        // Add click listener to cards
        connection.setOnClickListener(this);
        permission.setOnClickListener(this);
        option.setOnClickListener(this);
        mainSwitch.setOnClickListener(this);
        info.setOnClickListener(this);

        // Collapsing toolbar options
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/East_Lift.ttf");
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingtoolbar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbar.setCollapsedTitleTypeface(tf);
        collapsingToolbar.setExpandedTitleTypeface(tf);

        serviceRunningCheck();
        alertStatusCheck();
        lockScreen();
    }

    // OnClick methods
    @Override
    public void onClick(View view) {
        Intent i;
        switch (view.getId()) {
            case R.id.switchMain:
                if (mainSwitch.isChecked()) {
                    startInBackground(view);
                } else {
                    Log.d(TAG, "Switch checked OFF");
                    killService(view);
                }
                break;

            case R.id.card_connection:
                i = new Intent(this, Connection.class);
                startActivity(i);
                break;

            case R.id.card_permission:
                i = new Intent(this, Permission.class);
                startActivity(i);
                break;
            case R.id.card_option:
                i = new Intent(this, Options.class);
                startActivity(i);
                break;
            case R.id.card_info:
                i = new Intent(this, InfoActivity.class);
                startActivity(i);
                break;
            default:
                break;
        }
    }
    
    // Show / Hide alert widget above card menu
    public void alertStatusCheck() {
        perms = sharedPreferences.getString(PERMS, "");
        if (perms.equals("YES")) {
            Log.d(TAG, "alertStatusCheck: YES");
            imgView0.setVisibility(View.INVISIBLE);
        } else {
            Log.d(TAG, "alertStatusCheck: ??");
        }
    }

    // Check Android version code
    private String androidCodeName() {
        String codeName = "UNKNOWN";
        try {
            Field[] fields = Build.VERSION_CODES.class.getFields();
            for (Field field : fields) {
                try {
                    if (field.getInt(Build.VERSION_CODES.class) == Build.VERSION.SDK_INT) {
                        codeName = field.getName();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            codeName = ("Android " + codeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return codeName;
    }

    // Get total available ram
    private String getMemorySizeHumanized() {
        Context context = getApplicationContext();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        assert activityManager != null;
        activityManager.getMemoryInfo(memoryInfo);
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        String finalValue = "";
        long totalMemory = memoryInfo.totalMem;
        double kb = totalMemory / 1024.0;
        double mb = totalMemory / 1048576.0;
        double gb = totalMemory / 1073741824.0;
        double tb = totalMemory / 1099511627776.0;

        if (tb > 1) {
            finalValue = twoDecimalForm.format(tb).concat(" TB");
        } else if (gb > 1) {
            finalValue = twoDecimalForm.format(gb).concat(" GB");
        } else if (mb > 1) {
            finalValue = twoDecimalForm.format(mb).concat(" MB");
        }else if(kb > 1){
            finalValue = twoDecimalForm.format(mb).concat(" KB");
        } else {
            finalValue = twoDecimalForm.format(totalMemory).concat(" Bytes");
        }

        return ("RAM " + finalValue);
    }

    // ---------------------------------------------------------------------------------------------
    // - UI - UPDATE -
    // Determine if service is running - ONLY UPDATE UI -
    private boolean GUI_checkServiceRunning(Class<MainLoopIntentService> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "checkServiceRunning: is Running");

                return true;
            }
        }
        Log.d(TAG, "checkServiceRunning: not Running");

        return false;
    }

    private void serviceRunningCheck() {
        if (GUI_checkServiceRunning(MainLoopIntentService.class)) {
            mainSwitch.setChecked(true);
            tv5.setText(R.string.service_status_on);
        } else {
            mainSwitch.setChecked(false);
            tv5.setText(R.string.service_status_off);
        }
    }
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    // ----- Start MainLoopClientService -----------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // First step
    public void startInBackground(View view) {
        // Check if text view fields in Connection class are populated
        String host = sharedPreferences.getString(TEXT,"");
        String port = sharedPreferences.getString(PORT,"");

        if (host.equals("") || port.equals("")) {
            Toast.makeText(this, R.string.usr_has_saved, Toast.LENGTH_SHORT).show();
            mainSwitch.setChecked(false);
            Intent c = new Intent(this, Connection.class);
            startActivity(c);

        } else {
            // Check if service is already active
            isServiceRunning(MainLoopIntentService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Check if service guard sub-service is scheduled
                isGuardServiceScheduled();
            }
        }
    }

    // -----------------------------------------------------
    // Second step
    // Check if the service is running
    // -----------------------------------------------------

    private boolean isServiceRunning(Class<MainLoopIntentService> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "Service is already running");

                return true;
            }
        }
        Log.d(TAG, "Service not running, starting it now");

        // * STARTS A NORMAL FOREGROUND SERVICE *
        Intent serviceIntent = new Intent(this, MainLoopIntentService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        //textViewServiceStatus.setText(R.string.service_notice_1);   // notice the service has started
        tv5.setText("in esecuzione");
        return false;
    }

    // -----------------------------------------------------
    // Third step
    // Check if Service Guard sub-service is already scheduled (android > Oreo)
    // -----------------------------------------------------
    private boolean isGuardServiceScheduled() {
        JobScheduler jobScheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        boolean hasBeenScheduled = false;
        int JobId = 123;

        assert jobScheduler != null;
        for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JobId) {
                hasBeenScheduled = true;
                Log.d(TAG, "Job [Service Guard] already scheduled");
                break;
            }
        }

        if (!hasBeenScheduled) {
            Log.d(TAG, "Job [Service Guard] not scheduled, starting it now");
            startServiceGuard();
        }
        return hasBeenScheduled;
    }

    // -----------------------------------------------------
    // Fourth step
    // Start service guard sub-service if not scheduled
    // -----------------------------------------------------
    private void startServiceGuard() {
        // * STARTS SERVICE GUARD TO RELAUNCH SERVICE IN CASE IT IS CLOSED *
        // - Avvio del servizio di supporto ServiceGuard
        ComponentName componentName = new ComponentName(this,ServiceGuard.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiresCharging(false)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) this.getSystemService(JOB_SCHEDULER_SERVICE);
        assert scheduler != null;
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG,"ServiceGuard service scheduled");
        } else {
            Log.d(TAG,"ServiceGuard service scheduled failed");
        }
    }

    // -----------------------------------------------------
    // * KILLS ALL SERVICES *
    // -----------------------------------------------------
    public void killService(View view) {
        Intent serviceIntent = new Intent(this, MainLoopIntentService.class);
        stopService(serviceIntent);
        tv5.setText(R.string.service_notice_2);
        Log.d(TAG, "IntentService killed");
        tv5.setText("non in esecuzione");

        // * KILL SCHEDULED JOB *
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            JobScheduler scheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            assert scheduler != null;
            scheduler.cancelAll();
            Log.d(TAG, "All scheduled jobs stopped");
        }
    }

    // App lock screen
    public void lockScreen() {
        Intent connIntent = getIntent();
        String noLockFromConnection = connIntent.getStringExtra("PLEASE_NO_LOCK");
        Log.d(TAG, "connMsg: " + noLockFromConnection);

        String OptionsPIN = sharedPreferences.getString(PIN,"");

        if (OptionsPIN.isEmpty()) {
            Log.d(TAG, "lockScreen: pin empty / not active");
        } else {

            if (noLockFromConnection!=null) {
                if (noLockFromConnection.equals("NO_LOCK")) {
                    Log.d(TAG, "lockScreen: unlocked...");
                }

            } else {
                Log.d(TAG, "lockScreen: PIN: " + OptionsPIN);

                Intent intent = getIntent();
                String pinFromLockScreen = intent.getStringExtra(ActivityPin.EXTRA_MESSAGE);
                Log.d(TAG, "received pin:" + pinFromLockScreen);

                if (!OptionsPIN.equals(pinFromLockScreen)) {
                    Intent intentActivityPin = new Intent(this, ActivityPin.class);
                    startActivity(intentActivityPin);
                }
            }
        }
    }

    public void showCredits(View view) {
        CreditDialog creditDialog = new CreditDialog();
        creditDialog.show(getSupportFragmentManager(),"example");
    }

    @Override
    protected void onRestart() {
        serviceRunningCheck();
        alertStatusCheck();
        super.onRestart();
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }
}