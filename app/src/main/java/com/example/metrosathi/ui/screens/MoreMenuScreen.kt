package com.example.metrosathi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.metrosathi.ui.MetroUiState

@Composable
fun MoreMenuScreen(
    uiState: MetroUiState,
    onNavigateToNearest: () -> Unit,
    onNavigateToFare: () -> Unit,
    onNavigateToCardBalance: () -> Unit,
    onNavigateToAlarm: () -> Unit,
    onNavigateToTripHistory: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCitySelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = "Services & Tools",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Transit utilities for ${uiState.selectedCity?.name ?: "Metro"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    MoreMenuItem(
                        icon = Icons.Default.LocationCity,
                        title = "Switch Metro City",
                        subtitle = "Currently: ${uiState.selectedCity?.name ?: "Delhi-NCR"}",
                        onClick = onNavigateToCitySelect
                    )
                    HorizontalDivider()
                    MoreMenuItem(
                        icon = Icons.Default.NearMe,
                        title = "Nearest Metro Station",
                        subtitle = "GPS proximity & walking guide",
                        onClick = onNavigateToNearest
                    )
                    HorizontalDivider()
                    MoreMenuItem(
                        icon = Icons.Default.Calculate,
                        title = "Fare Calculator",
                        subtitle = "Smart Card & Token price estimator",
                        onClick = onNavigateToFare
                    )
                    HorizontalDivider()
                    MoreMenuItem(
                        icon = Icons.Default.CreditCard,
                        title = "Card Balance Reminder",
                        subtitle = "Keep track of smart card balance",
                        onClick = onNavigateToCardBalance
                    )
                    HorizontalDivider()
                    MoreMenuItem(
                        icon = Icons.Default.Alarm,
                        title = "Destination Travel Alarm",
                        subtitle = "Foreground wake-up alert before deboarding",
                        onClick = onNavigateToAlarm
                    )
                    HorizontalDivider()
                    MoreMenuItem(
                        icon = Icons.Default.History,
                        title = "Trip History & Spend",
                        subtitle = "Logged rides & monthly travel expenses",
                        onClick = onNavigateToTripHistory
                    )
                    HorizontalDivider()
                    MoreMenuItem(
                        icon = Icons.Default.Gavel,
                        title = "Metro Rules, Fines & Helpline",
                        subtitle = "Luggage allowances, penalties & 24/7 contacts",
                        onClick = onNavigateToRules
                    )
                    HorizontalDivider()
                    MoreMenuItem(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        subtitle = "Theme, language, font scale, about & privacy",
                        onClick = onNavigateToSettings
                    )
                }
            }
        }
    }
}

@Composable
fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("menu_item_${title.lowercase().replace(" ", "_")}")
    )
}
