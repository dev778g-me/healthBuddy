package org.dev.healthbuddy.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.dev.healthbuddy.data.Store
import java.time.LocalDate

/** Shows the dose notification, then re-arms the next occurrence. */
class DoseAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getStringExtra(ReminderScheduler.EXTRA_MEDICINE_ID)
        val baseTime = intent.getStringExtra(ReminderScheduler.EXTRA_BASE_TIME)
        Log.d("DoseAlarmReceiver", "Alarm received: med=$medicineId, time=$baseTime")

        if (medicineId == null || baseTime == null) return

        val snapshot = Store(context).load()
        if (!snapshot.onboarded || !snapshot.remindersEnabled) {
            Log.d("DoseAlarmReceiver", "Skipping: onboarded=${snapshot.onboarded}, enabled=${snapshot.remindersEnabled}")
            return
        }

        val medicine = snapshot.medicines.firstOrNull { it.id == medicineId && it.active }
        if (medicine == null) {
            Log.d("DoseAlarmReceiver", "Medicine not found or inactive: $medicineId")
            return
        }

        val isTest = intent.getBooleanExtra(ReminderScheduler.EXTRA_IS_TEST, false)

        // Only skip if the dose was already marked as TAKEN today.
        // Unsure or missed doses, or test reminders, should still notify.
        val today = LocalDate.now().toString()
        val alreadyTaken = snapshot.logs.any {
            it.medicineId == medicineId && it.date == today && it.time == baseTime && it.status == org.dev.healthbuddy.domain.DoseStatus.TAKEN
        }

        val scheduler = ReminderScheduler(context)
        if (isTest || !alreadyTaken) {
            Log.d("DoseAlarmReceiver", "Posting dose notification for $medicineId at $baseTime")
            scheduler.postDoseNotification(medicine, baseTime)
        } else {
            Log.d("DoseAlarmReceiver", "Already taken today, skipping notification for $medicineId at $baseTime")
        }
        // The fired alarm is spent; replace the whole set so tomorrow exists
        // even if the app is never opened again.
        scheduler.sync(snapshot.medicines, enabled = snapshot.onboarded && snapshot.remindersEnabled)
    }
}
