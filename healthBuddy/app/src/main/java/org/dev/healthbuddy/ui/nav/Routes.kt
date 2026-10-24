package org.dev.healthbuddy.ui.nav

import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation Compose destinations.
 * Each one is a `data object`, so no arguments are needed yet — when a screen
 * needs one (e.g. an education card id) turn it into a `data class` and the
 * compiler will keep the call sites honest.
 */
@Serializable
data object MainRoute

@Serializable
data object SettingsRoute

@Serializable
data object AddMedicineRoute

@Serializable
data object LearnDetailRoute

@Serializable
data object WhyMattersRoute
