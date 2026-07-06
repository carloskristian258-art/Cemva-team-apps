package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CemvaViewModel
import com.example.data.EmergencyAlertEntity
import com.example.data.EmergencyReportEntity
import com.example.data.UserRole
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TacticalScreen(viewModel: CemvaViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userRole by viewModel.currentUserRole
    val userName by viewModel.currentUserEmail // Using email as name for now
    
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    val reports by viewModel.emergencyReports.collectAsStateWithLifecycle()
    val alerts by viewModel.emergencyAlerts.collectAsStateWithLifecycle()
    val membersList by viewModel.members.collectAsStateWithLifecycle()
    
    val lastLat by viewModel.lastKnownLatitude
    val lastLng by viewModel.lastKnownLongitude
    val isFetchingLocation by viewModel.isFetchingLocation
    
    var showSosConfirmation by remember { mutableStateOf(false) }
    var showAddAlertDialog by remember { mutableStateOf(false) }

    val cavite = LatLng(lastLat, lastLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(cavite, 13f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Live Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = locationPermissionState.status.isGranted),
            uiSettings = MapUiSettings(myLocationButtonEnabled = locationPermissionState.status.isGranted)
        ) {
            // Incident Markers
            reports.forEach { report ->
                Marker(
                    state = MarkerState(position = LatLng(report.latitude, report.longitude)),
                    title = report.type,
                    snippet = "${report.reporterName}: ${report.details}",
                    icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED)
                )
            }

            // Active Member Markers (Simulated near user for visualization if no real GPS)
            val activeMembers = membersList.filter { it.status == "Active" }
            for ((index, member) in activeMembers.withIndex()) {
                // Simulate some spread if they are all at the same default HQ location
                val offsetLat = (index % 5) * 0.002
                val offsetLng = (index / 5) * 0.002
                Marker(
                    state = MarkerState(position = LatLng(lastLat + offsetLat - 0.005, lastLng + offsetLng - 0.005)),
                    title = member.name,
                    snippet = "${member.position} • Online",
                    icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE)
                )
            }
        }

        // 2. SOS Button Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (userRole == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = { showAddAlertDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = "Broadcast Alert")
                }
            }

            LargeFloatingActionButton(
                onClick = { showSosConfirmation = true },
                containerColor = Color.Red,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Sos, contentDescription = "SOS", modifier = Modifier.size(32.dp))
                    Text("SOS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Alerts Feed Overlay (Top)
        if (alerts.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alerts.first().title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = alerts.first().message,
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = { viewModel.deleteEmergencyAlert(alerts.first()) }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
                    }
                }
            }
        }
    }

    // SOS Confirmation Dialog
    if (showSosConfirmation) {
        AlertDialog(
            onDismissRequest = { showSosConfirmation = false },
            title = { Text("Confirm Emergency SOS") },
            text = { Text("This will send your current GPS coordinates and an emergency request to all active personnel. Continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val loc = viewModel.getCurrentLocation()
                            val now = System.currentTimeMillis()
                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
                            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now))
                            
                            val report = EmergencyReportEntity(
                                reporterName = userName,
                                type = "SOS ALERT",
                                timestamp = now,
                                date = date,
                                time = time,
                                latitude = loc?.first ?: lastLat,
                                longitude = loc?.second ?: lastLng,
                                locationDescription = "GPS Verified Location",
                                details = "EMERGENCY SOS TRIGGERED VIA APP",
                                status = "Open"
                            )
                            viewModel.addEmergencyReport(report)
                            
                            // Also broadcast a local notification for feedback
                            NotificationHelper.showEmergencyNotification(
                                context, 
                                "SOS SENT", 
                                "Emergency report dispatched with GPS: ${loc?.first}, ${loc?.second}"
                            )
                            
                            showSosConfirmation = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("SEND SOS", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Alert Dialog (Admin Only)
    if (showAddAlertDialog) {
        var alertTitle by remember { mutableStateOf("") }
        var alertMsg by remember { mutableStateOf("") }
        var alertPriority by remember { mutableStateOf("Emergency") }

        AlertDialog(
            onDismissRequest = { showAddAlertDialog = false },
            title = { Text("Broadcast Tactical Alert") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = alertTitle,
                        onValueChange = { alertTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = alertMsg,
                        onValueChange = { alertMsg = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (alertTitle.isNotBlank() && alertMsg.isNotBlank()) {
                        val alert = EmergencyAlertEntity(
                            title = alertTitle,
                            message = alertMsg,
                            timestamp = System.currentTimeMillis(),
                            priority = alertPriority,
                            sender = userName
                        )
                        viewModel.addEmergencyAlert(alert)
                        showAddAlertDialog = false
                        
                        // Notify self for confirmation
                        NotificationHelper.showEmergencyNotification(context, "Alert Broadcasted", alertTitle)
                    }
                }) {
                    Text("Broadcast")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAlertDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
