package background.location.service;

import static android.content.Context.POWER_SERVICE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;

public class MyWorker extends Worker {
    private final Context context;
    private String TAG = "MyWorker:Keyss";

    public MyWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;//@see https://stackoverflow.com/a/18841394/9191757
        PowerManager pm;
        PowerManager.WakeLock wakeLock/*, wakeLock_deamScreen*/;
        WifiManager.WifiLock lock;
        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
/*
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SleepMode: by pressing Power Button");
        wakeLock.acquire();
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire();
*/
        PowerManager.WakeLock screenLock = ((PowerManager)context.getSystemService(POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TA:G");
        screenLock.acquire();

        /*
        wakeLock_deamScreen = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK| PowerManager.ON_AFTER_RELEASE| PowerManager.ACQUIRE_CAUSES_WAKEUP,"Screen Deam: or screen stays on for a little longer");
        wakeLock_deamScreen.acquire();
        wakeLock_Full_wake_lock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,"");
        wakeLock_Full_wake_lock.acquire();
        */
        ComponentName receiver = new ComponentName(context, MainActivity.class);
        PackageManager packagemanager = context.getPackageManager();
        packagemanager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            lock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "WIFI_LOCK_TAG");
            lock.acquire();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("keyss","CAUGHT Exptn==>"+e.getMessage());
        }
    }

    @NonNull
    @Override
    public Result doWork() {
//        Log.d(TAG, "doWork called for: " + this.getId());
        Log.d(TAG, "Service Running: " + MyService.isServiceRunning);
        if (!MyService.isServiceRunning) {
            Log.d(TAG, "starting service from doWork");
            Intent intent = new Intent(this.context, MyService.class);
            ContextCompat.startForegroundService(context, intent);
        }
        return Result.success();
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    @Override
    public void onStopped() {
        Log.d(TAG, "onStopped called for: " + this.getId());
        super.onStopped();
    }


}