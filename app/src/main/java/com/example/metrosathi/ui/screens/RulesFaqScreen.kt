package com.example.metrosathi.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrosathi.ui.MetroUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesFaqScreen(
    uiState: MetroUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val city = uiState.selectedCity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Metro Rules & Helpline", fontWeight = FontWeight.Bold) },
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
            // Helplines Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("24/7 Passenger Emergency Helplines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        HelplineActionRow(
                            label = "${city?.name ?: "Metro"} General Customer Helpline",
                            number = city?.helplineNumber?.ifEmpty { "155370" } ?: "155370",
                            onDial = { dialNumber(context, city?.helplineNumber ?: "155370") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HelplineActionRow(
                            label = "Women Safety & Emergency Support",
                            number = city?.womenHelpline?.ifEmpty { "1091" } ?: "1091",
                            onDial = { dialNumber(context, city?.womenHelpline ?: "1091") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HelplineActionRow(
                            label = "CISF Metro Security Control Room",
                            number = "155655",
                            onDial = { dialNumber(context, "155655") }
                        )
                    }
                }
            }

            // Luggage Allowances
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Luggage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Luggage & Baggage Guidelines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        FaqPoint("Maximum Permissible Weight", "Up to 25 kg per passenger is allowed free of charge (Airport Express Line allows up to 30 kg).")
                        FaqPoint("Maximum Dimensions", "Baggage must not exceed 80 cm (length) × 50 cm (width) × 30 cm (height).")
                        FaqPoint("Security Scanning", "All baggage must pass through X-Ray scanners at the station entry gate.")
                    }
                }
            }

            // Penalty Tariffs
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFFC62828))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Key Metro Penalties & Fines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        FaqPoint("Traveling without valid Token / Card", "₹50 fine + maximum fare of the line system.")
                        FaqPoint("Eating or drinking inside train / platform", "₹500 fine and confiscation.")
                        FaqPoint("Unauthorized entry into Ladies Coach", "₹250 fine + removal from train.")
                        FaqPoint("Smoking inside station or coaches", "₹200 fine under COTPA act.")
                        FaqPoint("Carrying prohibited articles", "₹500 fine + criminal proceeding under Metro Railways Act.")
                    }
                }
            }

            // Prohibited Items
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Prohibited Items Inside Metro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "• Firecrackers, explosive substances, inflammable liquids (petrol, kerosene, thinner)\n" +
                                   "• Sharp blades, firearms, swords, knives longer than 4 inches\n" +
                                   "• Alcoholic beverages (except sealed bottles where state excise permits up to 2)\n" +
                                   "• Live pets or animals (except certified service dogs)\n" +
                                   "• Corrosive chemicals, dry ice, gas cylinders",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HelplineActionRow(label: String, number: String, onDial: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(number, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        FilledTonalButton(onClick = onDial) {
            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Call")
        }
    }
}

@Composable
fun FaqPoint(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "• $title", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun dialNumber(context: Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
