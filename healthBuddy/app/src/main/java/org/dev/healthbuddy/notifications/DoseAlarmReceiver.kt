package org.dev.healthbuddy.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.dev.healthbuddy.data.Store
import java.time.LocalDate

/** Shows the dose notification, then re-arms the next occurrence. */
class DoseAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getStringExtra(ReminderScheduler.EXTRA_MEDICINE_ID) ?: return
        val baseTime = intent.getStringExtra(ReminderScheduler.EXTRA_BASE_TIME) ?: return

        val snapshot = Store(context).load()
        if (!snapshot.onboarded || !snapshot.remindersEnabled) return

        val medicine = snapshot.medicines.firstOrNull { it.id == medicineId && it.active }
            ?: return

        // Logging before the alarm goes off should not produce a "take it"
        // notification five minutes later.
        val today = LocalDate.now().toString()
        val alreadyLogged = snapshot.logs.any {
            it.medicineId == medicineId && it.date == today && it.time == baseTime
        }

        val scheduler = ReminderScheduler(context)
        if (!alreadyLogged) {
            scheduler.postDoseNotification(medicine, baseTime)
        }
        // The fired alarm is spent; replace the whole set so tomorrow exists
        // even if the app is never opened again.
        scheduler.sync(snapshot.medicines, enabled = true)
    }
}
