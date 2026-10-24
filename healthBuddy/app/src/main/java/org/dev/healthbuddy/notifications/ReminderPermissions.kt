package org.dev.healthbuddy.notifications

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/**
 * The two system switches a local reminder needs. Neither is a normal
 * permission dialog on every OS version, so the UI has to show the state and
 * link to the right system screen instead of assuming a grant.
 */
object ReminderPermissions {

    fun notificationsEnabled(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    /** "Alarms & reminders" special access — user-granted since Android 14. */
    fun exactAlarmsEnabled(context: Context): Boolean =
        context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    fun exactAlarmIntent(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            .setData(Uri.parse("package:${context.packageName}"))

    /** Escape hatch when the runtime prompt has already been dismissed twice. */
    fun notificationSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
}
