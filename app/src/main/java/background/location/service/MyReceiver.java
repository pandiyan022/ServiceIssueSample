package background.location.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MyReceiver extends BroadcastReceiver {
    private String TAG = "MyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");

        // We are starting MyService via a worker and not directly because since Android 7
        // (but officially since Lollipop!), any process called by a BroadcastReceiver
        // (only manifest-declared receiver) is run at low priority and hence eventually
        // killed by Android.
        WorkManager workManager = WorkManager.getInstance(context);
        OneTimeWorkRequest startServiceRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        workManager.enqueue(startServiceRequest);
//        if(!MyService.isServiceRunning) {
            Intent serviceIntent = new Intent(context, MyService.class);
            ContextCompat.startForegroundService(context, serviceIntent);
//        }
    }
    /**
     * https://www.zoftino.com/android-job-scheduler-example#:~:text=To%20schedule%20a%20job%2C%20first,getSystemService(JOB_SCHEDULER_SERVICE)%3B
     * */
}