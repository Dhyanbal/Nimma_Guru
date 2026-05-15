package com.example.nimmaguru.utils

import android.content.Context
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object ReminderManager {

    fun scheduleReminder(context: Context, bookingId: String, dateStr: String, timeStr: String, subject: String) {
        val dateTimeStr = "$dateStr $timeStr"
        
        // Comprehensive list of possible formats used in the app
        val formats = listOf(
            "dd MMM hh:mm a",
            "EEEE, dd MMM hh:mm a",
            "yyyy-MM-dd hh:mm a",
            "dd/MM/yyyy hh:mm a",
            "dd MMM HH:mm",
            "yyyy-MM-dd HH:mm"
        )
        
        var sessionDate: Date? = null
        val locale = Locale.getDefault()
        
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, locale)
                sessionDate = sdf.parse(dateTimeStr)
                if (sessionDate != null) {
                    val cal = Calendar.getInstance()
                    val sessionCal = Calendar.getInstance()
                    sessionCal.time = sessionDate
                    
                    // If year is missing or defaults to 1970, set it to current year
                    if (sessionCal.get(Calendar.YEAR) < 2000) {
                        sessionCal.set(Calendar.YEAR, cal.get(Calendar.YEAR))
                        // If the date has already passed this year, assume next year
                        if (sessionCal.before(cal)) {
                            sessionCal.add(Calendar.YEAR, 1)
                        }
                    }
                    sessionDate = sessionCal.time
                    break
                }
            } catch (e: Exception) { continue }
        }

        if (sessionDate == null) return

        // Calculate delay: 24 hours before the session
        val reminderTimeMillis = sessionDate.time - TimeUnit.DAYS.toMillis(1)
        val currentTimeMillis = System.currentTimeMillis()
        val delay = reminderTimeMillis - currentTimeMillis

        // If the session is less than 24 hours away, don't schedule or schedule for immediately?
        // Let's only schedule if we have at least some time.
        if (delay > 0) {
            val data = Data.Builder()
                .putString("title", "Session Reminder")
                .putString("message", "Reminder: Your session for $subject is tomorrow at $timeStr!")
                .build()

            val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("reminder_$bookingId")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "reminder_$bookingId",
                ExistingWorkPolicy.REPLACE,
                reminderRequest
            )
        }
    }
}
