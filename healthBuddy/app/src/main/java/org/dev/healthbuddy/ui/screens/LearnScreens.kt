package org.dev.healthbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.dev.healthbuddy.data.AppState
import org.dev.healthbuddy.domain.EduCard
import org.dev.healthbuddy.domain.EduIcon
import org.dev.healthbuddy.domain.Education
import org.dev.healthbuddy.ui.components.HbCard
import org.dev.healthbuddy.ui.i18n.LocalStrings

private fun iconFor(icon: EduIcon): ImageVector = when (icon) {
    EduIcon.HEART -> Icons.Filled.Favorite
    EduIcon.COST -> Icons.Filled.ShoppingCart
    EduIcon.SIDE_EFFECT -> Icons.Filled.Info
    EduIcon.CLOCK -> Icons.Filled.DateRange
    EduIcon.WARNING -> Icons.Filled.Warning
    EduIcon.MIND -> Icons.Filled.Person
}

@Composable
fun LearnScreen(
    state: AppState,
    onOpenDetail: (String) -> Unit,
    onWhyMatters: () -> Unit
) {
    val s = LocalStrings.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = s.learnSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(Education.cards, key = { it.id }) { card ->
            EduRow(
                card = card,
                lang = state.lang,
                onClick = { onOpenDetail(card.id) }
            )
        }

        item {
            HbCard(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = onWhyMatters
            ) {
                Text(
                    text = s.whyMattersTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = s.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            Text(
                text = s.disclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EduRow(card: EduCard, lang: org.dev.healthbuddy.domain.Lang, onClick: () -> Unit) {
    val text = card.text(lang)
    HbCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconFor(card.icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = text.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun LearnDetailScreen(state: AppState) {
    val s = LocalStrings.current
    val card = Education.byId(state.selectedEduId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        if (card == null) {
            Text(s.learnSubtitle, style = MaterialTheme.typography.bodyMedium)
        } else {
            val text = card.text(state.lang)
            Spacer(Modifier.height(8.dp))
            Text(text = text.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text(
                text = text.body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(28.dp))
            HbCard(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Text(
                    text = s.disclaimer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
