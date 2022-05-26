package background.location.service;

import static com.facebook.network.connectionclass.ConnectionQuality.EXCELLENT;
import static com.facebook.network.connectionclass.ConnectionQuality.MODERATE;
import static com.facebook.network.connectionclass.ConnectionQuality.POOR;
import static com.facebook.network.connectionclass.ConnectionQuality.UNKNOWN;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import background.location.service.jb.DownloadJobService;
import background.location.service.td.ConnectivityChecker;
import background.location.service.td.DownloadManager;
import background.location.service.td.DownloadResultUpdateTask;
import background.location.service.td.DownloadTask;
import background.location.service.td.MyBReceiver;

public class MyService extends Service {
    private String TAG = "MyService";
    public static boolean isServiceRunning = false;
    public static String CHANNEL_ID = "NOTIFICATION_CHANNEL";
    public static boolean isUpdateLocationRunning = false;

    /**********************/
    private WindowManager windowManager = null;
    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!isUpdateLocationRunning)
                startPeriodicLocationUpdate();
        }
    };
    public static String RESTART_LOCATIONUPDATE = "RESTART_LOCATIONUPDATE";
    /**********************/

    public MyService() {
        Log.d(TAG, "constructor called");
        isServiceRunning = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate called");
        createNotificationChannel();
        isServiceRunning = true;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, Build.VERSION.SDK_INT >= 30 ? PendingIntent.FLAG_MUTABLE : 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Service is Running")
                .setContentText("Listening for Screen Off/On events")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.black))
                .build();

        startForeground(1, notification);
        if (windowManager == null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifi.createWifiLock(TAG + ":Location Update");
        }
        CustomWindow window = new CustomWindow(this);
        window.open();
        startPeriodicLocationUpdate();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,new IntentFilter(RESTART_LOCATIONUPDATE));
        return START_STICKY;
    }

    ConnectionClassManager.ConnectionClassStateChangeListener connectManager = new ConnectionClassManager.ConnectionClassStateChangeListener() {
        @Override
        public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
            if (bandwidthState.equals(POOR)) {
                ConnectivityChecker.Companion.generateNotification(MyService.this);
            } else if (bandwidthState.equals(UNKNOWN)) {
                ConnectivityChecker.Companion.generateNotification(MyService.this);
            } else if (bandwidthState.equals(MODERATE)) {
                ConnectivityChecker.Companion.generateNotification(MyService.this);
            } else if (bandwidthState.equals(EXCELLENT)) {
                ConnectivityChecker.Companion.cancelNotification(MyService.this);
            }
        }
    };
    /**
     * Fused locaiton api to initialize the location services
     */
    FusedLocationProviderClient fusedLocationClient = null;
    LocationCallback mLocationCallBack = null;

    @SuppressLint("MissingPermission")
    private void startPeriodicLocationUpdate() {
        ConnectionClassManager.getInstance().register(connectManager);
        DeviceBandwidthSampler.getInstance().startSampling();
        LocationRequest locationRequest = LocationRequest.create();
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(10000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        if (mLocationCallBack == null) {
            mLocationCallBack = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Log.e("keysss", "Service On Location Received");
                    if (locationResult.getLastLocation() != null) {
                        send(locationResult.getLastLocation());
                    }
                }
            };
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    mLocationCallBack,
                    Looper.myLooper()
            );
            isUpdateLocationRunning=true;
        }
    }

    /**
     * https://localcoder.org/execute-task-every-second-using-work-manager-api
     */
    public void send(Location lastLocation) {
        /*Constraints powerConstraint = new Constraints.Builder()*//*.setRequiresCharging(true)*//*.build();
        Data taskData = new Data.Builder()
                .putString(MainActivity.Companion.getLATITUDE(), lastLocation.getLatitude() + "")
                .putString(MainActivity.Companion.getLONGITUDE(), lastLocation.getLongitude() + "")
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MyWorker.class)
                .setConstraints(powerConstraint).setInputData(taskData).build();
        WorkManager.getInstance(this).enqueue(request);*/
        ConnectivityChecker.Companion.checkInternet(this);
        Intent broadcastIntent = new Intent(this, MyBReceiver.class);
        sendBroadcast(broadcastIntent);
        /*sendLocationData(lastLocation.getLatitude() + "",
                lastLocation.getLongitude() + "",
                getApplicationContext());*/
        mScheduleJob(lastLocation.getLatitude() + "",
                lastLocation.getLongitude() + "",
                getApplicationContext());
        if(isPhoneLocked(getApplicationContext())){
            ConnectivityChecker.Companion.generateLockNotification(getApplicationContext());
        }else
            ConnectivityChecker.Companion.cancelLockNotification(getApplicationContext());
        ConnectivityChecker.Companion.executePinCommand(getApplicationContext());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String appName = getString(R.string.app_name);
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    appName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        isServiceRunning = false;
        stopForeground(true);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        if (fusedLocationClient != null && mLocationCallBack != null) {
            fusedLocationClient
                    .removeLocationUpdates(mLocationCallBack)
                    .addOnCompleteListener(task -> {
                        Log.d("keys", "stopLocationUpdates : ");
                        isUpdateLocationRunning=false;
                    });
            isUpdateLocationRunning=false;
        }
        // call MyReceiver which will restart this service via a worker
        Intent broadcastIntent = new Intent(this, MyReceiver.class);
        sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

    private void sendLocationData(String latitude, String longitude, Context context) {
        DownloadResultUpdateTask drUpdateTask = new DownloadResultUpdateTask(context);
        DownloadTask downloadTask = new DownloadTask(latitude, longitude, drUpdateTask);
        DownloadManager.getDownloadManager().runDownloadFile(downloadTask);
    }

    public static void mScheduleJob(String latitude, String longitude, Context context) {
        ComponentName componentName = new ComponentName(context, DownloadJobService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            PersistableBundle pb = new PersistableBundle();
            pb.putString(MainActivity.Companion.getLATITUDE(), latitude);
            pb.putString(MainActivity.Companion.getLONGITUDE(), longitude);
            if (MyApp.Companion.getJobScheduler() == null)
                MyApp.Companion.setJobScheduler((JobScheduler) MyApp.Companion.getMContext()
                        .getSystemService(JOB_SCHEDULER_SERVICE));
            int currentJobCount = MyApp.Companion.getJobCounter();
//            if(currentJobCount<2) {
            JobInfo jobInfo = new JobInfo.Builder(currentJobCount + 1, componentName)
                    .setRequiresCharging(false)
                    //                .setRequiresDeviceIdle(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//                    .setRequiresBatteryNotLow(false)
                    .setExtras(pb)
                    .setPeriodic(/*15*60**/1000)
                    .setPersisted(true)
                    .build();
            MyApp.Companion.setJobCounter(currentJobCount + 1);
            if (MyApp.Companion.getJobScheduler().getAllPendingJobs().size() >= 90) {
                MyApp.Companion.getJobScheduler().cancelAll();
            }
            try {
                MyApp.Companion.getJobScheduler().schedule(jobInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            }
        }
        DownloadResultUpdateTask drUpdateTask = new DownloadResultUpdateTask(context);
        DownloadTask downloadTask = new DownloadTask(latitude, longitude, drUpdateTask);
        DownloadManager.getDownloadManager().runDownloadFile(downloadTask);
    }

    /**
     * Keyguard manager returns true if Phone Locked
     */
    public boolean isPhoneLocked(Context context) {
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return (myKM.inKeyguardRestrictedInputMode());/* {
            //it is locked
        } else {
            //it is not locked
        }*/
    }

}