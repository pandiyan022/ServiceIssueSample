package background.location.service

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.work.*
import androidx.work.multiprocess.RemoteListenableWorker.ARGUMENT_CLASS_NAME
import androidx.work.multiprocess.RemoteListenableWorker.ARGUMENT_PACKAGE_NAME
import androidx.work.multiprocess.RemoteWorkerService
import background.location.service.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import androidx.work.PeriodicWorkRequest
import java.util.concurrent.TimeUnit
import androidx.work.ExistingPeriodicWorkPolicy

import androidx.work.WorkManager

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View

import androidx.core.content.ContextCompat

/**
 * @see https://developer.android.com/topic/libraries/architecture/workmanager/advanced/long-running
 *
 *
 * working=>
 * https://gist.github.com/varunon9/f2beec0a743c96708eb0ef971a9ff9cd
 * */
public class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        requestLocation(this)
        if (!checkHasDrawOverlayPermissions()) {
            navigateDrawPermissionSetting()
        }
    }

    private fun navigateDrawPermissionSetting() {
        val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        startActivity(myIntent)
    }

    private fun checkHasDrawOverlayPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    val locationRequest = LocationRequest.create()?.apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Permissions required to access location.
     */
    val PERMISSIONS_LOCATION = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    /**
     * Id to identify a location permission request.
     */
    val REQUEST_LOCATION = 101

    /**
     * Check if locaiton permission is already available,
     * if not request location permission
     * */
    private fun requestLocation(activity: MainActivity) {
        if (!activity.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) ||
            !activity.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            requestPermissions(PERMISSIONS_LOCATION, REQUEST_LOCATION)
        } else {
            startPeriodicLocationUpdate()
            startServiceViaWorker()
        }
    }

    /**
     * Fused locaiton api to initialize the location services
     * */
    val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    var mLocationCallBack: LocationCallback? = null
    private fun startPeriodicLocationUpdate() {
        if (mLocationCallBack == null) {
            mLocationCallBack = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
//                    Log.d("keyss", "keeps onGettingLociont:" + p0.lastLocation)
                    if (p0.lastLocation != null) {
                        send(p0.lastLocation);
                    }
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocation(this)
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            mLocationCallBack!!,
            Looper.getMainLooper()
        )
        startService()
    }

    companion object {
        public val LATITUDE = "LATITUDE"
        public val LONGITUDE = "LONGITUDE"
        public val MESSAGE_STATUS = "MESSAGE_STATUS"
        public val WORK_RESULT = "WORK_RESULT"

        const val ACTION_STOP_FOREGROUND = "${BuildConfig.APPLICATION_ID}.stopfloating.service"
        const val REQUEST_CODE_DRAW_PREMISSION = 2
    }

    var wkmgr: WorkManager? = null;
    var cTime: Long = 0

    /**https://localcoder.org/execute-task-every-second-using-work-manager-api
     * */
    public fun send(lastLocation: Location) {
        startService()
        val powerConstraint = Constraints.Builder()/*.setRequiresCharging(true)*/.build()
        val taskData = Data.Builder()
            .putString(MainActivity.LATITUDE, lastLocation.latitude.toString())
            .putString(MainActivity.LONGITUDE, lastLocation.longitude.toString())
            .putString(MESSAGE_STATUS, "Notify Done.").build()
        val request = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .setConstraints(powerConstraint).setInputData(taskData).build()
        WorkManager.getInstance(this).enqueue(request)

    }

    var lastUpdate = System.currentTimeMillis()
    fun AppCompatActivity.isPermissionGranted(permission: String) =
        ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        startPeriodicLocationUpdate()
    }

    /*********************************************************/
    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        stopService()
        super.onDestroy()
    }

    fun startService() {
        Log.d(TAG, "startService called")
        if (!MyService.isServiceRunning) {
            val serviceIntent = Intent(this, MyService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }

    fun stopService() {
        Log.d(TAG, "stopService called"+"=== Service Running("+MyService.isServiceRunning+")")
        if (MyService.isServiceRunning) {
            val serviceIntent = Intent(this, MyService::class.java)
            stopService(serviceIntent)
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
                MyService.isServiceRunning=false;
                val broadcastIntent = Intent(this, MyReceiver::class.java)
                sendBroadcast(broadcastIntent)
            }
        }else {
            // call MyReceiver which will restart this service via a worker

            // call MyReceiver which will restart this service via a worker
            val broadcastIntent = Intent(this, MyReceiver::class.java)
            sendBroadcast(broadcastIntent)
        }
    }

    val TAG = "keyss"
    fun startServiceViaWorker() {
        Log.d(TAG, "startServiceViaWorker called")
        val UNIQUE_WORK_NAME = "StartMyServiceViaWorker"
        val workManager = WorkManager.getInstance(this)

        // As per Documentation: The minimum repeat interval that can be defined is 15 minutes
        // (same as the JobScheduler API), but in practice 15 doesn't work. Using 16 here
        val request = PeriodicWorkRequest.Builder(
            MyWorker::class.java,
            16,
            TimeUnit.MINUTES
        )
            .build()

        // to schedule a unique work, no matter how many times app is opened i.e. startServiceViaWorker gets called
        // do check for AutoStart permission
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /*********************************************************/


}