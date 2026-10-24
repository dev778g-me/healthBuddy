package org.dev.healthbuddy

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.AndroidViewModel
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.data.Store
import org.dev.healthbuddy.ui.HealthBuddyApp

/**
 * Holds [AppState] for the lifetime of the activity's task, so a rotation or a
 * system config change (dark mode, font size, language) does not throw away the
 * current tab, the back stack, or an open dose sheet.
 */
class HealthBuddyViewModel(app: Application) : AndroidViewModel(app) {
    val state: AppState by lazy { AppState(Store(getApplication<Application>())) }
}

class MainActivity : ComponentActivity() {

    private val viewModel: HealthBuddyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HealthBuddyApp(viewModel.state)
        }
    }
}
