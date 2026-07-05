package com.vfstr.smartclass.data.remote.ble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vfstr.smartclass.utils.security.CryptoUtils
import java.util.UUID

class BleAdvertiserService : Service() {
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "ble_advertiser_channel"
        const val VFSTR_BLE_SERVICE_UUID = "0000FEAA-0000-1000-8000-00805F9B34FB" // Example UUID
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification("SmartClass Radar Active", "Broadcasting session beacon...")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothLeAdvertiser = manager.adapter.bluetoothLeAdvertiser

        val sessionId = intent?.getStringExtra("SESSION_ID")
        if (sessionId != null) {
            startAdvertisingSession(sessionId)
        }

        return START_STICKY
    }

    private fun startAdvertisingSession(sessionId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADVERTISE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.e("BLE", "Bluetooth Advertise permission not granted")
                stopSelf()
                return
            }
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
            .setConnectable(false)
            .build()

        val encryptedPayload = CryptoUtils.encryptSessionToken(sessionId)

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(UUID.fromString(VFSTR_BLE_SERVICE_UUID)))
            .addServiceData(ParcelUuid(UUID.fromString(VFSTR_BLE_SERVICE_UUID)), encryptedPayload)
            .setIncludeDeviceName(false)
            .build()

        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d("BLE", "Radar ping active for session")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("BLE", "Beacon initialization failed: $errorCode")
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "SmartClass BLE Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(title: String, content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth) // Use system icon for now
            .build()
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADVERTISE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
            }
        } else {
            bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
