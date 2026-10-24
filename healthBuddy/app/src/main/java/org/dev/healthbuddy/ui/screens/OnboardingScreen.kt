package org.dev.healthbuddy.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fitInside
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.dev.healthbuddy.R
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.domain.Lang
import org.dev.healthbuddy.ui.i18n.LocalStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen(state: AppState) {
    val s = LocalStrings.current
    var name by remember { mutableStateOf("") }

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

            Image(
                painter = painterResource(id = R.drawable.med),
                contentDescription = null,
                modifier = Modifier.size(200.dp).align(Alignment.End).weight(1f)
            )
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

                ToggleButton(
                    checked = state.lang == Lang.MR,
                    modifier = Modifier.weight(1f),
                    onCheckedChange = { state.changeLanguage(Lang.MR) }
                ) {
                    Text("मराठी")
                }
            }

            Text(
                text = s.disclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { state.completeOnboarding(name, state.lang) },
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
