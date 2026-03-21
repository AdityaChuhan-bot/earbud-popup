package com.example.earbudpopup

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.earbudpopup.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val overlayPermissionRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkAndRequestPermissions()
    }

    private fun setupUI() {
        binding.btnStartService.setOnClickListener {
            if (canDrawOverlays()) {
                startConnectionService()
                Toast.makeText(this, "Earbud detection service started", Toast.LENGTH_SHORT).show()
            } else {
                requestOverlayPermission()
            }
        }

        binding.btnStopService.setOnClickListener {
            stopConnectionService()
            Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show()
        }

        binding.btnTestPopup.setOnClickListener {
            if (canDrawOverlays()) {
                showTestPopup()
            } else {
                requestOverlayPermission()
            }
        }

        updateStatus()
    }

    private fun checkAndRequestPermissions() {
        if (!canDrawOverlays()) {
            requestOverlayPermission()
        }
    }

    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, overlayPermissionRequestCode)
            Toast.makeText(this, "Please grant overlay permission", Toast.LENGTH_LONG).show()
        }
    }

    private fun startConnectionService() {
        val intent = Intent(this, ConnectionService::class.java).apply {
            action = ConnectionService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        updateStatus()
    }

    private fun stopConnectionService() {
        val intent = Intent(this, ConnectionService::class.java).apply {
            action = ConnectionService.ACTION_STOP
        }
        startService(intent)
        updateStatus()
    }

    private fun showTestPopup() {
        val intent = Intent(this, ConnectionService::class.java).apply {
            action = ConnectionService.ACTION_TEST_POPUP
            putExtra(ConnectionService.EXTRA_DEVICE_NAME, "Test Earbuds")
            putExtra(ConnectionService.EXTRA_CONNECTED, true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun updateStatus() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        val btStatus = if (adapter?.isEnabled == true) "Enabled ✓" else "Disabled ✗"
        val overlayStatus = if (canDrawOverlays()) "Granted ✓" else "Not Granted ✗"

        binding.tvBluetoothStatus.text = "Bluetooth: $btStatus"
        binding.tvOverlayStatus.text = "Overlay Permission: $overlayStatus"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == overlayPermissionRequestCode) {
            updateStatus()
            if (canDrawOverlays()) {
                Toast.makeText(this, "Overlay permission granted!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }
}
