package background.location.service.jb;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import background.location.service.MainActivity;
import background.location.service.MyApp;
import background.location.service.MyReceiver;
import background.location.service.MyService;
import background.location.service.MyWorker;
import background.location.service.td.DownloadManager;
import background.location.service.td.DownloadResultUpdateTask;
import background.location.service.td.DownloadTask;
import background.location.service.td.FirebaseHelperUtilz;

public class DownloadJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
/*        FirebaseHelperUtilz.Companion.getInstance().updateLocation(
                params.getExtras().getString(MainActivity.Companion.getLATITUDE()),
                params.getExtras().getString(MainActivity.Companion.getLONGITUDE()));*/
        /*String latitude=params.getExtras().getString(MainActivity.Companion.getLATITUDE());
        String longitude=params.getExtras().getString(MainActivity.Companion.getLONGITUDE());
        DownloadResultUpdateTask drUpdateTask = new DownloadResultUpdateTask(MyApp.Companion.getMContext());
        DownloadTask downloadTask = new DownloadTask(latitude, longitude, drUpdateTask);
        DownloadManager.getDownloadManager().runDownloadFile(downloadTask);*/
        if(!isMyServiceRunning(MyService.class)){
            Intent broadcastIntent = new Intent(this, MyReceiver.class);
            sendBroadcast(broadcastIntent);
        }
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        OneTimeWorkRequest startServiceRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        workManager.enqueue(startServiceRequest);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
