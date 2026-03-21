package com.example.earbudpopup

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import androidx.core.app.NotificationCompat

class ConnectionService : Service() {

    companion object {
        const val ACTION_START = "com.example.earbudpopup.START"
        const val ACTION_STOP = "com.example.earbudpopup.STOP"
        const val ACTION_BLUETOOTH_EVENT = "com.example.earbudpopup.BLUETOOTH_EVENT"
        const val ACTION_TEST_POPUP = "com.example.earbudpopup.TEST_POPUP"
        const val EXTRA_DEVICE_NAME = "device_name"
        const val EXTRA_CONNECTED = "connected"

        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "earbud_service_channel"
    }

    private var overlayView: OverlayView? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())

        when (intent?.action) {
            ACTION_START -> {
                // Service is now running and listening via EarbudReceiver
            }
            ACTION_STOP -> {
                removeOverlay()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_BLUETOOTH_EVENT -> {
                val deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME) ?: "Unknown Device"
                val connected = intent.getBooleanExtra(EXTRA_CONNECTED, false)
                handleBluetoothEvent(deviceName, connected)
            }
            ACTION_TEST_POPUP -> {
                val deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME) ?: "Test Earbuds"
                val connected = intent.getBooleanExtra(EXTRA_CONNECTED, true)
                handleBluetoothEvent(deviceName, connected)
            }
        }

        return START_STICKY
    }

    private fun handleBluetoothEvent(deviceName: String, connected: Boolean) {
        if (!Settings.canDrawOverlays(this)) return

        handler.post {
            removeOverlay()
            overlayView = OverlayView(this, deviceName, connected)
            overlayView?.show()

            handler.postDelayed({
                removeOverlay()
            }, 3500L)
        }
    }

    private fun removeOverlay() {
        overlayView?.dismiss()
        overlayView = null
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Earbud Detector Active")
            .setContentText("Listening for Bluetooth connections...")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Earbud Popup Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when earbuds connect or disconnect"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }
}
