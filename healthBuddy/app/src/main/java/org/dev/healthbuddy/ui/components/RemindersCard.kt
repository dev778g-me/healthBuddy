package org.dev.healthbuddy.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import org.dev.healthbuddy.notifications.ReminderPermissions
import org.dev.healthbuddy.ui.i18n.LocalStrings

/**
 * One "Reminders" list item, shared by onboarding and Settings, so the demo
 * can never end up with the two screens disagreeing about permissions.
 *
 * Onboarding uses it as a gate: the Continue button is only enabled once both
 * system switches are on. Settings uses it as a preference.
 */
@Composable
fun RemindersCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onPermissionsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val context = LocalContext.current

    var notifications by remember {
        mutableStateOf(ReminderPermissions.notificationsEnabled(context))
    }
    var alarms by remember {
        mutableStateOf(ReminderPermissions.exactAlarmsEnabled(context))
    }
    // True when this device simply has no exact-alarm screen. Reminders then
    // fall back to inexact alarms instead of locking the app.
    var fallback by remember { mutableStateOf(false) }
    var askedNotifications by remember { mutableStateOf(false) }

    val ready = notifications && (alarms || fallback)
    LaunchedEffect(ready) { onPermissionsChanged(ready) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        notifications = ReminderPermissions.notificationsEnabled(context)
    }
    val alarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        alarms = ReminderPermissions.exactAlarmsEnabled(context)
    }
    // The system notification screen is a separate activity; re-read both
    // switches when the user comes back, or Continue could stay disabled after
    // a successful grant.
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        notifications = ReminderPermissions.notificationsEnabled(context)
        alarms = ReminderPermissions.exactAlarmsEnabled(context)
    }

    val openSystemNotificationSettings: () -> Unit = {
        runCatching {
            settingsLauncher.launch(ReminderPermissions.notificationSettingsIntent(context))
        }
        Unit
    }

    val askNotifications: () -> Unit = {
        if (!notifications) {
            askedNotifications = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Android 12 has no runtime dialog; the only route is Settings.
                openSystemNotificationSettings()
            }
        }
    }

    val askAlarms: () -> Unit = {
        if (!alarms && !fallback) {
            val launched = runCatching {
                alarmLauncher.launch(ReminderPermissions.exactAlarmIntent(context))
            }.isSuccess
            if (!launched) fallback = true
        }
    }

    HbCard(modifier = modifier, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Lucide.Bell,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(s.remindersTitle, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(3.dp))
                Text(
                    text = s.remindersBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = { on ->
                    onCheckedChange(on)
                    if (on) askNotifications()
                }
            )
        }

        if (checked) {
            Spacer(Modifier.height(14.dp))
            PermissionRow(
                label = s.notificationsLabel,
                granted = notifications,
                onRequest = askNotifications
            )

            if (!notifications && (askedNotifications || Build.VERSION.SDK_INT < 33)) {
                TextButton(
                    onClick = openSystemNotificationSettings,
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) { Text(s.openSettings) }
            }

            Spacer(Modifier.height(2.dp))
            PermissionRow(
                label = s.alarmsLabel,
                granted = alarms || fallback,
                onRequest = askAlarms
            )
        }
    }
}

@Composable
private fun PermissionRow(
    label: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    val s = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (granted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Lucide.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = s.permissionGranted,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            TextButton(
                onClick = onRequest,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) { Text(s.permissionTurnOn) }
        }
    }
}
