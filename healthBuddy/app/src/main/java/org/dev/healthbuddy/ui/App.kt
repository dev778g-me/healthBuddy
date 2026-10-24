package org.dev.healthbuddy.ui

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.domain.DoseSlot
import org.dev.healthbuddy.domain.Tab
import org.dev.healthbuddy.domain.ThemeMode
import org.dev.healthbuddy.ui.flow.DoseFlowSheet
import org.dev.healthbuddy.ui.i18n.LocalStrings
import org.dev.healthbuddy.ui.i18n.strings
import org.dev.healthbuddy.ui.nav.AddMedicineRoute
import org.dev.healthbuddy.ui.nav.LearnDetailRoute
import org.dev.healthbuddy.ui.nav.MainRoute
import org.dev.healthbuddy.ui.nav.SettingsRoute
import org.dev.healthbuddy.ui.nav.WhyMattersRoute
import org.dev.healthbuddy.ui.screens.AddMedicineScreen
import org.dev.healthbuddy.ui.screens.DashboardScreen
import org.dev.healthbuddy.ui.screens.HistoryScreen
import org.dev.healthbuddy.ui.screens.LearnDetailScreen
import org.dev.healthbuddy.ui.screens.LearnScreen
import org.dev.healthbuddy.ui.screens.MedicinesScreen
import org.dev.healthbuddy.ui.screens.OnboardingScreen
import org.dev.healthbuddy.ui.screens.SettingsScreen
import org.dev.healthbuddy.ui.screens.WhyMattersScreen

@Composable
fun HealthBuddyApp(state: AppState) {
    val darkTheme = when (state.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // Keep the status/navigation bar icons in step with the theme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalStrings provides state.lang.strings()) {
        AppTheme(darkTheme = darkTheme) {
            if (!state.onboarded) {
                OnboardingScreen(state)
            } else {
                MainShell(state)
            }
        }
    }
}

/**
 * One Scaffold owns the app chrome. The top bar title, back arrow and actions
 * are driven by the current navigation destination, so no screen builds its
 * own app bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(state: AppState) {
    val s = LocalStrings.current
    val motion = MaterialTheme.motionScheme
    val nav = rememberNavController()
    var flowSlot by remember { mutableStateOf<DoseSlot?>(null) }

    val backStackEntry by nav.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val isMain = destination?.hasRoute<MainRoute>() == true

    val title = when {
        destination?.hasRoute<SettingsRoute>() == true -> s.settingsTitle
        destination?.hasRoute<AddMedicineRoute>() == true -> s.addMedicine
        destination?.hasRoute<LearnDetailRoute>() == true -> s.learnTitle
        destination?.hasRoute<WhyMattersRoute>() == true -> s.whyMattersTitle
        isMain -> when (state.tab) {
            Tab.TODAY -> if (state.name.isBlank()) s.hello else "${s.hello}, ${state.name}"
            Tab.MEDICINES -> s.medicinesTitle
            Tab.HISTORY -> s.historyTitle
            Tab.LEARN -> s.learnTitle
        }
        else -> s.appName
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = title,
                        transitionSpec = {
                            (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 })
                                .togetherWith(
                                    fadeOut(tween(120)) +
                                        slideOutVertically(tween(120)) { -it / 3 }
                                )
                        },
                        label = "topBarTitle"
                    ) { text ->
                        Text(text = text)
                    }
                },
                navigationIcon = {
                    if (!isMain) {
                        IconButton(onClick = { nav.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    if (isMain) {
                        IconButton(onClick = { nav.navigate(SettingsRoute) }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = s.settingsTitle
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (isMain) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    NavigationBarItem(
                        selected = state.tab == Tab.TODAY,
                        onClick = { state.selectTab(Tab.TODAY) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text(s.tabToday) }
                    )
                    NavigationBarItem(
                        selected = state.tab == Tab.MEDICINES,
                        onClick = { state.selectTab(Tab.MEDICINES) },
                        icon = { Icon(Icons.Filled.List, contentDescription = null) },
                        label = { Text(s.tabMedicines) }
                    )
                    NavigationBarItem(
                        selected = state.tab == Tab.HISTORY,
                        onClick = { state.selectTab(Tab.HISTORY) },
                        icon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                        label = { Text(s.tabHistory) }
                    )
                    NavigationBarItem(
                        selected = state.tab == Tab.LEARN,
                        onClick = { state.selectTab(Tab.LEARN) },
                        icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                        label = { Text(s.tabLearn) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (isMain && state.tab == Tab.MEDICINES) {
                FloatingActionButton(
                    onClick = { nav.navigate(AddMedicineRoute) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Filled.Add, contentDescription = s.addMedicine)
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = MainRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = motion.defaultSpatialSpec()
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = motion.fastSpatialSpec()
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = motion.defaultSpatialSpec()
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = motion.fastSpatialSpec()
                )
            },
            predictivePopEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = motion.fastSpatialSpec()
                )
            },
            predictivePopExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = motion.fastSpatialSpec()
                )
            }
        ) {
            composable<MainRoute> {
                when (state.tab) {
                    Tab.TODAY -> DashboardScreen(state) { flowSlot = it }
                    Tab.MEDICINES -> MedicinesScreen(state)
                    Tab.HISTORY -> HistoryScreen(state) { flowSlot = it }
                    Tab.LEARN -> LearnScreen(
                        state = state,
                        onOpenDetail = { id ->
                            state.selectedEduId = id
                            nav.navigate(LearnDetailRoute)
                        },
                        onWhyMatters = { nav.navigate(WhyMattersRoute) }
                    )
                }
            }

            composable<SettingsRoute> {
                SettingsScreen(state)
            }

            composable<AddMedicineRoute> {
                AddMedicineScreen(state, onDone = { nav.navigateUp() })
            }

            composable<LearnDetailRoute> {
                LearnDetailScreen(state)
            }

            composable<WhyMattersRoute> {
                WhyMattersScreen()
            }
        }
    }

    flowSlot?.let { slot ->
        DoseFlowSheet(
            slot = slot,
            state = state,
            onDismiss = { flowSlot = null }
        )
    }
}
