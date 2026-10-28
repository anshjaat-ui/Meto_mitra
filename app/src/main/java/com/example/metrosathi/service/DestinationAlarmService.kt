package com.example.metrosathi.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.metrosathi.domain.SearchEngine
import com.google.android.gms.location.*

class DestinationAlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "metrosathi_alarm_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START_ALARM"
        const val ACTION_STOP = "ACTION_STOP_ALARM"

        const val EXTRA_STATION_NAME = "EXTRA_STATION_NAME"
        const val EXTRA_STATION_LAT = "EXTRA_STATION_LAT"
        const val EXTRA_STATION_LNG = "EXTRA_STATION_LNG"
        const val EXTRA_AHEAD_COUNT = "EXTRA_AHEAD_COUNT"

        @Volatile
        var isRunning: Boolean = false
            private set
    }

    private var targetStationName: String = ""
    private var targetLat: Double = 0.0
    private var targetLng: Double = 0.0
    private var aheadCount: Int = 1

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlarm()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_START) {
            targetStationName = intent.getStringExtra(EXTRA_STATION_NAME) ?: "Destination"
            targetLat = intent.getDoubleExtra(EXTRA_STATION_LAT, 0.0)
            targetLng = intent.getDoubleExtra(EXTRA_STATION_LNG, 0.0)
            aheadCount = intent.getIntExtra(EXTRA_AHEAD_COUNT, 1)

            isRunning = true
            startForeground(NOTIFICATION_ID, buildForegroundNotification("Tracking journey towards $targetStationName…"))
            startLocationUpdates()
        }

        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                checkProximity(loc)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun checkProximity(currentLoc: Location) {
        val distMeters = SearchEngine.calculateDistanceMeters(
            currentLoc.latitude,
            currentLoc.longitude,
            targetLat,
            targetLng
        )

        // Threshold: 1 station ahead is approximately 1.5 km to 2.0 km
        val alertThresholdMeters = aheadCount * 1800

        if (distMeters <= alertThresholdMeters) {
            triggerArrivalAlarm(distMeters)
        } else {
            val kmText = String.format("%.1f km", distMeters / 1000.0)
            updateNotification("En route to $targetStationName • $kmText away")
        }
    }

    private fun triggerArrivalAlarm(distMeters: Int) {
        val kmText = if (distMeters > 500) "${distMeters / 1000.0} km away" else "$distMeters meters away"
        val alertTitle = "Wake up! Approaching $targetStationName"
        val alertContent = "Your destination is arriving soon ($kmText). Get ready to deboard!"

        // Vibrate
        val pattern = longArrayOf(0, 800, 400, 800, 400, 800)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }

        // Play Sound
        try {
            val alertTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, alertTone)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val notification = buildArrivalNotification(alertTitle, alertContent)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildForegroundNotification(status: String): Notification {
        val stopIntent = Intent(this, DestinationAlarmService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MetroSathi Travel Alarm")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Tracking", stopPending)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun buildArrivalNotification(title: String, content: String): Notification {
        val stopIntent = Intent(this, DestinationAlarmService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(android.R.drawable.ic_delete, "Dismiss Alarm", stopPending)
            .setAutoCancel(false)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildForegroundNotification(text))
    }

    private fun stopAlarm() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Metro Destination Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when approaching the destination metro station"
                enableVibration(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
