package com.vfstr.smartclass.utils.formatters

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

object Formatters {
    private val fullDateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    private val dateOnlyFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val timeOnlyFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    fun formatFullDateTime(dateTimeStr: String): String {
        return try {
            val dt = OffsetDateTime.parse(dateTimeStr).toLocalDateTime()
            dt.format(fullDateTimeFormatter)
        } catch (e: Exception) {
            dateTimeStr
        }
    }

    fun formatDateOnly(dateTimeStr: String): String {
        return try {
            val dt = OffsetDateTime.parse(dateTimeStr).toLocalDateTime()
            dt.format(dateOnlyFormatter)
        } catch (e: Exception) {
            dateTimeStr
        }
    }

    fun formatTimeOnly(dateTimeStr: String): String {
        return try {
            val dt = OffsetDateTime.parse(dateTimeStr).toLocalDateTime()
            dt.format(timeOnlyFormatter)
        } catch (e: Exception) {
            dateTimeStr
        }
    }

    fun formatRelativeTime(dateTimeStr: String): String {
        return try {
            val dt = OffsetDateTime.parse(dateTimeStr).toLocalDateTime()
            val now = LocalDateTime.now()
            val diffSeconds = ChronoUnit.SECONDS.between(dt, now)
            
            when {
                diffSeconds < 60 -> "just now"
                diffSeconds < 3600 -> "${diffSeconds / 60} min ago"
                diffSeconds < 86400 -> "${diffSeconds / 3600} hours ago"
                else -> dt.format(dateOnlyFormatter)
            }
        } catch (e: Exception) {
            dateTimeStr
        }
    }
}
