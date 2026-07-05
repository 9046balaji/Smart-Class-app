package com.vfstr.smartclass.utils.geofence

import android.content.Context
import android.net.wifi.WifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun getCurrentBssid(): String? {
        return try {
            val info = wifiManager.connectionInfo
            if (info != null && info.bssid != null && info.bssid != "00:00:00:00:00:00") {
                info.bssid.uppercase()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun isOnCampusWifi(): Boolean {
        return try {
            val info = wifiManager.connectionInfo
            val ssid = info?.ssid?.replace("\"", "")
            ssid == "Vignan_WiFi" || ssid == "VFSTR_CAMPUS"
        } catch (e: Exception) {
            false
        }
    }
}
