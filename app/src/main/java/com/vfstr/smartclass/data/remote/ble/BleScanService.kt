package com.vfstr.smartclass.data.remote.ble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vfstr.smartclass.data.repositories.AppRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class BleScanService : Service() {
    @Inject
    lateinit var repository: AppRepository

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val NOTIFICATION_ID = 1002
        const val CHANNEL_ID = "ble_scanner_channel"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // CRITICAL: Call startForeground IMMEDIATELY to satisfy the OS and prevent ForegroundServiceDidNotStartInTimeException
        createNotificationChannel()
        val initialNotification = createNotification("SmartClass Hub", "Initializing background sync...")
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(NOTIFICATION_ID, initialNotification)
            }
        } catch (e: Exception) {
            Log.e("BLE", "Failed to start foreground: ${e.message}")
            stopSelf()
            return START_NOT_STICKY
        }

        // All hardware and permission logic MUST happen after startForeground
        serviceScope.launch {
            try {
                if (!hasRequiredPermissions()) {
                    updateNotification("Sync Paused", "Grant permissions to enable classroom detection.")
                    Log.w("BLE", "Missing permissions, service idling...")
                    return@launch
                }

                val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
                val adapter = manager.adapter
                if (adapter == null || !adapter.isEnabled) {
                    updateNotification("Sync Disabled", "Bluetooth is turned off.")
                    Log.w("BLE", "Bluetooth disabled, service idling...")
                    return@launch
                }

                bluetoothLeScanner = adapter.bluetoothLeScanner
                if (bluetoothLeScanner == null) {
                    updateNotification("Sync Error", "Hardware scanner unavailable.")
                    stopSelf()
                    return@launch
                }

                updateNotification("Active Sync", "Scanning for classroom beacon...")
                startPassiveClassroomScan()
            } catch (e: Exception) {
                Log.e("BLE", "Delayed initialization failed: ${e.message}")
            }
        }

        return START_STICKY
    }

    private fun updateNotification(title: String, content: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(title, content))
    }

    private fun hasRequiredPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scan = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val connect = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val fineLocation = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            return scan && connect && fineLocation
        }
        return androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun startPassiveClassroomScan() {
        if (bluetoothLeScanner == null) {
            Log.e("BLE", "BluetoothLeScanner is null, cannot start scan")
            stopSelf()
            return
        }

        try {
            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID.fromString(BleAdvertiserService.VFSTR_BLE_SERVICE_UUID)))
                .build()

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()

            bluetoothLeScanner?.startScan(listOf(filter), settings, scanCallback)
        } catch (e: SecurityException) {
            Log.e("BLE", "SecurityException during startScan: ${e.message}")
            stopSelf()
        } catch (e: Exception) {
            Log.e("BLE", "Unexpected error during startScan: ${e.message}")
            stopSelf()
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val record = result.scanRecord ?: return
            val serviceData = record.getServiceData(ParcelUuid(UUID.fromString(BleAdvertiserService.VFSTR_BLE_SERVICE_UUID))) ?: return

            val encryptedToken = Base64.encodeToString(serviceData, Base64.NO_WRAP)
            Log.d("BLE", "Beacon found, submitting check-in")
            
            serviceScope.launch {
                repository.submitBleCheckIn(encryptedToken)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE", "Scan failed: $errorCode")
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "SmartClass BLE Scanner Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(title: String, content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                bluetoothLeScanner?.stopScan(scanCallback)
            }
        } else {
            bluetoothLeScanner?.stopScan(scanCallback)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
