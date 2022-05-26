package background.location.service

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.PowerManager
import androidx.work.WorkerParameters

/**
 * @see https://stackoverflow.com/a/18841394/9191757
 * */
class MyPeriodicWorker : MyWorker {
    companion object {
        var mContext: Context? = null;
    }

    constructor(context: Context, params: WorkerParameters) : super(context, params) {
        mContext = context
    }

    override fun doWork(): Result {
        wakeMeUp(if (mContext != null) mContext!! else MyApp.mContext!!)
        return super.doWork()
    }

    public fun wakeMeUp(context: Context) {
        val lock: WifiLock
        val screenLock =
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "TA:G"
            )
        screenLock.acquire()

        val receiver = ComponentName(context, MainActivity::class.java)
        val packagemanager = context.packageManager
        packagemanager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        lock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "WIFI_LOCK_TAG")
        lock.acquire()
    }
}