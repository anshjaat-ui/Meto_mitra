package com.example.metrosathi.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrosathi.data.model.Line
import com.example.metrosathi.data.model.Station
import com.example.metrosathi.ui.MetroUiState
import com.example.metrosathi.ui.components.LineBadge
import com.example.ui.theme.MetroLineColors
import kotlin.math.hypot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetroMapScreen(
    uiState: MetroUiState,
    onStationSelected: (String) -> Unit,
    onSetFromStation: (Station) -> Unit,
    onSetToStation: (Station) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isGeographicView by remember { mutableStateOf(false) }
    var selectedStationForSheet by remember { mutableStateOf<Station?>(null) }
    var hiddenLineIds by remember { mutableStateOf(setOf<String>()) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val stations = uiState.stations
    val lines = uiState.lines
    val plannedRoute = uiState.selectedRoute

    // Active route stations for highlighting
    val routeStationIds = remember(plannedRoute) {
        plannedRoute?.allStationsInOrder?.map { it.id }?.toSet() ?: emptySet()
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Main Interactive Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isGeographicView) Color(0xFFF0F4F8) else MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.4f, 4.0f)
                        offset += pan
                    }
                }
                .pointerInput(stations, scale, offset, isGeographicView) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            scale = if (scale > 1.5f) 1.0f else 2.2f
                            offset = Offset.Zero
                        },
                        onTap = { tapOffset ->
                            // Hit test for stations
                            val canvasTap = (tapOffset - offset) / scale
                            var clickedStation: Station? = null
                            var minDistance = Float.MAX_VALUE

                            stations.forEach { station ->
                                val (sx, sy) = if (isGeographicView) {
                                    val latNorm = ((station.latitude - 28.4) / 0.4).toFloat().coerceIn(0f, 1f)
                                    val lngNorm = ((station.longitude - 77.0) / 0.4).toFloat().coerceIn(0f, 1f)
                                    Pair(lngNorm * 1000f + 100f, (1f - latNorm) * 1000f + 100f)
                                } else {
                                    Pair(station.schematicX, station.schematicY)
                                }

                                val dist = hypot(canvasTap.x - sx, canvasTap.y - sy)
                                if (dist < 35f && dist < minDistance) {
                                    minDistance = dist
                                    clickedStation = station
                                }
                            }

                            if (clickedStation != null) {
                                selectedStationForSheet = clickedStation
                            }
                        }
                    )
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Render Metro Lines
            lines.filter { it.id !in hiddenLineIds }.forEach { line ->
                val lineStations = stations.filter { st ->
                    st.id.contains(line.id.replace("delhi_", "").replace("mumbai_", "").replace("blr_", ""))
                }.ifEmpty {
                    if (line.id.contains("yellow")) stations.take(6)
                    else if (line.id.contains("blue")) stations.takeLast(6)
                    else emptyList()
                }

                if (lineStations.size >= 2) {
                    val lineColor = try {
                        Color(android.graphics.Color.parseColor(line.colorHex))
                    } catch (e: Exception) {
                        MetroLineColors.getColorForLine(line.name)
                    }

                    val path = Path()
                    lineStations.forEachIndexed { idx, st ->
                        val (x, y) = if (isGeographicView) {
                            val latNorm = ((st.latitude - 28.4) / 0.4).toFloat().coerceIn(0f, 1f)
                            val lngNorm = ((st.longitude - 77.0) / 0.4).toFloat().coerceIn(0f, 1f)
                            Pair(lngNorm * 1000f + 100f, (1f - latNorm) * 1000f + 100f)
                        } else {
                            Pair(st.schematicX, st.schematicY)
                        }

                        val screenX = x * scale + offset.x
                        val screenY = y * scale + offset.y

                        if (idx == 0) path.moveTo(screenX, screenY)
                        else path.lineTo(screenX, screenY)
                    }

                    drawPath(
                        path = path,
                        color = lineColor.copy(alpha = 0.85f),
                        style = Stroke(width = 12f * scale)
                    )
                }
            }

            // Render Route Highlight if active
            if (plannedRoute != null && plannedRoute.allStationsInOrder.size >= 2) {
                val routePath = Path()
                plannedRoute.allStationsInOrder.forEachIndexed { idx, st ->
                    val (x, y) = if (isGeographicView) {
                        val latNorm = ((st.latitude - 28.4) / 0.4).toFloat().coerceIn(0f, 1f)
                        val lngNorm = ((st.longitude - 77.0) / 0.4).toFloat().coerceIn(0f, 1f)
                        Pair(lngNorm * 1000f + 100f, (1f - latNorm) * 1000f + 100f)
                    } else {
                        Pair(st.schematicX, st.schematicY)
                    }
                    val screenX = x * scale + offset.x
                    val screenY = y * scale + offset.y

                    if (idx == 0) routePath.moveTo(screenX, screenY)
                    else routePath.lineTo(screenX, screenY)
                }
                drawPath(
                    path = routePath,
                    color = Color(0xFF00E676),
                    style = Stroke(width = 6f * scale)
                )
            }

            // Render Station Dots
            stations.forEach { station ->
                val (x, y) = if (isGeographicView) {
                    val latNorm = ((station.latitude - 28.4) / 0.4).toFloat().coerceIn(0f, 1f)
                    val lngNorm = ((station.longitude - 77.0) / 0.4).toFloat().coerceIn(0f, 1f)
                    Pair(lngNorm * 1000f + 100f, (1f - latNorm) * 1000f + 100f)
                } else {
                    Pair(station.schematicX, station.schematicY)
                }

                val screenX = x * scale + offset.x
                val screenY = y * scale + offset.y

                val isRouteStation = station.id in routeStationIds
                val dotRadius = if (station.isInterchange) 14f * scale else 9f * scale

                // Interchange concentric outer ring
                if (station.isInterchange) {
                    drawCircle(
                        color = Color.Black,
                        radius = dotRadius + 5f * scale,
                        center = Offset(screenX, screenY),
                        style = Stroke(width = 3f * scale)
                    )
                }

                // Station fill dot
                drawCircle(
                    color = if (isRouteStation) Color(0xFF00E676) else Color.White,
                    radius = dotRadius,
                    center = Offset(screenX, screenY)
                )
                drawCircle(
                    color = if (isRouteStation) Color(0xFF1B5E20) else Color(0xFF1A237E),
                    radius = dotRadius,
                    center = Offset(screenX, screenY),
                    style = Stroke(width = 3f * scale)
                )
            }
        }

        // Top Toolbar Overlay
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // View Mode Toggle (Schematic vs Geographic)
                FilterChip(
                    selected = !isGeographicView,
                    onClick = { isGeographicView = false },
                    label = { Text("Schematic") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Schema,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                FilterChip(
                    selected = isGeographicView,
                    onClick = { isGeographicView = true },
                    label = { Text("Geographic") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                // Line Filter Button
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier.testTag("filter_lines_button")
                ) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.offset(x = 6.dp, y = (-6).dp)
                    ) {
                        Text("${lines.size - hiddenLineIds.size}")
                    }
                    Icon(Icons.Default.FilterList, contentDescription = "Filter Lines")
                }
            }
        }

        // Floating Map Controls (Zoom in, Zoom out, Fit to Screen)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { scale = (scale * 1.3f).coerceAtMost(4.0f) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("zoom_in_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }

            SmallFloatingActionButton(
                onClick = { scale = (scale / 1.3f).coerceAtLeast(0.4f) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("zoom_out_button")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }

            FloatingActionButton(
                onClick = {
                    scale = 1.0f
                    offset = Offset.Zero
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("fit_to_screen_button")
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Fit to Screen")
            }
        }

        // Bottom Sheet for Tapped Station
        if (selectedStationForSheet != null) {
            val station = selectedStationForSheet!!
            ModalBottomSheet(
                onDismissRequest = { selectedStationForSheet = null },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (station.nameHi.isNotEmpty()) {
                                Text(
                                    text = station.nameHi,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (station.code.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = station.code,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(station.layoutType) },
                            icon = { Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )

                        if (station.isInterchange) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Interchange") },
                                icon = { Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }

                        if (station.hasParking) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Parking") },
                                icon = { Icon(Icons.Default.LocalParking, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSetFromStation(station)
                                selectedStationForSheet = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.TripOrigin, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set as From")
                        }

                        OutlinedButton(
                            onClick = {
                                onSetToStation(station)
                                selectedStationForSheet = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set as To")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            selectedStationForSheet = null
                            onStationSelected(station.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Full Station Guide")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Line Filter Bottom Sheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Filter Metro Lines",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Show or hide specific lines on the map",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    lines.forEach { line ->
                        val isVisible = line.id !in hiddenLineIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LineBadge(line = line)
                            Checkbox(
                                checked = isVisible,
                                onCheckedChange = { checked ->
                                    hiddenLineIds = if (checked) {
                                        hiddenLineIds - line.id
                                    } else {
                                        hiddenLineIds + line.id
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
