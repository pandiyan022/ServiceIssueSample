package background.location.service.td

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import background.location.service.MyReceiver
import background.location.service.MyService.CHANNEL_ID
import background.location.service.R
import android.os.PowerManager
import java.io.IOException


/**
 * https://stackoverflow.com/questions/54124631/how-to-keep-my-android-app-alive-in-background-for-24-7-continuous-sensor-data-c
 * send notification when internet disconnected to reconnect
 * https://stackoverflow.com/a/57237708/9191757
 *
 *
 * https://developer.android.com/topic/performance/vitals/bg-network-usage
 *
 * */
class ConnectivityChecker {
    companion object {
        var mContext: Context? = null
        var notificationId = 1015
        var notificationLockId = 1016

        fun generateNotification(ctx: Context) {
            // Create an explicit intent for an Activity in your app
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                ctx,
                notificationId,
                Intent(ctx, MyReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            var builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_work_notification)
                .setContentTitle("Network")
                .setContentText("Trying to reach internet..Unavailable!! ")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            with(NotificationManagerCompat.from(ctx)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
                wakeScreen(ctx)
            }
        }

        fun cancelNotification(ctx: Context) {
            with(NotificationManagerCompat.from(ctx)) {
                // notificationId is a unique int for each notification that you must define
                cancel(notificationId)
            }
        }

        fun internet(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
            return false
        }

        fun checkInternet(ctx: Context) {
            if (!internet(ctx)) {
                generateNotification(ctx)
            } else cancelNotification(ctx)
        }

        fun wakeScreen(ctx: Context) {
            val pm:PowerManager = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isScreenOn =
                if (Build.VERSION.SDK_INT >= 20) pm.isInteractive else pm.isScreenOn // check if screen is on

            if (!isScreenOn) {
                val wl = pm.newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "myApp:notificationLock"
                )
                wl.acquire(3000) //set your time in milliseconds
            }
        }
        fun generateLockNotification(ctx: Context) {
            // Create an explicit intent for an Activity in your app
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                ctx,
                notificationId,
                Intent(ctx, MyReceiver::class.java),
                PendingIntent.FLAG_MUTABLE
            )
            var builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_work_notification)
                .setContentTitle("Phone Locked")
                .setContentText("Unlock your phone to function properly!!")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            with(NotificationManagerCompat.from(ctx)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationLockId, builder.build())
                wakeScreen(ctx)
            }
        }
        fun cancelLockNotification(ctx: Context) {
            with(NotificationManagerCompat.from(ctx)) {
                // notificationId is a unique int for each notification that you must define
                cancel(notificationLockId)
            }
        }
        fun executePinCommand(ctx:Context){
            val runtime = Runtime.getRuntime()
            try {
                val mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
//                val mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 192.168.3.42")
                val mExitValue = mIpAddrProcess.waitFor()
                println(" mExitValue $mExitValue")
                if(mExitValue!=0){
                    generateNotification(ctx)
                }else cancelNotification(ctx)
//                return if (mExitValue == 0) {
//                    true
//                } else {
//                    false
//                }
            } catch (ignore: InterruptedException) {
                ignore.printStackTrace()
                generateNotification(ctx)
                println(" Exception:$ignore")
            } catch (e: IOException) {
                e.printStackTrace()
                generateNotification(ctx)
                println(" Exception:$e")
            }
        }
    }

}