package background.location.service.td;

import static background.location.service.MyWorker.getDeviceName;

import android.util.Log;

import androidx.annotation.NonNull;

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

/**
 * https://www.zoftino.com/android-threadpoolexecutor-example
 * */
public class DownloadTask implements Runnable {
    String latitude;
    String longitude;
    DownloadResultUpdateTask resultUpdateTask;

    public DownloadTask(String lat, String lng,
                        DownloadResultUpdateTask drUpdateTask) {
        latitude = lat;
        longitude = lng;
        resultUpdateTask = drUpdateTask;
    }

    @Override
    public void run() {
        //update results download status on the main thread
        resultUpdateTask.sendLocation(Thread.currentThread().getId()+"",latitude,longitude);
        DownloadManager.getDownloadManager().getMainThreadExecutor()
                .execute(resultUpdateTask);
    }
}