package com.example.metrosathi.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.metrosathi.data.model.Station
import com.example.metrosathi.service.DestinationAlarmService
import com.example.metrosathi.ui.MetroUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelAlarmScreen(
    uiState: MetroUiState,
    onStartAlarm: (Context, Station, Int) -> Unit,
    onStopAlarm: (Context) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedDestination by remember(uiState.stations) {
        mutableStateOf(uiState.toStation ?: uiState.stations.lastOrNull())
    }
    var aheadStationsCount by remember { mutableIntStateOf(1) }
    var showPicker by remember { mutableStateOf(false) }

    val isRunning = DestinationAlarmService.isRunning || uiState.isAlarmActive

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Destination Travel Alarm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Alarm Status Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isRunning) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(if (isRunning) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isRunning) Icons.Default.AlarmOn else Icons.Default.Alarm,
                            contentDescription = null,
                            tint = if (isRunning) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isRunning) "Travel Alarm is Active" else "Alarm is Inactive",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isRunning) "Foreground GPS service running. Works even with phone locked."
                            else "Set a wake-up reminder before reaching your station.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Destination Station Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Target Destination Station", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPicker = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedDestination?.name ?: "Select Station",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            }

            // Ahead Stations Options
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Alert Trigger Distance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = aheadStationsCount == 1,
                            onClick = { aheadStationsCount = 1 },
                            label = { Text("1 Station Before (~1.5 km)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = aheadStationsCount == 2,
                            onClick = { aheadStationsCount = 2 },
                            label = { Text("2 Stations Before (~3 km)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Battery Optimization Note
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Android battery saver may throttle background GPS. Please allow background location for 100% reliable wake-up rings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Button
            if (isRunning) {
                Button(
                    onClick = { onStopAlarm(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("stop_alarm_button")
                ) {
                    Icon(Icons.Default.AlarmOff, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop Travel Alarm", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        selectedDestination?.let { st ->
                            onStartAlarm(context, st, aheadStationsCount)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_alarm_button")
                ) {
                    Icon(Icons.Default.Alarm, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Travel Alarm", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showPicker) {
        ModalBottomSheet(onDismissRequest = { showPicker = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Select Target Station", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    items(uiState.stations) { st ->
                        ListItem(
                            headlineContent = { Text(st.name, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(st.nameHi) },
                            modifier = Modifier.clickable {
                                selectedDestination = st
                                showPicker = false
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
