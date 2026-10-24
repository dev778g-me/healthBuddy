package org.dev.healthbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.domain.Lang
import org.dev.healthbuddy.domain.ThemeMode
import org.dev.healthbuddy.ui.components.HbCard
import org.dev.healthbuddy.ui.i18n.LocalStrings

/** The pitch screen: the loop, the research question, the safety stance. */
@Composable
fun WhyMattersScreen() {
    val s = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {

        Spacer(Modifier.height(8.dp))
        Text(
            text = s.tagline,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(20.dp))

        LoopStep(number = "1", title = s.loopRemindTitle, body = s.loopRemindBody)
        LoopStep(number = "2", title = s.loopIdentifyTitle, body = s.loopIdentifyBody)
        LoopStep(number = "3", title = s.loopSupportTitle, body = s.loopSupportBody)
        LoopStep(number = "4", title = s.loopFollowUpTitle, body = s.loopFollowUpBody)

        Spacer(Modifier.height(24.dp))

        HbCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
            Text(
                text = s.researchTitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = s.researchBody,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(12.dp))

        HbCard(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Text(
                text = s.neverTitle,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = s.neverBody,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(28.dp))
        Text(
            text = s.disclaimer,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun LoopStep(number: String, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(state: AppState) {
    val s = LocalStrings.current
    var confirmReset by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(s.languageLabel, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ToggleButton(
                checked = state.lang == Lang.EN,
                modifier = Modifier.weight(1f),
                onCheckedChange = { state.changeLanguage(Lang.EN) }
            ) { Text("English") }

            ToggleButton(
                checked = state.lang == Lang.HI,
                modifier = Modifier.weight(1f),
                onCheckedChange = { state.changeLanguage(Lang.HI) }
            ) { Text("हिंदी") }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ToggleButton(
                checked = state.lang == Lang.MR,
                modifier = Modifier.weight(1f),
                onCheckedChange = { state.changeLanguage(Lang.MR) }
            ) { Text("मराठी") }

            ToggleButton(
                checked = state.lang == Lang.OR,
                modifier = Modifier.weight(1f),
                onCheckedChange = { state.changeLanguage(Lang.OR) }
            ) { Text("ଓଡ଼ିଆ") }
        }

        Spacer(Modifier.height(20.dp))
        Text(s.themeLabel, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ToggleButton(
                checked = state.themeMode == ThemeMode.SYSTEM,
                modifier = Modifier.weight(1f),
                onCheckedChange = { state.changeThemeMode(ThemeMode.SYSTEM) }
            ) { Text(s.themeSystem) }

            ToggleButton(
                checked = state.themeMode == ThemeMode.LIGHT,
                modifier = Modifier.weight(1f),
                onCheckedChange = { state.changeThemeMode(ThemeMode.LIGHT) }
            ) { Text(s.themeLight) }

            ToggleButton(
                checked = state.themeMode == ThemeMode.DARK,
                modifier = Modifier.weight(1f),
                onCheckedChange = { state.changeThemeMode(ThemeMode.DARK) }
            ) { Text(s.themeDark) }
        }

        Spacer(Modifier.height(22.dp))

        HbCard(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(s.privateMode, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = s.privateModeBody,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = state.privateMode,
                    onCheckedChange = { state.changePrivateMode(it) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        HbCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
            Text(
                text = s.demoScenario,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = s.demoScenarioBody,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { state.loadDemo() },
                modifier = Modifier.fillMaxWidth()
            ) { Text(s.demoScenario) }
        }

        Spacer(Modifier.height(12.dp))

        HbCard(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Text(
                text = s.aboutBody,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { confirmReset = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text(s.resetData) }

        Spacer(Modifier.height(32.dp))
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(s.resetData) },
            text = { Text(s.resetConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    state.resetAll()
                }) { Text(s.resetData) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text(s.cancel) }
            }
        )
    }
}
