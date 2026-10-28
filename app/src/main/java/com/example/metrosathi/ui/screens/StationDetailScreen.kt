package com.example.metrosathi.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import com.example.metrosathi.data.model.NearbyPlace
import com.example.metrosathi.data.repository.FullStationDetails
import com.example.metrosathi.ui.components.AmenityChip
import com.example.metrosathi.ui.components.LineBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailScreen(
    details: FullStationDetails?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showReportDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Overview",
        "Gates & Exits",
        "Amenities",
        "Parking",
        "Nearby Places",
        "Train Timings",
        "Accessibility"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = details?.station?.name ?: "Station Guide",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (details?.station?.nameHi?.isNotEmpty() == true) {
                            Text(
                                text = details.station.nameHi,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier.testTag("report_correction_button")
                    ) {
                        Icon(Icons.Default.Feedback, contentDescription = "Report Correction")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (details == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val station = details.station

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> { // Overview Tab
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("General Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    DetailRow(label = "Station Name (EN)", value = station.name)
                                    DetailRow(label = "Station Name (Hindi)", value = station.nameHi)
                                    DetailRow(label = "Station Code", value = station.code)
                                    DetailRow(label = "Layout", value = station.layoutType)
                                    DetailRow(label = "Opening Year", value = "${station.openingYear}")
                                    DetailRow(label = "Number of Platforms", value = "${station.platformsCount}")
                                    DetailRow(label = "Wheelchair Accessible", value = if (station.isWheelchairAccessible) "Yes (Step-free)" else "Partial")
                                    DetailRow(label = "Station Helpline", value = station.contactNumber)
                                }
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Connected Lines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        details.lines.forEach { line ->
                                            LineBadge(line = line)
                                        }
                                    }
                                }
                            }
                        }

                        if (details.interchanges.isNotEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Interchange Transfer Guide", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        details.interchanges.forEach { ic ->
                                            Text(
                                                text = "• Transfer Time: ~${ic.walkingTimeMinutes} minutes",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = ic.directions,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // Gates & Exits Tab
                        if (details.gates.isEmpty()) {
                            item {
                                Text("No gate information registered for this station yet.")
                            }
                        } else {
                            items(details.gates) { gate ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(1.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(gate.gateNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                if (gate.hasLift) {
                                                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(6.dp)) {
                                                        Text("Lift", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                    }
                                                }
                                                if (gate.hasEscalator) {
                                                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
                                                        Text("Escalator", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                    }
                                                }
                                                if (gate.isWheelchairAccessible) {
                                                    Icon(Icons.Default.Accessible, contentDescription = "Wheelchair", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Direction / Facing: ${gate.facingDirectionOrLandmark}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        if (gate.nearbyLandmarks.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Closest to: ${gate.nearbyLandmarks}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // Amenities Tab
                        item {
                            Text("Station Facilities & Amenities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        if (details.amenities.isEmpty()) {
                            item {
                                Text("Standard amenities (Ticket Counters, Security Check, Drinking Water, Restrooms) available on concourse.")
                            }
                        } else {
                            items(details.amenities) { am ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AmenityChip(type = am.type)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(am.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                            Text("Location: ${am.locationInsideStation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> { // Parking Tab
                        item {
                            val parking = details.parking
                            if (parking == null || !parking.available) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("No Official Metro Parking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = parking?.chargesDescription ?: "No dedicated parking available at this station. Use public municipal parking nearby.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalParking, contentDescription = null, tint = Color(0xFF2E7D32))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Parking Facility Available", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        DetailRow(label = "2-Wheeler Capacity", value = "${parking.twoWheelerCapacity} slots")
                                        DetailRow(label = "4-Wheeler Capacity", value = "${parking.fourWheelerCapacity} slots")
                                        DetailRow(label = "Bicycle Capacity", value = "${parking.cycleCapacity} slots")
                                        DetailRow(label = "Timings", value = parking.timings)
                                        DetailRow(label = "Charges & Tariffs", value = parking.chargesDescription)
                                        DetailRow(label = "Location Notes", value = parking.locationNotes)
                                    }
                                }
                            }
                        }
                    }

                    4 -> { // Nearby Places Tab
                        if (details.nearbyPlaces.isEmpty()) {
                            item {
                                Text("No landmark data available for this station yet.")
                            }
                        } else {
                            items(details.nearbyPlaces) { place ->
                                NearbyPlaceCard(place = place, onNavigate = { openMapsDirections(context, place.name) })
                            }
                        }
                    }

                    5 -> { // Train Timings Tab
                        if (details.timings.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Standard Timetable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("First Train: ~05:45 AM • Last Train: ~11:30 PM\nAverage frequency: 3-5 mins during peak hours, 7-10 mins non-peak.")
                                    }
                                }
                            }
                        } else {
                            items(details.timings) { tm ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Towards ${tm.directionTerminal}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        DetailRow(label = "First Train (Mon - Sat)", value = tm.firstTrainWeekday)
                                        DetailRow(label = "Last Train (Mon - Sat)", value = tm.lastTrainWeekday)
                                        DetailRow(label = "First Train (Sunday)", value = tm.firstTrainSunday)
                                        DetailRow(label = "Last Train (Sunday)", value = tm.lastTrainSunday)
                                        DetailRow(label = "Peak Frequency", value = "${tm.peakFrequencyMin} mins")
                                        DetailRow(label = "Non-Peak Frequency", value = "${tm.nonPeakFrequencyMin} mins")
                                    }
                                }
                            }
                        }
                    }

                    6 -> { // Accessibility Tab
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Passenger Accessibility & Women Safety", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    DetailRow(label = "Ladies Coach Guide", value = "First coach in the moving direction is strictly reserved for women commuters.")
                                    DetailRow(label = "Tactile Paths", value = "Yellow tactile guidance tiles present on all platforms & concourses for visually impaired.")
                                    DetailRow(label = "Divyangjan Assistance", value = "Wheelchair & staff assistance available at Customer Care Centre.")
                                    DetailRow(label = "CISF Security Helpline", value = "155370")
                                    DetailRow(label = "Women Helpline", value = "1091 / 011-23415404")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Data Correction") },
            text = {
                Text("Did you notice an outdated gate landmark, lift maintenance status, or timing change? Your feedback helps maintain accurate metro data.")
            },
            confirmButton = {
                Button(onClick = { showReportDialog = false }) {
                    Text("Submit Suggestion")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun NearbyPlaceCard(place: NearbyPlace, onNavigate: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(place.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "${place.category} • Exit via ${place.recommendedGate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${place.walkingMinutes} min walk (${place.distanceMeters} m)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onNavigate) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Navigate", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

fun openMapsDirections(context: Context, placeName: String) {
    try {
        val uri = Uri.parse("geo:0,0?q=" + Uri.encode(placeName))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
