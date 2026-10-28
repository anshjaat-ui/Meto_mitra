package com.example.metrosathi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.metrosathi.ui.MetroUiState
import com.example.metrosathi.ui.components.LineBadge
import com.example.metrosathi.ui.components.StationCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationsDirectoryScreen(
    uiState: MetroUiState,
    onStationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLineId by remember { mutableStateOf<String?>(null) }

    val stations = uiState.stations
    val lines = uiState.lines

    val filteredStations = remember(searchQuery, selectedLineId, stations) {
        stations.filter { st ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                st.name.contains(searchQuery, ignoreCase = true) ||
                st.nameHi.contains(searchQuery, ignoreCase = true) ||
                st.code.contains(searchQuery, ignoreCase = true)
            }
            val matchesLine = if (selectedLineId == null) true else {
                val lineKey = selectedLineId!!.replace("delhi_", "").replace("mumbai_", "").replace("blr_", "")
                st.id.contains(lineKey)
            }
            matchesQuery && matchesLine
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Stations Directory",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${uiState.selectedCity?.name ?: "Metro"} • ${stations.size} Total Stations",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, Hindi, code…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("stations_directory_search")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Line Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedLineId == null,
                    onClick = { selectedLineId = null },
                    label = { Text("All Lines") }
                )
            }
            items(lines) { line ->
                FilterChip(
                    selected = selectedLineId == line.id,
                    onClick = {
                        selectedLineId = if (selectedLineId == line.id) null else line.id
                    },
                    label = { Text(line.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Showing ${filteredStations.size} stations",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredStations) { station ->
                val matchedLines = lines.filter { line ->
                    val lineKey = line.id.replace("delhi_", "").replace("mumbai_", "").replace("blr_", "")
                    station.id.contains(lineKey)
                }.ifEmpty { lines.take(1) }

                StationCard(
                    station = station,
                    lines = matchedLines,
                    onClick = { onStationClick(station.id) }
                )
            }
        }
    }
}
