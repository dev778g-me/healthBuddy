package org.dev.healthbuddy.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.dev.healthbuddy.data.Store

/** AlarmManager forgets everything across a reboot; put the dose alarms back. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val snapshot = Store(context).load()
        ReminderScheduler(context).sync(
            snapshot.medicines,
            enabled = snapshot.onboarded && snapshot.remindersEnabled
        )
    }
}
