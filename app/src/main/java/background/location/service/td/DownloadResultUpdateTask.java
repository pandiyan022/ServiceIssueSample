package background.location.service.td;

import static background.location.service.MyWorker.getDeviceName;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;

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

public class DownloadResultUpdateTask implements Runnable{
    private Context context;
    private String mThreadID;
    private String mlatitude;
    private String mlongitude;

    public DownloadResultUpdateTask(Context context){
        this.context = context;
    }
    public void sendLocation(String threadID,String latitude,String longitude){
        mThreadID = threadID;
        mlatitude = latitude;
        mlongitude = longitude;
        MainThreadExecutor.ThreadID = mThreadID;
        MainThreadExecutor.threadComplete = false;
        FirebaseHelperUtilz.Companion.getInstance().updateLocation(latitude,longitude);
//        String latitude = getInputData().getString("LATITUDE");
//        String longitude = getInputData().getString("LONGITUDE");
        // Mark the Worker as important

    }
    @Override
    public void run() {
        Constraints powerConstraint = new Constraints.Builder()/*.setRequiresCharging(true)*/.build();
        /*Data taskData = new Data.Builder()
                .putString(MainActivity.Companion.getLATITUDE(), mlatitude)
                .putString(MainActivity.Companion.getLONGITUDE(), mlongitude)
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MyUpdateWorker.class)
            .setConstraints(powerConstraint).setInputData(taskData).build();
        WorkManager.getInstance(context).enqueue(request);*/
        Log.d("keysss","Updating=>>>>");
        MainThreadExecutor.ThreadID = mThreadID;
        MainThreadExecutor.threadComplete = true;
        /*WorkManager workManager = WorkManager.getInstance(context);
        OneTimeWorkRequest startServiceRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        workManager.enqueue(startServiceRequest);*/
    }
}