package org.dev.healthbuddy.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.dev.healthbuddy.MainActivity
import org.dev.healthbuddy.R
import org.dev.healthbuddy.data.Store
import org.dev.healthbuddy.domain.Medicine
import org.dev.healthbuddy.ui.Fmt
import org.dev.healthbuddy.ui.i18n.strings
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Local, offline dose reminders.
 *
 * One one-shot exact alarm per active medicine × time, for the next occurrence
 * only. [DoseAlarmReceiver] shows the notification and asks this class to
 * replace the whole set, so tomorrow's alarm always exists without the app
 * running. Every persisted change calls [sync], which cancels the previous set
 * first — an edited or deleted time can never leave a stale alarm behind.
 *
 * This is a demo, so time zones are whatever the phone says and DST is handled
 * by `java.time` without a wake-up service.
 */
class ReminderScheduler(context: Context) {

    private val appContext = context.applicationContext
    private val alarms: AlarmManager = appContext.getSystemService(AlarmManager::class.java)
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun sync(medicines: List<Medicine>, enabled: Boolean) {
        cancelAll()

        if (!enabled || !ReminderPermissions.notificationsEnabled(appContext)) return

        // Created with the final onboarding language, only once reminders can
        // actually fire. A channel name is immutable after creation.
        ensureChannel()

        val now = LocalDateTime.now()
        val ids = mutableSetOf<String>()

        medicines.filter { it.active }.forEach { medicine ->
            medicine.times.forEach { base ->
                val time = runCatching { LocalTime.parse(effectiveTime(medicine, base)) }
                    .getOrNull() ?: return@forEach

                var next = now.toLocalDate().atTime(time)
                if (!next.isAfter(now)) next = next.plusDays(1)

                val id = requestCode(medicine.id, base)
                val pending = alarmPendingIntent(id, medicine.id, base)
                val triggerAt = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val scheduled = runCatching {
                    if (ReminderPermissions.exactAlarmsEnabled(appContext)) {
                        alarms.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, triggerAt, pending
                        )
                    } else {
                        // Never brick reminders on a phone that will not grant
                        // exact alarms: this still fires around the right time.
                        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
                    }
                }.isSuccess

                if (scheduled) ids += id.toString()
            }
        }

        prefs.edit().putStringSet(KEY_IDS, ids).apply()
    }

    fun cancelAll() {
        prefs.getStringSet(KEY_IDS, emptySet()).orEmpty().forEach { raw ->
            raw.toIntOrNull()?.let { alarms.cancel(alarmPendingIntent(it)) }
        }
        prefs.edit().remove(KEY_IDS).apply()
    }

    fun cancelNotification(medicineId: String, baseTime: String) {
        NotificationManagerCompat.from(appContext)
            .cancel(NOTIFICATION_TAG, requestCode(medicineId, baseTime))
    }

    fun cancelAllNotifications() {
        NotificationManagerCompat.from(appContext).cancelAll()
    }

    /** Called by [DoseAlarmReceiver] when a dose alarm fires. */
    fun postDoseNotification(medicine: Medicine, baseTime: String) {
        if (!ReminderPermissions.notificationsEnabled(appContext)) return
        ensureChannel()

        val snapshot = Store(appContext).load()
        val s = snapshot.lang.strings()
        val private = snapshot.privateMode

        val time = Fmt.time(effectiveTime(medicine, baseTime))
        val dose = medicine.dose.takeUnless { it.isBlank() || it == "—" }
        val condition = medicine.condition.takeUnless { it.isBlank() || it == "—" }

        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent())

        if (private) {
            // Generic everywhere, including the lock screen. That is the whole
            // point of private mode.
            builder
                .setContentTitle(s.reminderTitle)
                .setContentText(s.reminderBody)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPublicVersion(
                    NotificationCompat.Builder(appContext, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(s.reminderTitle)
                        .setContentText("")
                        .build()
                )
        } else {
            // One detail per line when the shade is expanded. Collapsed
            // notifications only get a single text line, so that line is the
            // time; the rest is in the big text.
            val details = buildList {
                add(time)
                dose?.let { add("${s.notifDose}: $it") }
                condition?.let { add("${s.notifFor}: $it") }
                add("")
                add(s.reminderBody)
            }.joinToString("\n")

            builder
                .setContentTitle(medicine.name)
                .setContentText(time)
                .setStyle(NotificationCompat.BigTextStyle().bigText(details))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        val notification = builder.build()

        runCatching {
            NotificationManagerCompat.from(appContext)
                .notify(NOTIFICATION_TAG, requestCode(medicine.id, baseTime), notification)
        }
    }

    fun ensureChannel() {
        val manager = appContext.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val s = Store(appContext).load().lang.strings()
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                s.remindersTitle,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = s.remindersBody }
        )
    }

    private fun openAppPendingIntent(): PendingIntent {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_OPEN_TODAY, true)
        }
        return PendingIntent.getActivity(
            appContext,
            OPEN_APP_REQUEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * The data URI + action are the identity of the alarm. Extras are only read
     * by the receiver, so cancelling does not need to know them.
     */
    private fun alarmPendingIntent(
        id: Int,
        medicineId: String? = null,
        baseTime: String? = null
    ): PendingIntent {
        val intent = Intent(appContext, DoseAlarmReceiver::class.java).apply {
            action = ACTION_DOSE
            data = Uri.parse("healthbuddy://dose/$id")
            if (medicineId != null && baseTime != null) {
                putExtra(EXTRA_MEDICINE_ID, medicineId)
                putExtra(EXTRA_BASE_TIME, baseTime)
            }
        }
        return PendingIntent.getBroadcast(
            appContext,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val EXTRA_OPEN_TODAY = "org.dev.healthbuddy.OPEN_TODAY"
        const val EXTRA_MEDICINE_ID = "org.dev.healthbuddy.MEDICINE_ID"
        const val EXTRA_BASE_TIME = "org.dev.healthbuddy.BASE_TIME"

        private const val CHANNEL_ID = "dose_reminders"
        private const val NOTIFICATION_TAG = "dose_reminder"
        private const val ACTION_DOSE = "org.dev.healthbuddy.action.DOSE_ALARM"
        private const val PREFS = "reminder_alarms"
        private const val KEY_IDS = "scheduled_ids"
        private const val OPEN_APP_REQUEST = 4242

        private val HH_MM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        /** Stable across restarts; used for both the alarm and the notification. */
        fun requestCode(medicineId: String, baseTime: String): Int =
            (medicineId + "|" + baseTime).hashCode() and 0x7fffffff

        fun effectiveTime(medicine: Medicine, base: String): String =
            runCatching {
                LocalTime.parse(base).plusMinutes(medicine.reminderOffsetMinutes.toLong())
                    .format(HH_MM)
            }.getOrDefault(base)
    }
}
