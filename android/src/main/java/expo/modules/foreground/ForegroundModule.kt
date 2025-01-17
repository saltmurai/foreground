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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

import java.net.HttpURLConnection
import java.net.URL

class ForegroundModule : Module() {
    // Each module class must implement the definition function. The definition consists of components
    // that describes the module's functionality and behavior.
    // See https://docs.expo.dev/modules/module-api for more details about available components.
    override fun definition() = ModuleDefinition {
        // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
        // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
        // The module will be accessible from `requireNativeModule('Foreground')` in JavaScript.
        Name("Foreground")


        Function("startForegroundService") { endpoint: String, title: String, subtext: String ->
            val context = appContext.reactContext
            if (context != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ForegroundService.startService(context, endpoint, title, subtext)
                } else {
                    val serviceIntent = Intent(context, ForegroundService::class.java)
                    serviceIntent.putExtra("endpoint", endpoint)  // Pass the message via intent
                    serviceIntent.putExtra("title", title)
                    serviceIntent.putExtra("subtext", subtext)
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

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var updateJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val endpoint = intent?.getStringExtra("endpoint")
        val title = intent?.getStringExtra("title")
        val subtext = intent?.getStringExtra("subtext")

        if (endpoint == null) {
            Log.e("ForegroundService", "No endpoint provided for updates.")
            stopSelf()
            return START_NOT_STICKY
        }
        if (title == null) {
            Log.e("ForegroundService", "No title provided for the notification.")
            stopSelf()
            return START_NOT_STICKY
        }
        if (subtext == null) {
            Log.e("ForegroundService", "No subtext provided for the notification.")
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "ChannelId")
            .setContentTitle("Your ride is on its way!")
            .setContentText("Initializing...")
            .setProgress(100, 0, true) // Indeterminate progress initially
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_app_icon)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        }

        // Start fetching updates from the endpoint
        startFetchingUpdates(endpoint, title, subtext)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel() // Stop the update job if the service is destroyed
    }

    private fun startFetchingUpdates(endpoint: String, title: String, subtext: String) {
        updateJob = coroutineScope.launch {
            while (true) {
                try {
                    val (progress, estimate) = fetchUpdateFromEndpoint(endpoint)

                    // Update the notification with new progress and estimate
                    val updatedNotification = NotificationCompat.Builder(this@ForegroundService, "ChannelId")
                        .setContentTitle(title)
                        .setContentText("$subtext: $estimate")
                        .setProgress(100, progress, false)
                        .setOngoing(true)
                        .setSilent(true)
                        .setSmallIcon(R.drawable.ic_app_icon)
                        .build()

                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    
                    if (progress == 100) {
                        Log.d("ForegroundService", "Progress reached 100, stopping foreground service.")
                        stopSelf() // Call the method defined in the module
                        notificationManager.cancel(1)
                        break
                    }
                    
                    notificationManager.notify(1, updatedNotification)
                } catch (e: Exception) {
                    Log.e("ForegroundService", "Error fetching updates: ${e.message}", e)
                }

                delay(5000) // Fetch updates every 5 seconds
            }
        }
    }

    private fun fetchUpdateFromEndpoint(endpoint: String): Pair<Int, Int> {
        val urlConnection = URL(endpoint).openConnection() as HttpURLConnection
        return try {
            urlConnection.requestMethod = "GET"

            if (urlConnection.responseCode == 200) {
                val response = urlConnection.inputStream.bufferedReader().use { it.readText() }

                // Assume the response is in JSON format: { "progress": 50, "estimate": 3 }
                val json = org.json.JSONObject(response)
                val progress = json.getInt("progress")
                val estimate = json.getInt("estimate")

                progress to estimate
            } else {
                Log.e("ForegroundService", "Failed to fetch updates: HTTP ${urlConnection.responseCode}")
                0 to 0 // Default values on failure
            }
        } finally {
            urlConnection.disconnect()
        }
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
        fun startService(context: Context, endpoint: String, title: String, subtext: String) {
            val startIntent = Intent(context, ForegroundService::class.java)
            startIntent.putExtra("endpoint", endpoint)
            startIntent.putExtra("title", title)
            startIntent.putExtra("subtext", subtext)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }
}
