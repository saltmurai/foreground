package expo.modules.foreground

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.content.pm.ServiceInfo
import android.R
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ForegroundModule : Module() {
    // Each module class must implement the definition function. The definition consists of components
    // that describes the module's functionality and behavior.
    // See https://docs.expo.dev/modules/module-api for more details about available components.
    override fun definition() = ModuleDefinition {
        // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
        // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
        // The module will be accessible from `requireNativeModule('Foreground')` in JavaScript.
        Name("Foreground")


        Function("startForegroundService") { message: String ->
            val context = appContext.reactContext
            if (context != null) {
                Log.d("RideStatusForeground", "Starting foreground service with message: $message")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ForegroundService.startService(context, message)
                } else {
                    val serviceIntent = Intent(context, ForegroundService::class.java)
                    serviceIntent.putExtra("inputExtra", message)  // Pass the message via intent
                    context.startService(serviceIntent)
                }
            } else {
                Log.e("RideStatusForeground", "Context is null, cannot start service.")
            }
        }

        Function("stopForegroundService") {
            val context = appContext.reactContext
            if (context != null) {
                Log.d("RideStatusForeground", "Stopping foreground service...")
                ForegroundService.stopService(context)
            } else {
                Log.e("RideStatusForeground", "Context is null, cannot stop service.")
            }
        }
        // Sets constant properties on the module. Can take a dictionary or a closure that returns a dictionary.
    }
}

class ForegroundService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent?.getStringExtra("inputExtra")
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "ChannelId")
            .setContentTitle("Your ride is on its way!")
            .setContentText("Estimated time: 3 minutes")
            .setProgress(100, 50, false) // Progress bar (max: 100, current progress: 50)
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Use a standard Android drawable
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "ChannelId", "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        fun startService(context: Context, message: String) {
            val startIntent = Intent(context, ForegroundService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }
}
