package com.vfstr.smartclass.utils

import android.util.Log
import com.vfstr.smartclass.data.remote.api.FrontendEventPayload
import com.vfstr.smartclass.data.remote.api.RetrofitApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Telemetry @Inject constructor(
    private val api: RetrofitApi
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val isDebug = true // Should be BuildConfig.DEBUG

    fun track(event: String, properties: Map<String, Any> = emptyMap(), userId: String? = null) {
        val payload = FrontendEventPayload(
            event = event,
            properties = properties,
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            userId = userId
        )

        if (isDebug) {
            Log.d("Telemetry", "Event: $event, Props: $properties")
        } else {
            scope.launch {
                try {
                    api.trackEvents(listOf(payload))
                } catch (e: Exception) {
                    // Fail silently in release
                }
            }
        }
    }

    companion object {
        fun fnv1aHash(value: String): String {
            var hash = 0x811c9dc5L
            for (char in value) {
                hash = hash xor char.code.toLong()
                hash = (hash * 0x01000193L) and 0xFFFFFFFFL
            }
            return hash.toString(16).padStart(8, '0')
        }

        fun toOpaqueId(value: String): String = "id_${fnv1aHash(value)}"
    }
}
