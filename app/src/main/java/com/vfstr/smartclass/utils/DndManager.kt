package com.vfstr.smartclass.utils

import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DndManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun enableDnd() {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            try {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun disableDnd() {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            try {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isPermissionGranted(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }
}
