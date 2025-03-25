package com.qy.cloud.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CommentUtil {
    fun timestampToHHmmLegacy(timestamp: Long, isMillis: Boolean = true): String {
        val calendar = Calendar.getInstance()
        if (isMillis) {
            calendar.timeInMillis = timestamp
        } else {
            calendar.timeInMillis = timestamp * 1000
        }
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(calendar.time)
    }
}