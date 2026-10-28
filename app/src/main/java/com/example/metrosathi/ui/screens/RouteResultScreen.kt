package com.example.metrosathi.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrosathi.data.model.Station
import com.example.metrosathi.domain.PlannedRoute
import com.example.metrosathi.domain.RouteLeg
import com.example.metrosathi.ui.MetroUiState
import com.example.metrosathi.ui.components.LineBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteResultScreen(
    uiState: MetroUiState,
    onSelectAlternativeRoute: (PlannedRoute) -> Unit,
    onSaveFavourite: () -> Unit,
    onLogTrip: () -> Unit,
    onSetAlarmForDestination: (Station) -> Unit,
    onStationClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val routes = uiState.plannedRoutes
    val route = uiState.selectedRoute ?: routes.firstOrNull()
    var isFavouriteSaved by remember { mutableStateOf(false) }
    var isTripLogged by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (route != null) {
                                shareRouteText(context, route)
                            }
                        },
                        modifier = Modifier.testTag("share_route_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Route")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (route == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No route selected or found.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Alternative Routes Tabs
            if (routes.size > 1) {
                item {
                    Text(
                        text = "Available Routes",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(routes) { r ->
                            val isSelected = r.id == route.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectAlternativeRoute(r) },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(r.title, fontWeight = FontWeight.Bold)
                                        Text("${r.totalTimeMinutes}m • ${r.interchangeCount} chg", fontSize = 11.sp)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Route Highlights Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${route.totalTimeMinutes} Minutes",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${route.totalDistanceKm} km • ${route.totalStationsCount} Stations",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "₹${route.smartCardFare}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Token: ₹${route.tokenFare}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Action Buttons (Save Favourite, Set Alarm, Mark Done)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onSaveFavourite()
                                    isFavouriteSaved = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFavouriteSaved) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isFavouriteSaved) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    if (isFavouriteSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isFavouriteSaved) "Saved" else "Save", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    route.allStationsInOrder.lastOrNull()?.let { dest ->
                                        onSetAlarmForDestination(dest)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.1f)
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Travel Alarm", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    onLogTrip()
                                    isTripLogged = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTripLogged) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isTripLogged) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isTripLogged) "Logged" else "Log Trip", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Step-by-Step Guidance Header
            item {
                Text(
                    text = "Step-by-Step Route Itinerary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Route Legs
            items(route.legs) { leg ->
                RouteLegCard(leg = leg, onStationClick = onStationClick)
            }

            // Final Alight Step
            item {
                val dest = route.allStationsInOrder.lastOrNull()
                if (dest != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFC62828), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Alight at ${dest.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Destination reached • Check signage for nearest Exit Gate",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouteLegCard(
    leg: RouteLeg,
    onStationClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val lineColor = try {
        Color(android.graphics.Color.parseColor(leg.line.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Line Badge & Direction
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LineBadge(line = leg.line)
                Text(
                    text = "Platform ${leg.platformNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Boarding Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(lineColor, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Board at ${leg.fromStation.name}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Towards ${leg.directionTerminal}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Transit duration & station count
            Row(
                modifier = Modifier
                    .padding(start = 7.dp)
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(30.dp)
                        .background(lineColor)
                )
                Spacer(modifier = Modifier.width(17.dp))
                Text(
                    text = "Ride ${leg.stations.size - 1} stations (${leg.durationSeconds / 60} mins)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Collapsible intermediate stations list
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 26.dp, bottom = 8.dp)) {
                    leg.stations.drop(1).dropLast(1).forEach { st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStationClick(st.id) }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color.Gray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = st.name,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Alight / Interchange Station
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(lineColor, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = leg.toStation.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (leg.toStation.isInterchange) {
                        Text(
                            text = "Interchange Station",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

fun shareRouteText(context: Context, route: PlannedRoute) {
    val from = route.allStationsInOrder.firstOrNull()?.name ?: "Origin"
    val to = route.allStationsInOrder.lastOrNull()?.name ?: "Destination"
    val sb = StringBuilder()
    sb.append("🚇 *MetroSathi Route Guide*\n")
    sb.append("*From:* $from\n")
    sb.append("*To:* $to\n\n")
    sb.append("⏱ *Time:* ${route.totalTimeMinutes} mins\n")
    sb.append("🚉 *Stations:* ${route.totalStationsCount}\n")
    sb.append("🔄 *Interchanges:* ${route.interchangeCount}\n")
    sb.append("💰 *Fare:* ₹${route.smartCardFare} (Smart Card) / ₹${route.tokenFare} (Token)\n\n")
    sb.append("*Steps:*\n")

    route.legs.forEachIndexed { idx, leg ->
        sb.append("${idx + 1}. Board at ${leg.fromStation.name} on ${leg.line.name} towards ${leg.directionTerminal} (Platform ${leg.platformNumber})\n")
        sb.append("   • Ride ${leg.stations.size - 1} stations to ${leg.toStation.name}\n")
    }
    sb.append("🏁 Alight at $to.\n\n")
    sb.append("Planned via MetroSathi - Indian Metro Companion")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Metro Route")
    context.startActivity(shareIntent)
}
