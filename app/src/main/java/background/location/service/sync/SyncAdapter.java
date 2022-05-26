package background.location.service.sync;

import android.accounts.Account;
import android.app.ActivityManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncResult;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import background.location.service.MyApp;
import background.location.service.MyReceiver;
import background.location.service.MyService;
import background.location.service.MyWorker;
import background.location.service.td.ConnectivityChecker;

/**
 * The real magic happens here.
 * This is adapter that will be called when system decide it is time to sync data.
 * Created by Kursulla on 07/09/15.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        System.out.println("******* onPerformSync *******");
//        System.out.println("*******" + syncResult.syncAlreadyInProgress+" *******");
        if(!isMyServiceRunning(MyService.class)){
            Intent broadcastIntent = new Intent(getContext(), MyReceiver.class);
            getContext().sendBroadcast(broadcastIntent);
        }else{
            Intent broadcastIntent = new Intent(MyService.RESTART_LOCATIONUPDATE);
            broadcastIntent.setAction(MyService.RESTART_LOCATIONUPDATE);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(broadcastIntent);
        }
        ConnectivityChecker.Companion.checkInternet(MyApp.Companion.getMContext());
        WorkManager workManager = WorkManager.getInstance(getContext());
        OneTimeWorkRequest startServiceRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        workManager.enqueue(startServiceRequest);
        System.out.println("*****************************");

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}