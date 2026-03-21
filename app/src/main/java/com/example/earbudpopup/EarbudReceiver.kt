package com.example.earbudpopup

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class EarbudReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

        val deviceName = try {
            device?.name ?: "Unknown Device"
        } catch (e: SecurityException) {
            "Unknown Device"
        }

        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                triggerService(context, deviceName, connected = true)
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                triggerService(context, deviceName, connected = false)
            }
        }
    }

    private fun triggerService(context: Context, deviceName: String, connected: Boolean) {
        val serviceIntent = Intent(context, ConnectionService::class.java).apply {
            action = ConnectionService.ACTION_BLUETOOTH_EVENT
            putExtra(ConnectionService.EXTRA_DEVICE_NAME, deviceName)
            putExtra(ConnectionService.EXTRA_CONNECTED, connected)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
