package org.dev.healthbuddy.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.domain.Lang
import org.dev.healthbuddy.notifications.ReminderPermissions
import org.dev.healthbuddy.ui.components.RemindersCard
import org.dev.healthbuddy.ui.i18n.LocalStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen(state: AppState) {
    val s = LocalStrings.current
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var remindersWanted by remember { mutableStateOf(true) }
    // Seeded from the real system state so a re-install on a phone that has
    // already granted both does not show a disabled Continue for a frame.
    var permissionsReady by remember {
        mutableStateOf(
            ReminderPermissions.notificationsEnabled(context) &&
                ReminderPermissions.exactAlarmsEnabled(context)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        fontWeight = FontWeight.ExtraBold,
                        text = s.appName,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .animateContentSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp,),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {


            Text(
                text = s.onboardTitle,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = s.onboardBody,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(s.yourName, style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                shape = MaterialTheme.shapes.medium,
                value = name,
                onValueChange = { name = it },
                label = { Text(s.nameHint) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(s.chooseLanguage, style = MaterialTheme.typography.titleSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ToggleButton(
                    checked = state.lang == Lang.EN,
                    modifier = Modifier.weight(1f),
                    onCheckedChange = { state.changeLanguage(Lang.EN) }
                ) {
                    Text("English")
                }

                ToggleButton(
                    checked = state.lang == Lang.HI,
                    modifier = Modifier.weight(1f),
                    onCheckedChange = { state.changeLanguage(Lang.HI) }
                ) {
                    Text("हिंदी")
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ToggleButton(
                    checked = state.lang == Lang.MR,
                    modifier = Modifier.weight(1f),
                    onCheckedChange = { state.changeLanguage(Lang.MR) }
                ) {
                    Text("मराठी")
                }

                ToggleButton(
                    checked = state.lang == Lang.OR,
                    modifier = Modifier.weight(1f),
                    onCheckedChange = { state.changeLanguage(Lang.OR) }
                ) {
                    Text("ଓଡ଼ିଆ")
                }
            }

            Spacer(Modifier.height(4.dp))
            RemindersCard(
                checked = remindersWanted,
                onCheckedChange = { remindersWanted = it },
                onPermissionsChanged = { permissionsReady = it }
            )

            Text(
                text = s.disclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { state.completeOnboarding(name, state.lang, remindersWanted) },
                enabled = !remindersWanted || permissionsReady,
                modifier = Modifier.fillMaxWidth(),
                shapes = ButtonDefaults.shapes()
            ) {
                Text(
                    s.getStarted,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }}
