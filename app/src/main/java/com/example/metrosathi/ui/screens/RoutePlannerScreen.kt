package com.example.metrosathi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrosathi.data.model.Station
import com.example.metrosathi.domain.RouteMode
import com.example.metrosathi.ui.MetroUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerScreen(
    uiState: MetroUiState,
    onFromStationChange: (Station) -> Unit,
    onToStationChange: (Station) -> Unit,
    onSwapStations: () -> Unit,
    onPlanRoute: () -> Unit,
    onUseMyLocation: () -> Unit,
    onStationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf(RouteMode.FASTEST) }

    val fromStation = uiState.fromStation
    val toStation = uiState.toStation
    val stations = uiState.stations

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Metro Route Planner",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Dijkstra optimal path with interchange penalty",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Station Selection Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Origin (From)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFromPicker = true }
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF2E7D32), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "From Station",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = fromStation?.name ?: "Select Starting Station",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select From Station",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Divider with Swap button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onSwapStations,
                        modifier = Modifier.testTag("swap_stations_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                Icons.Default.SwapVert,
                                contentDescription = "Swap Stations",
                                modifier = Modifier.padding(6.dp).size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                // Destination (To)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showToPicker = true }
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFC62828), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "To Station",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = toStation?.name ?: "Select Destination Station",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select To Station",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Use My Location quick action
        OutlinedButton(
            onClick = onUseMyLocation,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("use_my_location_button")
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Set Nearest Station to My Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Route Optimization Mode Selector
        Text(
            text = "Optimization Priority",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedMode == RouteMode.FASTEST,
                onClick = { selectedMode = RouteMode.FASTEST },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) {
                Text("Fastest", fontSize = 12.sp)
            }
            SegmentedButton(
                selected = selectedMode == RouteMode.LEAST_INTERCHANGES,
                onClick = { selectedMode = RouteMode.LEAST_INTERCHANGES },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) {
                Text("Least Chg", fontSize = 12.sp)
            }
            SegmentedButton(
                selected = selectedMode == RouteMode.FEWEST_STATIONS,
                onClick = { selectedMode = RouteMode.FEWEST_STATIONS },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) {
                Text("Few Stations", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Plan Route Button
        Button(
            onClick = onPlanRoute,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("find_route_button")
        ) {
            Icon(Icons.Default.DirectionsTransit, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Find Best Route", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    // Modal Sheet for Selecting Station (From or To)
    if (showFromPicker || showToPicker) {
        val isSelectingFrom = showFromPicker
        var pickerQuery by remember { mutableStateOf("") }
        val filteredList = remember(pickerQuery, stations) {
            if (pickerQuery.isBlank()) stations
            else stations.filter {
                it.name.contains(pickerQuery, ignoreCase = true) ||
                it.nameHi.contains(pickerQuery, ignoreCase = true) ||
                it.code.contains(pickerQuery, ignoreCase = true)
            }
        }

        ModalBottomSheet(
            onDismissRequest = {
                showFromPicker = false
                showToPicker = false
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isSelectingFrom) "Select Origin Station" else "Select Destination Station",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pickerQuery,
                    onValueChange = { pickerQuery = it },
                    placeholder = { Text("Search by name, Hindi, code…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(filteredList) { st ->
                        ListItem(
                            headlineContent = { Text(st.name, fontWeight = FontWeight.SemiBold) },
                            supportingContent = {
                                Text(
                                    if (st.nameHi.isNotEmpty()) "${st.nameHi} • ${st.layoutType}" else st.layoutType,
                                    fontSize = 12.sp
                                )
                            },
                            trailingContent = {
                                if (st.isInterchange) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            "Interchange",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelectingFrom) onFromStationChange(st)
                                    else onToStationChange(st)
                                    showFromPicker = false
                                    showToPicker = false
                                }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
