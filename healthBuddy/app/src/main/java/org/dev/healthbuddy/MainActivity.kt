package org.dev.healthbuddy

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.data.Store
import org.dev.healthbuddy.notifications.ReminderScheduler
import org.dev.healthbuddy.ui.HealthBuddyApp
import kotlin.getValue

/**
 * Holds [AppState] for the lifetime of the activity's task, so a rotation or a
 * system config change (dark mode, font size, language) does not throw away the
 * current tab, the back stack, or an open dose sheet.
 */
class HealthBuddyViewModel(app: Application) : AndroidViewModel(app) {
    val state: AppState by lazy {
        AppState(
            store = Store(getApplication<Application>()),
            reminders = ReminderScheduler(getApplication<Application>())
        )
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel: HealthBuddyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Tapping a dose notification always lands on Today.
        if (intent?.getBooleanExtra(ReminderScheduler.EXTRA_OPEN_TODAY, false) == true) {
            viewModel.state.openTodayFromNotification()
        }
        // Alarms do not survive a force-stop; a fresh launch re-arms them.
        viewModel.state.syncReminders()

        setContent {
            HealthBuddyApp(viewModel.state)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(ReminderScheduler.EXTRA_OPEN_TODAY, false)) {
            viewModel.state.openTodayFromNotification()
        }
    }
}
