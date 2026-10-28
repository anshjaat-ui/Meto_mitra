package com.example.metrosathi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrosathi.data.model.Station
import com.example.metrosathi.domain.SearchEngine
import com.example.metrosathi.ui.MetroUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FareCalculatorScreen(
    uiState: MetroUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stations = uiState.stations
    var originStation by remember(stations) { mutableStateOf(stations.firstOrNull()) }
    var destinationStation by remember(stations) { mutableStateOf(stations.getOrNull(5) ?: stations.lastOrNull()) }

    var showOriginPicker by remember { mutableStateOf(false) }
    var showDestPicker by remember { mutableStateOf(false) }

    // Distance calculation
    val distanceKm = remember(originStation, destinationStation) {
        if (originStation != null && destinationStation != null && originStation != destinationStation) {
            val meters = SearchEngine.calculateDistanceMeters(
                originStation!!.latitude, originStation!!.longitude,
                destinationStation!!.latitude, destinationStation!!.longitude
            )
            // Aerial distance scaled by 1.3 for rail track curvature
            (meters * 1.3 / 1000.0)
        } else 0.0
    }

    // Token vs Smart card fare
    val tokenFare = remember(distanceKm) {
        when {
            distanceKm <= 2.0 -> 10
            distanceKm <= 5.0 -> 20
            distanceKm <= 12.0 -> 30
            distanceKm <= 21.0 -> 40
            distanceKm <= 32.0 -> 50
            else -> 60
        }
    }
    val smartCardFare = (tokenFare * 0.9).toInt()
    val offPeakFare = (tokenFare * 0.8).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fare Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Origin & Destination Selector
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showOriginPicker = true }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.TripOrigin, contentDescription = null, tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Origin Station", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(originStation?.name ?: "Select Origin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDestPicker = true }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFC62828))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Destination Station", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(destinationStation?.name ?: "Select Destination", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                }
            }

            // Fare Breakdown Cards
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Fare Breakdown (${String.format("%.1f", distanceKm)} km)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FareCard(
                                title = "Smart Card",
                                fare = "₹$smartCardFare",
                                discount = "10% Discount",
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            FareCard(
                                title = "Token / QR",
                                fare = "₹$tokenFare",
                                discount = "Standard Fare",
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Discount, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Sunday & National Holiday Fare: ₹$offPeakFare",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "Additional 10% discount on Smart Cards during off-peak hours (until 08:00 AM, 12:00 PM - 05:00 PM, and after 09:00 PM).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Official Fare Slabs Table
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Official Distance Fare Slabs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        FareSlabRow("Distance", "Token / QR", "Smart Card", isHeader = true)
                        HorizontalDivider()
                        FareSlabRow("0 - 2 km", "₹10", "₹9")
                        FareSlabRow("2 - 5 km", "₹20", "₹18")
                        FareSlabRow("5 - 12 km", "₹30", "₹27")
                        FareSlabRow("12 - 21 km", "₹40", "₹36")
                        FareSlabRow("21 - 32 km", "₹50", "₹45")
                        FareSlabRow("More than 32 km", "₹60", "₹54")
                    }
                }
            }
        }
    }

    if (showOriginPicker || showDestPicker) {
        val isOrigin = showOriginPicker
        ModalBottomSheet(onDismissRequest = { showOriginPicker = false; showDestPicker = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(if (isOrigin) "Select Origin" else "Select Destination", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    items(stations) { st ->
                        ListItem(
                            headlineContent = { Text(st.name, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(st.nameHi) },
                            modifier = Modifier.clickable {
                                if (isOrigin) originStation = st else destinationStation = st
                                showOriginPicker = false
                                showDestPicker = false
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun FareCard(
    title: String,
    fare: String,
    discount: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = contentColor.copy(alpha = 0.85f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(fare, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = contentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(discount, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = contentColor.copy(alpha = 0.9f))
        }
    }
}

@Composable
fun FareSlabRow(col1: String, col2: String, col3: String, isHeader: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(col1, fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
        Text(col2, fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Medium, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(col3, fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Bold, color = if (!isHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    }
}
