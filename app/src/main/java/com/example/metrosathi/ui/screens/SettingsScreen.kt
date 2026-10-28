package com.example.metrosathi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.metrosathi.data.local.ThemeMode
import com.example.metrosathi.ui.MetroUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: MetroUiState,
    onNavigateToCitySelect: () -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetLanguage: (String) -> Unit,
    onSetFontScale: (Float) -> Unit,
    onClearAllData: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.appSettings
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
            // Metro Network Selection
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Active Metro Network", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        ListItem(
                            headlineContent = { Text(uiState.selectedCity?.name ?: "Delhi-NCR", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(uiState.selectedCity?.systemName ?: "Metro") },
                            trailingContent = {
                                TextButton(onClick = onNavigateToCitySelect) {
                                    Text("Switch")
                                }
                            },
                            modifier = Modifier.clickable { onNavigateToCitySelect() }
                        )
                    }
                }
            }

            // Appearance & Theme
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Appearance & Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = settings.themeMode == ThemeMode.SYSTEM,
                                onClick = { onSetThemeMode(ThemeMode.SYSTEM) },
                                shape = SegmentedButtonDefaults.itemShape(0, 3)
                            ) {
                                Text("System")
                            }
                            SegmentedButton(
                                selected = settings.themeMode == ThemeMode.LIGHT,
                                onClick = { onSetThemeMode(ThemeMode.LIGHT) },
                                shape = SegmentedButtonDefaults.itemShape(1, 3)
                            ) {
                                Text("Light")
                            }
                            SegmentedButton(
                                selected = settings.themeMode == ThemeMode.DARK,
                                onClick = { onSetThemeMode(ThemeMode.DARK) },
                                shape = SegmentedButtonDefaults.itemShape(2, 3)
                            ) {
                                Text("Dark")
                            }
                        }
                    }
                }
            }

            // Language Switcher
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Language / भाषा", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = settings.selectedLanguage == "en",
                                onClick = { onSetLanguage("en") },
                                label = { Text("English") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = settings.selectedLanguage == "hi",
                                onClick = { onSetLanguage("hi") },
                                label = { Text("हिंदी (Hindi)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Data & Cache Management
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Data & Storage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onClearAllData,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear Trip History & Recent Searches")
                        }
                    }
                }
            }

            // About & Legal
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("About & Privacy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        ListItem(
                            headlineContent = { Text("About MetroSathi") },
                            supportingContent = { Text("Version 1.0 • Offline-first transit companion") },
                            modifier = Modifier.clickable { showAboutDialog = true }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Privacy Policy") },
                            supportingContent = { Text("No location or personal data is collected or shared") },
                            modifier = Modifier.clickable { showPrivacyDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About MetroSathi") },
            text = {
                Text(
                    "MetroSathi is an all-in-one companion for rapid transit across 17+ Indian metro networks.\n\n" +
                    "Features full offline route planning, interactive schematic & geographic canvas maps, distance-based fare calculations, destination wake-up alarms, smart card balance reminders, and station gate accessibility guides."
                )
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Text(
                    "MetroSathi is built with privacy by design:\n\n" +
                    "1. All metro routes, maps, and fare tables work 100% offline on your device.\n" +
                    "2. Location permissions are used strictly in real-time to compute the nearest station and trigger local destination alarms. No location telemetry is ever uploaded to remote servers.\n" +
                    "3. Card balance and trip history are stored securely in local Room database on your phone."
                )
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }
}
