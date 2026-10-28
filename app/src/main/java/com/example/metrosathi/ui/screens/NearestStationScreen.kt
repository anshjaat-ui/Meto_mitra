package com.example.metrosathi.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.metrosathi.data.model.Station
import com.example.metrosathi.domain.SearchEngine
import com.example.metrosathi.ui.MetroUiState
import com.example.metrosathi.ui.components.StationCard
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearestStationScreen(
    uiState: MetroUiState,
    onSetLocation: (Double, Double) -> Unit,
    onStationClick: (String) -> Unit,
    onSetFromStation: (Station) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            fetchDeviceLocation(context, onSetLocation) { isLocating = it }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val userLat = uiState.userLat ?: uiState.selectedCity?.defaultLat ?: 28.6139
    val userLng = uiState.userLng ?: uiState.selectedCity?.defaultLng ?: 77.2090

    val sortedStations = remember(uiState.stations, userLat, userLng) {
        uiState.stations.map { st ->
            val dist = SearchEngine.calculateDistanceMeters(userLat, userLng, st.latitude, st.longitude)
            val walk = SearchEngine.calculateWalkMinutes(dist)
            Triple(st, dist, walk)
        }.sortedBy { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearest Metro Station", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        fetchDeviceLocation(context, onSetLocation) { isLocating = it }
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Refresh GPS")
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
        ) {
            // Location Status Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationSearching, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.userLat != null) "GPS Location Active" else "Defaulting to City Centre",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Showing stations closest to you with estimated walking times.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sortedStations) { (st, dist, walk) ->
                    StationCard(
                        station = st,
                        distanceMeters = dist,
                        walkMinutes = walk,
                        onClick = { onStationClick(st.id) }
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchDeviceLocation(
    context: Context,
    onLocationReceived: (Double, Double) -> Unit,
    setLocating: (Boolean) -> Unit
) {
    try {
        setLocating(true)
        val fused = LocationServices.getFusedLocationProviderClient(context)
        fused.lastLocation.addOnSuccessListener { loc ->
            setLocating(false)
            if (loc != null) {
                onLocationReceived(loc.latitude, loc.longitude)
            }
        }.addOnFailureListener {
            setLocating(false)
        }
    } catch (e: Exception) {
        setLocating(false)
    }
}
