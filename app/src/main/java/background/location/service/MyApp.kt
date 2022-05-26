package background.location.service

import android.app.Application
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

public class MyApp : Application(), DefaultLifecycleObserver {
    companion object {
        var mView: View? = null
        var mContext: Context? = null
        var jobScheduler: JobScheduler? = null
        var jobCounter:Int = 0
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mContext = base
    }

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    override fun onCreate(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onCreate(owner)
        Log.d("keyssssss","OnCreate")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d("keyssssss","On Destory")
        super.onDestroy(owner)
    }

    override fun onLowMemory() {
        Log.d("keyssssss","On Low Memory")
        super.onLowMemory()
        val broadcastIntent = Intent(this, MyReceiver::class.java)
        applicationContext.sendBroadcast(broadcastIntent)
    }
}