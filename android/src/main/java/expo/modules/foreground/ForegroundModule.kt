package expo.modules.foreground

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import android.util.TypedValue
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
import android.content.res.Resources
import kotlin.random.Random
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


        Function("startForegroundService") { endpoint: String, title: String, subtext: String, progress: Int ->
            val context = appContext.reactContext
            if (context != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ForegroundService.startService(context, endpoint, title, subtext, progress)
                } else {
                    val serviceIntent = Intent(context, ForegroundService::class.java)
                    serviceIntent.putExtra("endpoint", endpoint)  // Pass the message via intent
                    serviceIntent.putExtra("title", title)
                    serviceIntent.putExtra("subtext", subtext)
                    serviceIntent.putExtra("progress", progress)
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
        val progress = intent?.getIntExtra("progress", 0)

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
        if (progress == null) {
            Log.e("ForegroundService", "No progress provided for the notification.")
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "ChannelId")
            .setContentTitle("귀하의 차량이 곧 도착합니다!")
            .setContentText("초기화 중...")
            .setProgress(100, 0, true) // Indeterminate progress initially
            .setOngoing(true)
            .setSmallIcon(R.mipmap.noti_ic_launcher)
            .setContentIntent(createPendingIntent())
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startFetchingUpdates(endpoint, title, subtext, progress)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel() // Stop the update job if the service is destroyed
    }

    private fun startFetchingUpdates(endpoint: String, title: String, subtext: String, progress: Int) {
        updateJob = coroutineScope.launch {
            while(true) {
                try {
                    val updateData = fetchUpdateFromEndpoint(endpoint)

                    if (updateData.isEmpty()) {
                        Log.e("ForegroundService", "No update data received, stopping service.")
                        stopSelf()
                        break
                    }

                    val startMemo = updateData["startMemo"] as? String ?: "N/A"
                    val endMemo = updateData["endMemo"] as? String ?: "N/A"
                    val rideId = updateData["rideId"] as? Int ?: 0
                    val rideStatus = updateData["rideStatus"] as? String ?: "UNKNOWN"
                    val displayTime = updateData["displayTime"] as? String ?: "UNKNOWN"

                    Log.d("ForegroundService", "Start: $startMemo, End: $endMemo, Ride ID: $rideId, Status: $rideStatus, displayTime: $displayTime")

                    val textTitle = when (rideStatus) {
                        "RECEPTION" -> "$displayTime 까지 기사가 배차될 예정입니다."
                        "DISPATCH" -> "$displayTime 까지 기사가 도착할 예정입니다"
                        "ENROUTE" -> "안전하게 목적지까지 운행중입니다."
                        else -> "귀하의 차량이 곧 도착합니다!"
                    }

                    val subTextTitle = when (rideStatus) {
                       "RECEPTION", "DISPATCH", "ENROUTE" -> endMemo
                        else -> "초기화 중..."
                    }

                    val prog = when (rideStatus) {
                        "RECEPTION" -> 20
                        "DISPATCH" -> 50
                        "ENROUTE" -> 80
                        else -> 0
                    }

                    val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)
                    val notificationLayoutLarge = RemoteViews(packageName, R.layout.notification_layout_large)

                    notificationLayout.setTextViewText(R.id.notification_title, textTitle)
                    notificationLayoutLarge.setTextViewText(R.id.notification_title_large, textTitle)

                    notificationLayoutLarge.setTextViewText(R.id.notification_body_large, subTextTitle)

                    notificationLayout.setProgressBar(R.id.determinateBar, 100, prog, false)
                    notificationLayoutLarge.setProgressBar(R.id.determinateBarLarge, 100, prog, false)

                    val displayMetrics = Resources.getSystem().displayMetrics
                    val screenWidth = displayMetrics.widthPixels 
                    val screenWidthDp = screenWidth / displayMetrics.density
                    Log.d("ForegroundService", "screenWidth: $screenWidthDp")
                    val convertWidth = screenWidthDp - 104 - 16
                    val calculateMargin = (convertWidth * prog) / 100

                    notificationLayoutLarge.setViewLayoutMargin(R.id.imageView, 0x00000000, calculateMargin.toFloat(), TypedValue.COMPLEX_UNIT_DIP)

                    // Update the notification with new progress and estimate
                    val updatedNotification = NotificationCompat.Builder(this@ForegroundService, "ChannelId")
                        .setSmallIcon(R.mipmap.noti_ic_launcher)
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                        .setCustomContentView(notificationLayout)
                        .setCustomBigContentView(notificationLayoutLarge)
                        .setOnlyAlertOnce(true)
                        .setContentIntent(createPendingIntent())
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

                delay(5000)
            }
        }
    }

    private fun fetchUpdateFromEndpoint(endpoint: String): Map<String, Any> {
        val urlConnection = URL(endpoint).openConnection() as HttpURLConnection
        return try {
            urlConnection.requestMethod = "GET"
    
            if (urlConnection.responseCode == 200) {
                val response = urlConnection.inputStream.bufferedReader().use { it.readText() }
    
                // Parse JSON response
                val json = org.json.JSONObject(response)

                if (json.has("result")) {
                    val result = json.getJSONObject("result")

                    mapOf(
                        "startMemo" to result.getString("startMemo"),
                        "endMemo" to result.getString("endMemo"),
                        "rideId" to result.getInt("rideId"),
                        "rideStatus" to result.getString("rideStatus"),
                        "displayTime" to result.getString("displayTime")
                    )
                } else {
                    Log.e("ForegroundService", "API response is missing 'result' field")
                    return emptyMap()
                }
               
            } else {
                Log.e("ForegroundService", "Failed to fetch updates: HTTP ${urlConnection.responseCode}")
                emptyMap()
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

    private fun createPendingIntent(): PendingIntent {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        fun startService(context: Context, endpoint: String, title: String, subtext: String, progress: Int) {
            val startIntent = Intent(context, ForegroundService::class.java)
            startIntent.putExtra("endpoint", endpoint)
            startIntent.putExtra("title", title)
            startIntent.putExtra("subtext", subtext)
            startIntent.putExtra("progress", progress)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }
}