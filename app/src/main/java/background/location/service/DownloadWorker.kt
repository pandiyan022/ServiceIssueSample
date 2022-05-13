package background.location.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.*

import java.util.*
import kotlin.collections.HashMap


/**
 * @see https://developer.android.com/topic/libraries/architecture/workmanager/advanced/long-running
 * */
class DownloadWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override suspend fun doWork(): Result {
        val latitude = inputData.getString(MainActivity.LATITUDE)
            ?: return Result.failure()
        val longitude = inputData.getString(MainActivity.LONGITUDE)
            ?: return Result.failure()
        // Mark the Worker as important
        val progress = "Starting Download"
        setForeground(createForegroundInfo(progress))
        download(latitude, longitude)
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Location")
        var map = HashMap<String,String>()
        map.put(System.currentTimeMillis().toString(),latitude+","+longitude)
        /*myRef.setValue(map).addOnCompleteListener(OnCompleteListener { Unit->
            Log.d("keyss","Completed Update==>")
        }).addOnFailureListener(OnFailureListener { Unit ->
            Log.d("keyss","Failed update......")
        })*/
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val post = dataSnapshot.getValue()
                if(post==null){
                    myRef.setValue(map).addOnCompleteListener(OnCompleteListener { Unit ->
                        Log.d("keyss", "Completed Update==>")
                    }).addOnFailureListener(OnFailureListener { Unit ->
                        Log.d("keyss", "Failed update......")
                    })
                }else {
                    var parentMap = (post as HashMap<String, String>)
                    parentMap.putAll(map)
                    myRef.setValue(parentMap).addOnCompleteListener(OnCompleteListener { Unit ->
                        Log.d("keyss", "Completed Update==>")
                    }).addOnFailureListener(OnFailureListener { Unit ->
                        Log.d("keyss", "Failed update......")
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("keyss", "loadPost:onCancelled", databaseError.toException())
            }
        })

        return Result.success()
    }
    var cTime:Long=0

    private fun download(lat: String, lng: String) {
        // Downloads a file and updates bytes read
        // Calls setForegroundInfo() periodically when it needs to update
        // the ongoing Notification
        Log.d("keyss","Lat=>"+lat+" Lng=>"+lng)
        showNotification("Work Manager", "Lat=>"+lat+" Lng=>"+lng)
    }
    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val id = applicationContext.getString(R.string.notification_channel_id)
        val title = applicationContext.getString(R.string.notification_title)
        val cancel = applicationContext.getString(R.string.cancel_download)
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_work_notification)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(0,notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        // Create a Notification channel
    }

    companion object {
        const val KEY_INPUT_URL = "KEY_INPUT_URL"
        const val KEY_OUTPUT_FILE_NAME = "KEY_OUTPUT_FILE_NAME"
    }
    private fun showNotification(task: String, desc: String) {
        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "message_channel"
        val channelName = "message_name"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(task)
            .setContentText(desc)
            .setSmallIcon(R.mipmap.ic_launcher)
        manager.notify(1, builder.build())
    }
}