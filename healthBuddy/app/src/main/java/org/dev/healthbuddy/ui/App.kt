package org.dev.healthbuddy.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.History
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pill
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.ScanLine
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.X
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.domain.DoseSlot
import org.dev.healthbuddy.domain.Tab
import org.dev.healthbuddy.domain.ThemeMode
import org.dev.healthbuddy.ui.flow.DoseFlowSheet
import org.dev.healthbuddy.ui.i18n.LocalStrings
import org.dev.healthbuddy.ui.i18n.scanStrings
import org.dev.healthbuddy.ui.i18n.strings
import org.dev.healthbuddy.ui.nav.HistoryRoute
import org.dev.healthbuddy.ui.nav.LearnDetailRoute
import org.dev.healthbuddy.ui.nav.LearnRoute
import org.dev.healthbuddy.ui.nav.MedicinesRoute
import org.dev.healthbuddy.ui.nav.SettingsRoute
import org.dev.healthbuddy.ui.nav.TodayRoute
import org.dev.healthbuddy.ui.nav.WhyMattersRoute
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
    val snackbarHostState = remember { SnackbarHostState() }
    var flowSlot by remember { mutableStateOf<DoseSlot?>(null) }
    var pendingLogMessage by remember { mutableStateOf<String?>(null) }
    var fabMenuExpanded by remember { mutableStateOf(false) }

    // "Replay demo scenario" lands on the missed dose, ready for the barrier question.
    LaunchedEffect(state.pendingDemoFlow) {
        if (state.pendingDemoFlow) {
            state.consumeDemoFlow()
            nav.navigateToTab(Tab.TODAY)
            flowSlot = state.attentionSlots().firstOrNull()
        }
    }

    // A tapped dose notification always surfaces Today, whatever was open.
    LaunchedEffect(state.pendingOpenToday) {
        if (state.pendingOpenToday) {
            state.consumeOpenToday()
            nav.navigateToTab(Tab.TODAY)
        }
    }

    // The sheet is its own window, so the confirmation is shown once it closes.
    LaunchedEffect(flowSlot) {
        if (flowSlot == null) {
            val message = pendingLogMessage ?: return@LaunchedEffect
            pendingLogMessage = null
            snackbarHostState.showSnackbar(message)
        }
    }

    val backStackEntry by nav.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    // Each tab is its own destination, so the system back stack mirrors the tabs.
    val currentTab: Tab? = when {
        destination?.hasRoute<TodayRoute>() == true -> Tab.TODAY
        destination?.hasRoute<MedicinesRoute>() == true -> Tab.MEDICINES
        destination?.hasRoute<HistoryRoute>() == true -> Tab.HISTORY
        destination?.hasRoute<LearnRoute>() == true -> Tab.LEARN
        else -> null
    }
    val isMain = currentTab != null

    // Keep AppState.tab in step with the backstack (back pops between tabs too).
    LaunchedEffect(currentTab) {
        if (currentTab != null) state.selectTab(currentTab)
    }

    val title = when {
        destination?.hasRoute<SettingsRoute>() == true -> s.settingsTitle
        destination?.hasRoute<LearnDetailRoute>() == true -> s.learnTitle
        destination?.hasRoute<WhyMattersRoute>() == true -> s.whyMattersTitle
        currentTab != null -> when (currentTab) {
            Tab.TODAY -> if (state.name.isBlank()) s.hello else "${s.hello}, ${state.name}"
            Tab.MEDICINES -> s.medicinesTitle
            Tab.HISTORY -> s.historyTitle
            Tab.LEARN -> s.learnTitle
        }
        else -> s.appName
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                imageVector = Lucide.ArrowLeft,
                                contentDescription = s.back
                            )
                        }
                    }
                },
                actions = {
                    if (isMain) {
                        IconButton(onClick = { nav.navigate(SettingsRoute) }) {
                            Icon(
                                imageVector = Lucide.Settings,
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
                        selected = currentTab == Tab.TODAY,
                        onClick = { nav.navigateToTab(Tab.TODAY) },
                        icon = { Icon(Lucide.House, contentDescription = null) },
                        label = { Text(s.tabToday) }
                    )
                    NavigationBarItem(
                        selected = currentTab == Tab.MEDICINES,
                        onClick = { nav.navigateToTab(Tab.MEDICINES) },
                        icon = { Icon(Lucide.Pill, contentDescription = null) },
                        label = { Text(s.tabMedicines) }
                    )
                    NavigationBarItem(
                        selected = currentTab == Tab.HISTORY,
                        onClick = { nav.navigateToTab(Tab.HISTORY) },
                        icon = { Icon(Lucide.History, contentDescription = null) },
                        label = { Text(s.tabHistory) }
                    )
                    NavigationBarItem(
                        selected = currentTab == Tab.LEARN,
                        onClick = { nav.navigateToTab(Tab.LEARN) },
                        icon = { Icon(Lucide.BookOpen, contentDescription = null) },
                        label = { Text(s.tabLearn) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == Tab.MEDICINES) {
                val scanText = state.lang.scanStrings()
                BackHandler(enabled = fabMenuExpanded) { fabMenuExpanded = false }
                FloatingActionButtonMenu(
                    expanded = fabMenuExpanded,
                    button = {
                        ToggleFloatingActionButton(
                            checked = fabMenuExpanded,
                            onCheckedChange = { fabMenuExpanded = it },
                            modifier = Modifier.semantics {
                                contentDescription = if (fabMenuExpanded) s.close else s.addMedicine
                            }
                        ) {
                            val icon by remember {
                                derivedStateOf {
                                    if (checkedProgress > 0.5f) Lucide.X else Lucide.Plus
                                }
                            }
                            Icon(imageVector = icon, contentDescription = null)
                        }
                    }
                ) {
                    FloatingActionButtonMenuItem(
                        onClick = {
                            fabMenuExpanded = false
                            state.openScanSheet()
                        },
                        icon = { Icon(Lucide.ScanLine, contentDescription = null) },
                        text = { Text(scanText.scanPrescription) }
                    )
                    FloatingActionButtonMenuItem(
                        onClick = {
                            fabMenuExpanded = false
                            state.openAddMedicineSheet()
                        },
                        icon = { Icon(Lucide.Plus, contentDescription = null) },
                        text = { Text(s.addMedicine) }
                    )
                }
            }
        }
    ) { padding ->
        // Tab switches are fade-through (Material spec for bottom nav):
        // no horizontal slide, no layout jump.
        val tabFadeIn = fadeIn(motion.defaultEffectsSpec())
        val tabFadeOut = fadeOut(motion.defaultEffectsSpec())
        // Detail pushes keep a short, small slide so depth is legible
        // without the jarring full-width slide.
        val detailEnter =
            slideInHorizontally(motion.defaultSpatialSpec()) { it / 4 } +
                fadeIn(motion.defaultEffectsSpec())
        val detailPopExit =
            slideOutHorizontally(motion.defaultSpatialSpec()) { it / 4 } +
                fadeOut(motion.defaultEffectsSpec())

        NavHost(
            navController = nav,
            startDestination = TodayRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            enterTransition = { tabFadeIn },
            exitTransition = { tabFadeOut },
            popEnterTransition = { tabFadeIn },
            popExitTransition = { tabFadeOut },
            predictivePopEnterTransition = { tabFadeIn },
            predictivePopExitTransition = { tabFadeOut }
        ) {
            composable<TodayRoute> {
                DashboardScreen(state) { flowSlot = it }
            }

            composable<MedicinesRoute> {
                MedicinesScreen(state)
            }

            composable<HistoryRoute> {
                HistoryScreen(state) { flowSlot = it }
            }

            composable<LearnRoute> {
                LearnScreen(
                    state = state,
                    onOpenDetail = { id ->
                        state.selectedEduId = id
                        nav.navigate(LearnDetailRoute)
                    },
                    onWhyMatters = { nav.navigate(WhyMattersRoute) }
                )
            }

            // Detail screens push in with a short slide so the hierarchy is
            // clear; popping back plays the mirror transition.
            composable<SettingsRoute>(
                enterTransition = { detailEnter },
                popExitTransition = { detailPopExit }
            ) {
                SettingsScreen(state)
            }

            composable<LearnDetailRoute>(
                enterTransition = { detailEnter },
                popExitTransition = { detailPopExit }
            ) {
                LearnDetailScreen(state)
            }

            composable<WhyMattersRoute>(
                enterTransition = { detailEnter },
                popExitTransition = { detailPopExit }
            ) {
                WhyMattersScreen()
            }
        }
    }

    flowSlot?.let { slot ->
        DoseFlowSheet(
            slot = slot,
            state = state,
            onLogged = { message -> pendingLogMessage = message },
            onDismiss = { flowSlot = null }
        )
    }
}

/**
 * Standard bottom-nav navigation: Today is the root of every tab's back
 * stack, so pressing back from Medicines returns to Today instead of
 * exiting the app, and each tab keeps its own state across switches.
 */
private fun NavController.navigateToTab(tab: Tab) {
    val route = when (tab) {
        Tab.TODAY -> TodayRoute
        Tab.MEDICINES -> MedicinesRoute
        Tab.HISTORY -> HistoryRoute
        Tab.LEARN -> LearnRoute
    }
    navigate(route) {
        popUpTo<TodayRoute> {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
