package com.example.ui

import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontFamily
import com.example.ui.theme.SleekBlue
import com.example.ui.theme.SleekBlueDark
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.*

// CameraX, ML Kit and Permissions imports for QR Scanning
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CemvaApp(viewModel: CemvaViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentScreen by viewModel.currentScreen
    val userRole by viewModel.currentUserRole
    val userEmail by viewModel.currentUserEmail
    val currentUserId by viewModel.currentUserId

    val membersList by viewModel.members.collectAsStateWithLifecycle()
    val announcementsList by viewModel.announcements.collectAsStateWithLifecycle()
    val operationsList by viewModel.operations.collectAsStateWithLifecycle()
    val trainingsList by viewModel.trainings.collectAsStateWithLifecycle()
    val equipmentList by viewModel.equipment.collectAsStateWithLifecycle()
    val reportsList by viewModel.reports.collectAsStateWithLifecycle()
    val attendanceList by viewModel.attendance.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    // Dialog trigger states
    var showLoginDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAddOperationDialog by remember { mutableStateOf(false) }
    var showAddTrainingDialog by remember { mutableStateOf(false) }
    var showAddEquipmentDialog by remember { mutableStateOf(false) }
    var showAddAnnouncementDialog by remember { mutableStateOf(false) }
    var showAddReportDialog by remember { mutableStateOf(false) }
    var showAttendanceDialog by remember { mutableStateOf(false) }
    var showCameraScanner by remember { mutableStateOf(false) }

    // Secondary Screen State (for "More" sub-navigation)
    var currentSubScreen by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Sleek Logo Box
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "C",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            )
                        }
                        Column {
                            Text(
                                text = "CEMVA",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "VOLUNTEER MANAGEMENT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 8.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    val initials = when (userRole) {
                        UserRole.ADMIN -> "CK"
                        UserRole.MEMBER -> "JD"
                        UserRole.GUEST -> "G"
                    }

                    // Live Cloud Sync Indicator Badge
                    IconButton(
                        onClick = {
                            viewModel.currentScreen.value = "more"
                            currentSubScreen = "settings"
                        }
                    ) {
                        Icon(
                            imageVector = when (syncStatus) {
                                SyncStatus.SYNCING -> Icons.Default.Sync
                                SyncStatus.SUCCESS -> Icons.Default.CloudQueue
                                SyncStatus.ERROR -> Icons.Default.CloudOff
                                else -> Icons.Default.CloudQueue
                            },
                            contentDescription = "Cloud Status",
                            tint = when (syncStatus) {
                                SyncStatus.SYNCING -> Color.Blue
                                SyncStatus.SUCCESS -> Color(0xFF10B981) // Emerald Green
                                SyncStatus.ERROR -> Color.Red
                                else -> Color.Gray
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Sleek Profile Avatar Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                            .background(Color(0xFFE2E8F0))
                            .clickable { showLoginDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color(0xFF475569),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == "home",
                    onClick = {
                        viewModel.currentScreen.value = "home"
                        currentSubScreen = null
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = currentScreen == "members",
                    onClick = {
                        viewModel.currentScreen.value = "members"
                        currentSubScreen = null
                    },
                    icon = { Icon(Icons.Default.People, contentDescription = "Members") },
                    label = { Text("Members") },
                    modifier = Modifier.testTag("nav_members")
                )
                NavigationBarItem(
                    selected = currentScreen == "operations",
                    onClick = {
                        viewModel.currentScreen.value = "operations"
                        currentSubScreen = null
                    },
                    icon = { Icon(Icons.Default.LocalActivity, contentDescription = "Operations") },
                    label = { Text("Operations") },
                    modifier = Modifier.testTag("nav_operations")
                )
                NavigationBarItem(
                    selected = currentScreen == "trainings",
                    onClick = {
                        viewModel.currentScreen.value = "trainings"
                        currentSubScreen = null
                    },
                    icon = { Icon(Icons.Default.School, contentDescription = "Trainings") },
                    label = { Text("Trainings") },
                    modifier = Modifier.testTag("nav_trainings")
                )
                NavigationBarItem(
                    selected = currentScreen == "more",
                    onClick = {
                        viewModel.currentScreen.value = "more"
                    },
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                    label = { Text("More") },
                    modifier = Modifier.testTag("nav_more")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Screen Switcher
            AnimatedContent(
                targetState = if (currentSubScreen != null) "sub_$currentSubScreen" else currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { target ->
                when {
                    target.startsWith("sub_") -> {
                        val subName = target.substringAfter("sub_")
                        SubScreenContainer(
                            title = subName.replaceFirstChar { it.uppercase() },
                            onBack = { currentSubScreen = null }
                        ) {
                            when (subName) {
                                "documents" -> DocumentsSubScreen()
                                "equipment" -> EquipmentSubScreen(
                                    viewModel = viewModel,
                                    list = equipmentList,
                                    onAddClick = { showAddEquipmentDialog = true }
                                )
                                "gallery" -> GallerySubScreen()
                                "contacts" -> ContactsSubScreen(viewModel)
                                "reports_dashboard" -> ReportsDashboardSubScreen(
                                    membersList = membersList,
                                    operationsList = operationsList,
                                    trainingsList = trainingsList,
                                    reportsList = reportsList
                                )
                                "settings" -> SettingsSubScreen(viewModel)
                            }
                        }
                    }
                    target == "home" -> {
                        HomeScreen(
                            viewModel = viewModel,
                            announcements = announcementsList,
                            operations = operationsList,
                            onAttendanceClick = { showAttendanceDialog = true },
                            onReportClick = { showAddReportDialog = true },
                            onSopClick = { currentSubScreen = "documents" },
                            onIdClick = { viewModel.currentScreen.value = "members" },
                            onAddAnnouncement = { showAddAnnouncementDialog = true }
                        )
                    }
                    target == "members" -> {
                        MembersScreen(
                            viewModel = viewModel,
                            members = membersList,
                            onAddMemberClick = { showAddMemberDialog = true }
                        )
                    }
                    target == "operations" -> {
                        OperationsScreen(
                            viewModel = viewModel,
                            operations = operationsList,
                            reports = reportsList,
                            onAddOperationClick = { showAddOperationDialog = true },
                            onAddReportClick = { showAddReportDialog = true }
                        )
                    }
                    target == "trainings" -> {
                        TrainingsScreen(
                            viewModel = viewModel,
                            trainings = trainingsList,
                            onAddTrainingClick = { showAddTrainingDialog = true }
                        )
                    }
                    target == "more" -> {
                        MoreScreen(
                            userRole = userRole,
                            onSubScreenSelected = { currentSubScreen = it }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG IMPLEMENTATIONS ---

    // 1. Login & Signup/Register Tabbed Dialog
    if (showLoginDialog) {
        Dialog(onDismissRequest = { showLoginDialog = false }) {
            var activeTab by remember { mutableStateOf("login") } // "login" or "register"

            // Login States
            var loginEmail by remember { mutableStateOf("") }
            var loginPassword by remember { mutableStateOf("") }
            var isLoginPasswordVisible by remember { mutableStateOf(false) }

            // Register States
            var regName by remember { mutableStateOf("") }
            var regEmail by remember { mutableStateOf("") }
            var regPhone by remember { mutableStateOf("") }
            var regPassword by remember { mutableStateOf("") }
            var isRegPasswordVisible by remember { mutableStateOf(false) }
            var requestedRole by remember { mutableStateOf("GUEST") } // "GUEST", "MEMBER", "ADMIN"
            var isRoleDropdownExpanded by remember { mutableStateOf(false) }

            var showQuickBypass by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (activeTab == "login") Icons.Default.Lock else Icons.Default.PersonAdd,
                        contentDescription = "Auth",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = if (activeTab == "login") "Sign In to CEMVA" else "Register Volunteer Account",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    // Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeTab == "login") MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { activeTab = "login" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sign In",
                                color = if (activeTab == "login") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeTab == "register") MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { activeTab = "register" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Register",
                                color = if (activeTab == "register") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    if (activeTab == "login") {
                        // Email Field
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Password Field
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { isLoginPasswordVisible = !isLoginPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isLoginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (isLoginPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (loginEmail.isBlank() || loginPassword.isBlank()) {
                                    Toast.makeText(context, "Please fill in all credentials", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                coroutineScope.launch {
                                    val success = viewModel.login(loginEmail.trim(), loginPassword)
                                    if (success) {
                                        Toast.makeText(context, "Successfully authenticated!", Toast.LENGTH_SHORT).show()
                                        showLoginDialog = false
                                    } else {
                                        Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Authenticate & Sign In", fontWeight = FontWeight.Bold)
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Quick demo logins
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { showQuickBypass = !showQuickBypass }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (showQuickBypass) "Hide Demo Bypass Channels" else "Show Demo Bypass Channels",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Icon(
                                    imageVector = if (showQuickBypass) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (showQuickBypass) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.signInAs("carloskristian258@gmail.com", UserRole.ADMIN, "CEMVA-2026-001")
                                        showLoginDialog = false
                                        Toast.makeText(context, "Bypassed as Administrator", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bypass as Admin (Carlos K.)", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        viewModel.signInAs("maria.santos@cemva.org", UserRole.MEMBER, "CEMVA-2026-002")
                                        showLoginDialog = false
                                        Toast.makeText(context, "Bypassed as EMS Volunteer", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bypass as Member (Maria S.)", fontSize = 12.sp)
                                }
                            }
                        }

                    } else {
                        // Register Fields
                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text("Mobile Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Create Secure Password") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { isRegPasswordVisible = !isRegPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isRegPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (isRegPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Role Selection Box with simple simulated expandable dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = when (requestedRole) {
                                    "ADMIN" -> "Officer / Administrator"
                                    "MEMBER" -> "Active Duty Volunteer"
                                    else -> "Guest / Applicant"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Request Organization Role") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { isRoleDropdownExpanded = !isRoleDropdownExpanded }) {
                                        Icon(
                                            imageVector = if (isRoleDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            DropdownMenu(
                                expanded = isRoleDropdownExpanded,
                                onDismissRequest = { isRoleDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Guest / Applicant (Instant Access)") },
                                    onClick = {
                                        requestedRole = "GUEST"
                                        isRoleDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Active Duty Volunteer (Awaiting Admin Approval)") },
                                    onClick = {
                                        requestedRole = "MEMBER"
                                        isRoleDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Officer / Administrator (Awaiting Admin Approval)") },
                                    onClick = {
                                        requestedRole = "ADMIN"
                                        isRoleDropdownExpanded = false
                                    }
                                )
                            }
                        }

                        if (requestedRole != "GUEST") {
                            Text(
                                text = "⚠️ Admin & Volunteer accounts require approval from Carlos Kristian before accessing operational logs.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (regName.isBlank() || regEmail.isBlank() || regPhone.isBlank() || regPassword.isBlank()) {
                                    Toast.makeText(context, "Please populate all fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                coroutineScope.launch {
                                    val ok = viewModel.register(
                                        email = regEmail.trim(),
                                        password = regPassword,
                                        name = regName.trim(),
                                        phone = regPhone.trim(),
                                        requestedRole = requestedRole
                                    )
                                    if (ok) {
                                        Toast.makeText(context, "Account Registered! Logging in...", Toast.LENGTH_LONG).show()
                                        // Auto log in after register if Guest
                                        if (requestedRole == "GUEST") {
                                            viewModel.login(regEmail.trim(), regPassword)
                                            showLoginDialog = false
                                        } else {
                                            activeTab = "login"
                                            loginEmail = regEmail
                                            loginPassword = regPassword
                                        }
                                    } else {
                                        Toast.makeText(context, "Registration failed. Email might already exist.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create & Sync Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 2. Add/Edit Member Dialog
    if (showAddMemberDialog) {
        var mId by remember { mutableStateOf("CEMVA-2026-00" + (membersList.size + 1)) }
        var mName by remember { mutableStateOf("") }
        var mPosition by remember { mutableStateOf("EMT") }
        var mDepartment by remember { mutableStateOf("Emergency Response") }
        var mEmail by remember { mutableStateOf("") }
        var mPhone by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddMemberDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Register New Member",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mId,
                            onValueChange = { mId = it },
                            label = { Text("Member ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mName,
                            onValueChange = { mName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mPosition,
                            onValueChange = { mPosition = it },
                            label = { Text("Position (EMT, Medic, First Responder, Officer)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mDepartment,
                            onValueChange = { mDepartment = it },
                            label = { Text("Department") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mEmail,
                            onValueChange = { mEmail = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mPhone,
                            onValueChange = { mPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddMemberDialog = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (mName.isNotBlank()) {
                                        viewModel.addMember(
                                            MemberEntity(
                                                id = mId,
                                                name = mName,
                                                status = "Active",
                                                position = mPosition,
                                                department = mDepartment,
                                                email = mEmail,
                                                phone = mPhone,
                                                qrCodeToken = "M-CEMVA-${mName.take(2).uppercase()}${mId.takeLast(2)}",
                                                joinedDate = "2026-07-05"
                                            )
                                        )
                                        showAddMemberDialog = false
                                        Toast.makeText(context, "Member added successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Save Member")
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. Add Operation / Duty Roster Dialog
    if (showAddOperationDialog) {
        var opTitle by remember { mutableStateOf("") }
        var opType by remember { mutableStateOf("Deployment") }
        var opDate by remember { mutableStateOf("2026-07-05") }
        var opLocation by remember { mutableStateOf("") }
        var opDetails by remember { mutableStateOf("") }
        var opAssigned by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddOperationDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Add Operations / Duty Assignment",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = opTitle,
                            onValueChange = { opTitle = it },
                            label = { Text("Operation / Duty Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Deployment", "Event Standby", "Duty Roster").forEach { type ->
                                FilterChip(
                                    selected = opType == type,
                                    onClick = { opType = type },
                                    label = { Text(type, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = opDate,
                            onValueChange = { opDate = it },
                            label = { Text("Scheduled Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = opLocation,
                            onValueChange = { opLocation = it },
                            label = { Text("Location Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = opDetails,
                            onValueChange = { opDetails = it },
                            label = { Text("Operational Details / Guidelines") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = opAssigned,
                            onValueChange = { opAssigned = it },
                            label = { Text("Assigned Personnel (comma separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddOperationDialog = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (opTitle.isNotBlank()) {
                                        viewModel.addOperation(
                                            OperationEntity(
                                                title = opTitle,
                                                type = opType,
                                                date = opDate,
                                                location = opLocation,
                                                details = opDetails,
                                                assignedPersonnel = opAssigned,
                                                status = "Scheduled"
                                            )
                                        )
                                        showAddOperationDialog = false
                                        Toast.makeText(context, "Operations task scheduled", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Schedule")
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. Add Training Event Dialog
    if (showAddTrainingDialog) {
        var tTitle by remember { mutableStateOf("") }
        var tDate by remember { mutableStateOf("2026-07-12") }
        var tInstructor by remember { mutableStateOf("") }
        var tLocation by remember { mutableStateOf("") }
        var tCategory by remember { mutableStateOf("BLS") }
        var tMaterials by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddTrainingDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Schedule New Training Workshop",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = tTitle,
                            onValueChange = { tTitle = it },
                            label = { Text("Training Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = tInstructor,
                            onValueChange = { tInstructor = it },
                            label = { Text("Lead Instructor / Assessor") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = tLocation,
                            onValueChange = { tLocation = it },
                            label = { Text("Training Venue") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = tDate,
                            onValueChange = { tDate = it },
                            label = { Text("Date Scheduled") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = tMaterials,
                            onValueChange = { tMaterials = it },
                            label = { Text("Google Drive Materials Roster Link") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("Category", style = MaterialTheme.typography.labelMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            listOf("BLS", "ALS", "MFR", "ICS", "Ambulance Ops").forEach { cat ->
                                FilterChip(
                                    selected = tCategory == cat,
                                    onClick = { tCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddTrainingDialog = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (tTitle.isNotBlank()) {
                                        viewModel.addTraining(
                                            TrainingEntity(
                                                title = tTitle,
                                                date = tDate,
                                                instructor = tInstructor,
                                                location = tLocation,
                                                status = "Upcoming",
                                                materialsUrl = tMaterials.ifBlank { viewModel.googleUrls.value.googleDriveUrl },
                                                category = tCategory
                                            )
                                        )
                                        showAddTrainingDialog = false
                                        Toast.makeText(context, "Training course added", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Save Course")
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Add Equipment Dialog
    if (showAddEquipmentDialog) {
        var eqName by remember { mutableStateOf("") }
        var eqCategory by remember { mutableStateOf("Medical Kits") }
        var eqQty by remember { mutableStateOf("5") }

        Dialog(onDismissRequest = { showAddEquipmentDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Register Logistics Inventory Asset",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = eqName,
                        onValueChange = { eqName = it },
                        label = { Text("Asset / Equipment Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Category", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        listOf("Medical Kits", "Radios", "AED", "Stretchers", "Other").forEach { cat ->
                            FilterChip(
                                selected = eqCategory == cat,
                                onClick = { eqCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = eqQty,
                        onValueChange = { eqQty = it },
                        label = { Text("Initial Stock Quantity") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddEquipmentDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (eqName.isNotBlank()) {
                                    val qVal = eqQty.toIntOrNull() ?: 1
                                    viewModel.addEquipment(
                                        EquipmentEntity(
                                            name = eqName,
                                            category = eqCategory,
                                            quantity = qVal,
                                            status = "Available"
                                        )
                                    )
                                    showAddEquipmentDialog = false
                                    Toast.makeText(context, "Logistics record saved", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Add to Stock")
                        }
                    }
                }
            }
        }
    }

    // 6. Add Incident/PCR Report Dialog
    if (showAddReportDialog) {
        var repType by remember { mutableStateOf("Patient Care Report") }
        var repName by remember { mutableStateOf(membersList.find { it.id == currentUserId }?.name ?: "Unknown Responder") }
        var repLocation by remember { mutableStateOf("") }
        var repPatName by remember { mutableStateOf("") }
        var repPatAgeGend by remember { mutableStateOf("") }
        var repComplaint by remember { mutableStateOf("") }
        var repVitals by remember { mutableStateOf("BP: 120/80, HR: 80, RR: 16, SpO2: 98%") }
        var repInterventions by remember { mutableStateOf("") }
        var repNarrative by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddReportDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Submit Field Emergency Report",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Patient Care Report", "Incident Report").forEach { type ->
                                FilterChip(
                                    selected = repType == type,
                                    onClick = { repType = type },
                                    label = { Text(type, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = repName,
                            onValueChange = { repName = it },
                            label = { Text("Reporter / EMT Lead Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = repLocation,
                            onValueChange = { repLocation = it },
                            label = { Text("Incident Location / Site Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (repType == "Patient Care Report") {
                        item {
                            OutlinedTextField(
                                value = repPatName,
                                onValueChange = { repPatName = it },
                                label = { Text("Patient Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = repPatAgeGend,
                                onValueChange = { repPatAgeGend = it },
                                label = { Text("Patient Age / Gender") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = repComplaint,
                                onValueChange = { repComplaint = it },
                                label = { Text("Chief Complaint") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = repVitals,
                                onValueChange = { repVitals = it },
                                label = { Text("Vitals (BP, HR, RR, SpO2)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = repInterventions,
                                onValueChange = { repInterventions = it },
                                label = { Text("Medical Interventions Performed") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = repNarrative,
                            onValueChange = { repNarrative = it },
                            label = { Text("Incident Narrative / Event Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddReportDialog = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (repNarrative.isNotBlank() && repLocation.isNotBlank()) {
                                        viewModel.addReport(
                                            ReportEntity(
                                                reporterName = repName,
                                                type = repType,
                                                date = "2026-07-05",
                                                location = repLocation,
                                                patientName = repPatName,
                                                patientAgeGender = repPatAgeGend,
                                                chiefComplaint = repComplaint,
                                                vitals = repVitals,
                                                interventions = repInterventions,
                                                narrative = repNarrative
                                            )
                                        )
                                        showAddReportDialog = false
                                        Toast.makeText(context, "Emergency report saved securely", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Please write a narrative and location", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Submit Report")
                            }
                        }
                    }
                }
            }
        }
    }

    // 7. Add Announcement Dialog
    if (showAddAnnouncementDialog) {
        var aTitle by remember { mutableStateOf("") }
        var aContent by remember { mutableStateOf("") }
        var aPriority by remember { mutableStateOf("Normal") }

        Dialog(onDismissRequest = { showAddAnnouncementDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Broadcast New Announcement",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = aTitle,
                        onValueChange = { aTitle = it },
                        label = { Text("Announcement Headline") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = aContent,
                        onValueChange = { aContent = it },
                        label = { Text("Content / Action Instructions") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Priority:", style = MaterialTheme.typography.bodyMedium)
                        FilterChip(
                            selected = aPriority == "Normal",
                            onClick = { aPriority = "Normal" },
                            label = { Text("Normal") }
                        )
                        FilterChip(
                            selected = aPriority == "High",
                            onClick = { aPriority = "High" },
                            label = { Text("HIGH URGENCY") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddAnnouncementDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (aTitle.isNotBlank() && aContent.isNotBlank()) {
                                    viewModel.addAnnouncement(
                                        AnnouncementEntity(
                                            title = aTitle,
                                            content = aContent,
                                            date = "2026-07-05",
                                            author = membersList.find { it.id == currentUserId }?.name ?: "Operations Desk",
                                            priority = aPriority
                                        )
                                    )
                                    showAddAnnouncementDialog = false
                                    Toast.makeText(context, "Broadcast sent", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Broadcast")
                        }
                    }
                }
            }
        }
    }

    // 8. Attendance Logging (QR & Manual) Dialog
    if (showAttendanceDialog) {
        var attId by remember { mutableStateOf(currentUserId) }
        var attActivity by remember { mutableStateOf("Duty") }
        var attType by remember { mutableStateOf("Check-In") }

        Dialog(onDismissRequest = { showAttendanceDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Attendance Logging System",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = attId,
                        onValueChange = { attId = it },
                        label = { Text("Volunteer ID Number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Check-In", "Check-Out").forEach { type ->
                            FilterChip(
                                selected = attType == type,
                                onClick = { attType = type },
                                label = { Text(type, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text("Roster Activity", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Duty", "Training", "Deployment").forEach { act ->
                            FilterChip(
                                selected = attActivity == act,
                                onClick = { attActivity = act },
                                label = { Text(act) }
                            )
                        }
                    }

                    // Real Camera QR Code Scanner button
                    Button(
                        onClick = {
                            showAttendanceDialog = false
                            showCameraScanner = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Launch Camera QR Scanner")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan QR Code with Camera")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // QR Scanner Simulator
                        Button(
                            onClick = {
                                val matched = membersList.find { it.id == attId }
                                if (matched != null) {
                                    viewModel.recordAttendance(
                                        memberId = matched.id,
                                        memberName = matched.name,
                                        type = attType,
                                        activity = attActivity,
                                        method = "QR Code"
                                    )
                                    showAttendanceDialog = false
                                    Toast.makeText(context, "Attendance logged via simulated QR scanner: ${matched.name} (${attType})", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "No member matches ID: $attId", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate QR", fontSize = 11.sp)
                        }

                        // Manual Entry
                        Button(
                            onClick = {
                                val matched = membersList.find { it.id == attId }
                                if (matched != null) {
                                    viewModel.recordAttendance(
                                        memberId = matched.id,
                                        memberName = matched.name,
                                        type = attType,
                                        activity = attActivity,
                                        method = "Manual Entry"
                                    )
                                    showAttendanceDialog = false
                                    Toast.makeText(context, "Manual Attendance Saved: ${matched.name}", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No member matches ID: $attId", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manual Save", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (showCameraScanner) {
        Dialog(
            onDismissRequest = { showCameraScanner = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                QrCodeCameraScanner(
                    viewModel = viewModel,
                    membersList = membersList,
                    operationsList = operationsList,
                    onClose = { showCameraScanner = false }
                )
            }
        }
    }
}

// Sub-screen Back Container wrapper
@Composable
fun SubScreenContainer(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Divider()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            content()
        }
    }
}

// --- CORE NAVIGATION VIEWS ---

@Composable
fun HomeScreen(
    viewModel: CemvaViewModel,
    announcements: List<AnnouncementEntity>,
    operations: List<OperationEntity>,
    onAttendanceClick: () -> Unit,
    onReportClick: () -> Unit,
    onSopClick: () -> Unit,
    onIdClick: () -> Unit,
    onAddAnnouncement: () -> Unit
) {
    val context = LocalContext.current
    val userRole by viewModel.currentUserRole
    val membersList by viewModel.members.collectAsStateWithLifecycle()
    val weatherTemp = "29°C"
    val weatherCondition = "Scattered Thunderstorms"

    // Resolve current member details dynamically
    val currentMember = membersList.find { it.id == viewModel.currentUserId.value }
    val displayName = currentMember?.name ?: when (userRole) {
        UserRole.ADMIN -> "Vol. Carlos Kristian"
        UserRole.MEMBER -> "Vol. Juan Dela Cruz"
        UserRole.GUEST -> "Guest Volunteer"
    }
    val displayId = currentMember?.id ?: when (userRole) {
        UserRole.ADMIN -> "CEMVA-2026-001"
        UserRole.MEMBER -> "CEMVA-2024-0412"
        UserRole.GUEST -> "CEMVA-GUEST"
    }
    val isGuest = userRole == UserRole.GUEST

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sleek Welcome Hero Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SleekBlue, SleekBlueDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Good day,",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "MEMBER ID",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = displayId,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isGuest) "Standby/Guest" else "Active Duty",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Stats Row (3 Columns)
        item {
            val deploymentsCount = operations.size
            val activeMembersCount = membersList.filter { it.status == "Active" }.size
            val trainingsCount = viewModel.trainings.value.size

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Deployments Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Deployments",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = deploymentsCount.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Hours/Active Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Active Responders",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeMembersCount.coerceAtLeast(1).toString().padStart(2, '0'),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Patients/Trainings Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Trainings",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = trainingsCount.coerceAtLeast(4).toString().padStart(2, '0'),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Announcement Ticker
        item {
            val tickerText = if (announcements.isNotEmpty()) {
                announcements.first().title + ": " + announcements.first().content
            } else {
                "System standby: Active monitoring for Cavite EMS dispatch channels."
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val strokeWidth = 4.dp.toPx()
                            drawLine(
                                color = SleekBlue,
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(0f, size.height),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = tickerText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Actions Grid (Redesigned with beautiful Sleek styling)
        item {
            Column {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.QrCodeScanner,
                        label = "Log Attendance",
                        bgColor = Color.White,
                        textColor = MaterialTheme.colorScheme.primary,
                        onClick = onAttendanceClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Default.Assignment,
                        label = "Submit PCR",
                        bgColor = Color.White,
                        textColor = MaterialTheme.colorScheme.primary,
                        onClick = onReportClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.Book,
                        label = "SOP Manuals",
                        bgColor = Color.White,
                        textColor = MaterialTheme.colorScheme.primary,
                        onClick = onSopClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Default.QrCode,
                        label = "Digital ID",
                        bgColor = Color.White,
                        textColor = MaterialTheme.colorScheme.primary,
                        onClick = onIdClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Dynamic Weather & Base Alert
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Cavite Disaster Weather", style = MaterialTheme.typography.labelSmall)
                        Text("$weatherTemp - Imus HQ Area", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(weatherCondition, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "Weather",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Emergency Medical Contact Numbers Speed-dial
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "Phone Alert",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CEMVA Dispatch Emergency Lines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap below to immediately coordinate with EMS Dispatch and Cavite disaster channels.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EmergencyPhoneRow(
                            label = "Cavite Provincial Disaster RR (PDRRMO)",
                            number = "046-419-1234",
                            onDial = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0464191234"))
                                context.startActivity(intent)
                            }
                        )
                        EmergencyPhoneRow(
                            label = "Red Cross Cavite Chapter Hotline",
                            number = "046-416-9876",
                            onDial = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0464169876"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }

        // Broadcasters & Announcements
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Broadcast Announcements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (userRole == UserRole.ADMIN) {
                    IconButton(onClick = onAddAnnouncement) {
                        Icon(Icons.Default.Add, contentDescription = "Broadcast Alert", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (announcements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No announcements broadcasted yet.", color = Color.Gray)
                }
            }
        } else {
            items(announcements) { ann ->
                val isHigh = ann.priority == "High"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isHigh) MaterialTheme.colorScheme.error else Color.Transparent
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHigh) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isHigh) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = "Urgent", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = ann.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHigh) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = ann.date,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ann.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "By: ${ann.author}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (userRole == UserRole.ADMIN) {
                                Text(
                                    text = "Delete",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.clickable { viewModel.deleteAnnouncement(ann) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = modifier
            .height(115.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = label, 
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmergencyPhoneRow(
    label: String,
    number: String,
    onDial: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.4f))
            .clickable(onClick = onDial)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Text(number, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
        Icon(Icons.Default.Call, contentDescription = "Dial", tint = Color.Red)
    }
}

// --- MEMBERS TAB VIEW ---
@Composable
fun MembersScreen(
    viewModel: CemvaViewModel,
    members: List<MemberEntity>,
    onAddMemberClick: () -> Unit
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf("directory") }
    var searchQuery by viewModel.memberSearchQuery
    val selectedMemberId by viewModel.selectedMemberId
    val userRole by viewModel.currentUserRole

    val filteredMembers = members.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.id.contains(searchQuery, ignoreCase = true) ||
        it.position.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Headers
        TabRow(
            selectedTabIndex = when (activeSubTab) {
                "directory" -> 0
                "recruitment" -> 1
                "digital_id" -> 2
                else -> 0
            }
        ) {
            Tab(
                selected = activeSubTab == "directory",
                onClick = { activeSubTab = "directory" },
                text = { Text("Directory", fontSize = 12.sp) }
            )
            Tab(
                selected = activeSubTab == "recruitment",
                onClick = { activeSubTab = "recruitment" },
                text = { Text("Recruitment", fontSize = 12.sp) }
            )
            Tab(
                selected = activeSubTab == "digital_id",
                onClick = { activeSubTab = "digital_id" },
                text = { Text("Digital ID", fontSize = 12.sp) }
            )
        }

        when (activeSubTab) {
            "directory" -> {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search members, roles...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        )
                        if (userRole == UserRole.ADMIN) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onAddMemberClick,
                                modifier = Modifier.height(56.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Member")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (filteredMembers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No members found.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredMembers) { m ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectedMemberId.value = m.id
                                            activeSubTab = "digital_id"
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Avatar holder
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = m.name.take(2).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                            Column {
                                                Text(m.name, fontWeight = FontWeight.Bold)
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = m.position,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                    Text("•", style = MaterialTheme.typography.labelSmall)
                                                    Text(
                                                        text = m.id,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.Gray
                                                    )
                                                }
                                                Text(m.department, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${m.phone}"))
                                                    context.startActivity(intent)
                                                }
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            if (userRole == UserRole.ADMIN && m.id != "CEMVA-2026-001") {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteMember(m)
                                                        Toast.makeText(context, "Deleted ${m.name}", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "recruitment" -> {
                RecruitmentView(viewModel)
            }
            "digital_id" -> {
                val currentM = members.find { it.id == selectedMemberId } ?: members.firstOrNull()
                DigitalIdView(viewModel, currentM)
            }
        }
    }
}

@Composable
fun RecruitmentView(viewModel: CemvaViewModel) {
    val context = LocalContext.current
    val urls by viewModel.googleUrls

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Icon(
                Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(64.dp)
            )
        }
        item {
            Text(
                "Become a CEMVA First Responder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                "Cavite Emergency Medical Volunteers Association recruits dedicated citizens who want to serve Cavite Province. Receive certified training in Basic Life Support, Ambulance Operations, Trauma management, and Disaster Risk Response.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recruitment Roster Steps", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "1. Submit applicant details via our official Google Forms.",
                        "2. Attend the basic orientation at Imus HQ.",
                        "3. Pass physical stamina and basic first-aid assessment.",
                        "4. Get assigned to a squad & receive digital member ID."
                    ).forEach { step ->
                        Text(step, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urls.googleFormUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Google Recruitment Form")
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urls.googleSitesUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Language, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Visit CEMVA Google Site")
            }
        }
    }
}

@Composable
fun DigitalIdView(viewModel: CemvaViewModel, m: MemberEntity?) {
    val context = LocalContext.current
    val userRole by viewModel.currentUserRole
    val verificationResult by viewModel.qrVerificationResult
    var inputToken by remember { mutableStateOf("") }

    if (m == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select a member first to display Digital ID.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Interactive Digital ID Card Design
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.63f), // Standard ID Card proportion
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Accent dynamic wave in background
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFD32F2F),
                            radius = size.width * 0.4f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, 0f)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = Color(0xFF152A4A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text("CEMVA Emergency Volunteers", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                Text("Cavite Chapter", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Photo Avatar
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(2.dp, Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = m.status.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (m.status == "Active") Color(0xFF2E7D32) else Color.Red
                                )
                            }
                        }

                        // Name & Role details
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = m.name.uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = m.position,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "DEPT: ${m.department}",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        // QR Verification Pattern
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(90.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                // Draw an attractive abstract QR-code grid simulator in Canvas
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val blockCount = 8
                                    val blockSize = size.width / blockCount
                                    for (i in 0 until blockCount) {
                                        for (j in 0 until blockCount) {
                                            // Make patterns based on simple hashes of member id
                                            val hash = (m.id.hashCode() + i * j * 37) % 7
                                            if (hash == 0 || (i in listOf(0,1,6,7) && j in listOf(0,1,6,7))) {
                                                drawRect(
                                                    color = Color.Black,
                                                    topLeft = androidx.compose.ui.geometry.Offset(i * blockSize, j * blockSize),
                                                    size = androidx.compose.ui.geometry.Size(blockSize, blockSize)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ID Code Footer
                        Text(
                            text = m.id,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Action info for member
        item {
            Text(
                "This card serves as your digital volunteer ID. Present it at disaster incident check-ins for scanner attendance logging.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        // Admin verification tool
        if (userRole == UserRole.ADMIN) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Admin QR Verification Check", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = inputToken,
                                onValueChange = { inputToken = it },
                                placeholder = { Text("Enter QR Token to verify") },
                                label = { Text("QR Token ID") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.verifyQrToken(inputToken)
                                }
                            ) {
                                Text("Verify")
                            }
                        }

                        if (verificationResult != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (verificationResult!!.status == "Active") Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (verificationResult!!.status == "Active") Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (verificationResult!!.status == "Active") Color(0xFF2E7D32) else Color.Red
                                )
                                Column {
                                    Text(
                                        text = "Matched: ${verificationResult!!.name}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Status: ${verificationResult!!.status} (${verificationResult!!.position})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- OPERATIONS TAB VIEW ---
@Composable
fun OperationsScreen(
    viewModel: CemvaViewModel,
    operations: List<OperationEntity>,
    reports: List<ReportEntity>,
    onAddOperationClick: () -> Unit,
    onAddReportClick: () -> Unit
) {
    var opsTab by remember { mutableStateOf("calendar") }
    val userRole by viewModel.currentUserRole

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = when (opsTab) {
                "calendar" -> 0
                "duty_roster" -> 1
                "reports" -> 2
                else -> 0
            }
        ) {
            Tab(selected = opsTab == "calendar", onClick = { opsTab = "calendar" }, text = { Text("Deployments") })
            Tab(selected = opsTab == "duty_roster", onClick = { opsTab = "duty_roster" }, text = { Text("Roster") })
            Tab(selected = opsTab == "reports", onClick = { opsTab = "reports" }, text = { Text("Reports") })
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
        ) {
            when (opsTab) {
                "calendar" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Deployments & Event Coverage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (userRole == UserRole.ADMIN) {
                                Button(onClick = onAddOperationClick) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Ops")
                                }
                            }
                        }

                        val activeOps = operations.filter { it.type != "Duty Roster" }
                        if (activeOps.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("No active field deployments.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(activeOps) { op ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                when (op.status) {
                                                                    "Active" -> Color.Red
                                                                    "Scheduled" -> Color.Blue
                                                                    else -> Color.Gray
                                                                }
                                                            )
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(op.type.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                                }
                                                Text(op.date, style = MaterialTheme.typography.labelSmall)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(op.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Location: ${op.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(op.details, style = MaterialTheme.typography.bodyMedium)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Assigned Responders: ${op.assignedPersonnel}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "duty_roster" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Base Duty Roster", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (userRole == UserRole.ADMIN) {
                                Button(onClick = onAddOperationClick) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Roster")
                                }
                            }
                        }

                        val rosters = operations.filter { it.type == "Duty Roster" }
                        if (rosters.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("Roster is clear today.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(rosters) { r ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(r.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text("HQ Venue: ${r.location}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Shift Personnel: ${r.assignedPersonnel}", style = MaterialTheme.typography.bodyMedium)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("ACTIVE", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "reports" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("MFR Field Emergency Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(onClick = onAddReportClick) {
                                Icon(Icons.Default.Add, contentDescription = "Add Report")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Report")
                            }
                        }

                        if (reports.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("No incident reports submitted.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(reports) { rep ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(rep.type.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                                Text(rep.date, style = MaterialTheme.typography.labelSmall)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Site: ${rep.location}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            if (rep.type == "Patient Care Report") {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Patient: ${rep.patientName} (${rep.patientAgeGender})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                Text("Chief Complaint: ${rep.chiefComplaint}", style = MaterialTheme.typography.bodySmall)
                                                Text("Vitals Checked: ${rep.vitals}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                                Text("Interventions: ${rep.interventions}", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(rep.narrative, style = MaterialTheme.typography.bodyMedium)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("By: ${rep.reporterName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                                if (userRole == UserRole.ADMIN) {
                                                    Text(
                                                        "Delete",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.clickable { viewModel.deleteReport(rep) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TRAININGS TAB VIEW ---
@Composable
fun TrainingsScreen(
    viewModel: CemvaViewModel,
    trainings: List<TrainingEntity>,
    onAddTrainingClick: () -> Unit
) {
    val context = LocalContext.current
    var trainTab by remember { mutableStateOf("calendar") }
    val userRole by viewModel.currentUserRole
    val skillsChecklist by viewModel.userSkillsChecklist

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = when (trainTab) {
                "calendar" -> 0
                "materials" -> 1
                "skills" -> 2
                else -> 0
            }
        ) {
            Tab(selected = trainTab == "calendar", onClick = { trainTab = "calendar" }, text = { Text("Calendar") })
            Tab(selected = trainTab == "materials", onClick = { trainTab = "materials" }, text = { Text("Materials") })
            Tab(selected = trainTab == "skills", onClick = { trainTab = "skills" }, text = { Text("Skills Audit") })
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
        ) {
            when (trainTab) {
                "calendar" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Training Calendar & Certifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (userRole == UserRole.ADMIN) {
                                Button(onClick = onAddTrainingClick) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Training")
                                }
                            }
                        }

                        if (trainings.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("No training sessions scheduled.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(trainings) { t ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(t.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                                }
                                                Text(t.date, style = MaterialTheme.typography.labelSmall)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(t.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text("Instructor: ${t.instructor}", style = MaterialTheme.typography.bodySmall)
                                            Text("Venue: ${t.location}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(t.materialsUrl))
                                                        context.startActivity(intent)
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Get Materials", fontSize = 11.sp)
                                                }

                                                if (userRole == UserRole.ADMIN) {
                                                    Text(
                                                        "Delete",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.clickable { viewModel.deleteTraining(t) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "materials" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Text("Google Drive Training Manuals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Full offline access available once documents are downloaded locally.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(listOf(
                            "Basic Life Support (BLS) AHA Guideline Roster",
                            "Emergency Vehicle and Ambulance Ops Handbook",
                            "Mass Casualty Triage and ICS Checklist",
                            "Pre-Hospital Trauma Care Field Manual v3"
                        )) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.googleUrls.value.googleDriveUrl))
                                        context.startActivity(intent)
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                        Column {
                                            Text(item, fontWeight = FontWeight.Bold)
                                            Text("Sourced from CEMVA Google Drive", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }
                                    Icon(Icons.Default.CloudDownload, contentDescription = "Download File", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                "skills" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Text("Volunteer EMS Skills Audit Checklist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Check off physical competencies verified by training instructors.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(skillsChecklist.toList()) { (skill, done) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = skill,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Checkbox(
                                        checked = done,
                                        onCheckedChange = { viewModel.toggleUserSkill(skill) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- MORE TAB VIEW ---
@Composable
fun MoreScreen(
    userRole: UserRole,
    onSubScreenSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("CEMVA Logistics & Operations Roster", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            MoreMenuRow(
                icon = Icons.Default.Handyman,
                title = "Equipment & Medical Assets",
                subtitle = "AED, medical kits, and radios logs",
                onClick = { onSubScreenSelected("equipment") }
            )
        }
        item {
            MoreMenuRow(
                icon = Icons.Default.Description,
                title = "Documents & Constitution SOP",
                subtitle = "SOP guidelines, constitutions, by-laws",
                onClick = { onSubScreenSelected("documents") }
            )
        }
        item {
            MoreMenuRow(
                icon = Icons.Default.PhotoLibrary,
                title = "Photo & Outreach Gallery",
                subtitle = "CEMVA volunteer community work",
                onClick = { onSubScreenSelected("gallery") }
            )
        }
        item {
            MoreMenuRow(
                icon = Icons.Default.Phone,
                title = "Social Channels & Contact Us",
                subtitle = "Messenger, Facebook, email and locations",
                onClick = { onSubScreenSelected("contacts") }
            )
        }
        item {
            MoreMenuRow(
                icon = Icons.Default.BarChart,
                title = "Volunteer Activity Reports",
                subtitle = "Active metrics, deployments, volunteer hours",
                onClick = { onSubScreenSelected("reports_dashboard") }
            )
        }
        item {
            MoreMenuRow(
                icon = Icons.Default.Settings,
                title = "Google Services Customization",
                subtitle = "Configure Google Form, Sheet, Drive, Calendar URLs",
                onClick = { onSubScreenSelected("settings") }
            )
        }
    }
}

@Composable
fun MoreMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

// --- SUB-SCREEN CONTENT ---

// 1. Documents (SOP, By-Laws) Sub-screen
@Composable
fun DocumentsSubScreen() {
    var selectedDocKey by remember { mutableStateOf<String?>(null) }

    val docContents = mapOf(
        "sop_manual" to """
            CEMVA EMERGENCY SERVICES STANDING OPERATING PROCEDURES (SOP)
            
            1. EMS ACTIVATION METHODOLOGY:
            - Initial receipt of medical emergency alert via provincial 911 dispatch.
            - Mobile unit response must roll within exactly 3 minutes of base tone out.
            
            2. PROTOCOLS ON SCENE FIRST-AID:
            - Scene Safety is paramount. Assess road hazards, structures, and fire risk before approach.
            - Wear required Personal Protective Equipment (PPE) including surgical gloves, face shields, and high-visibility EMT rescue vests.
            - Primary Survey: Assess Airway, Breathing, and Circulation (ABCs) with urgent cervical spine stabilization in trauma skids.
            
            3. AMBULANCE PATIENT CONVEYANCE:
            - Responders must obtain written, signed medical refusal form if a conscious patient refuses transport.
            - Maintain continuous radio updates with incoming hospital ER staff over VHF channel 154.51 MHz.
        """.trimIndent(),
        "constitution" to """
            CAVITE EMERGENCY MEDICAL VOLUNTEERS ASSOCIATION (CEMVA) CONSTITUTION
            
            ARTICLE I - FOUNDING PRINCIPLES:
            Section 1: The official name of this non-profit humanitarian body is the Cavite Emergency Medical Volunteers Association (CEMVA).
            Section 2: The organizational motto is "Laging Handa, Laging Lingkod" representing prompt, expert, and selfless emergency medical assistance to the people of Cavite.
            
            ARTICLE II - ORGANIZATION STRUCTURE:
            - The association consists of an Executive Board, active EMT Responders, logistics officers, and recruits.
            - Administrative decisions regarding budget, asset purchase, and training courses require a majority vote of officers.
        """.trimIndent(),
        "bylaws" to """
            CEMVA OPERATIONAL BY-LAWS & DISCIPLINARY PROTOCOLS
            
            1. ACTIVE DUTY ROSTER EXPECTATIONS:
            - All registered active members must render a minimum of 24 volunteer duty-shift hours every calendar month.
            - Unauthorized absences from a scheduled disaster deployment will result in credential suspension.
            
            2. CODE OF CONDUCT ON PATIENTS:
            - Responders will treat every patient with dignity, regardless of race, age, nationality, or socio-economic background.
            - Protecting medical records, patient vitals, and injury identity is mandatory. Non-disclosure complies with Data Privacy regulations.
        """.trimIndent()
    )

    if (selectedDocKey != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDocKey!!.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = { selectedDocKey = null }) {
                    Text("Close Document")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Text(
                    text = docContents[selectedDocKey] ?: "Document empty",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "CEMVA Constitution, SOP, and Legal Handbooks. Downloaded locally for seamless, network-free reading in critical fields.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            DocRow(title = "CEMVA SOP Ambulance Manual v2.1", subtitle = "Tactical first-responder checklists & trauma triage", onClick = { selectedDocKey = "sop_manual" })
        }
        item {
            DocRow(title = "Association Constitution", subtitle = "Founding articles of volunteerism & board charters", onClick = { selectedDocKey = "constitution" })
        }
        item {
            DocRow(title = "Volunteer Operational By-Laws", subtitle = "Duty shift hour minimums & code of ethics", onClick = { selectedDocKey = "bylaws" })
        }
    }
}

@Composable
fun DocRow(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = "Read", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

// 2. Equipment Inventory Logistics Sub-screen
@Composable
fun EquipmentSubScreen(
    viewModel: CemvaViewModel,
    list: List<EquipmentEntity>,
    onAddClick: () -> Unit
) {
    val userRole by viewModel.currentUserRole
    val membersList by viewModel.members.collectAsStateWithLifecycle()
    val adminName = membersList.find { it.id == viewModel.currentUserId.value }?.name ?: "Rescue Officer"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CEMVA Logistics & Equipment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (userRole == UserRole.ADMIN) {
                Button(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Logistics")
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (list.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Logistics list is currently empty.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(list) { eq ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(eq.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (eq.quantity > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (eq.quantity > 0) "IN STOCK: ${eq.quantity}" else "CHECKED OUT",
                                        fontSize = 11.sp,
                                        color = if (eq.quantity > 0) Color(0xFF2E7D32) else Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Category: ${eq.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                            if (eq.lastCheckedOutBy.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Last Handed To: ${eq.lastCheckedOutBy} (${eq.checkoutDate})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (eq.quantity > 0) {
                                    OutlinedButton(
                                        onClick = { viewModel.checkOutEquipment(eq, adminName) }
                                    ) {
                                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Check Out Asset", fontSize = 11.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.checkInEquipment(eq) }
                                    ) {
                                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Check In Return", fontSize = 11.sp)
                                    }
                                }
                                if (userRole == UserRole.ADMIN) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteEquipment(eq) }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Item", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. Gallery Sub-screen
@Composable
fun GallerySubScreen() {
    val items = listOf(
        "MFR Flood Evacuation" to "Active responder deployments in Cavite monsoons rescuing elderly residents.",
        "AHA BLS Certified Training" to "Our paramedics mastering chest compression depth on simulator mannequins.",
        "General Assembly Assembly" to "Volunteers coming together at Imus central HQ for skills checks.",
        "Typhoon Relief Distribution" to "Delivering life support packages and water purification systems to families."
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { (title, desc) ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // Stylized canvas box representing photo placeholders dynamically
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("CEMVA SCENE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(desc, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = Color.Gray, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

// 4. Contacts Sub-screen (Google Maps, Facebook, Messenger integration)
@Composable
fun ContactsSubScreen(viewModel: CemvaViewModel) {
    val context = LocalContext.current
    val urls by viewModel.googleUrls

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Text("Get in Touch with CEMVA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Contact our Cavite EMS volunteer board for standby emergency coverages, donation support, and recruitment.", textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(8.dp))

        // Facebook Card
        ContactChannelCard(
            title = "Official Facebook Page",
            value = "fb.com/caviteemergencyvolunteers",
            icon = Icons.Default.Language,
            color = Color(0xFF1877F2),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com"))
                context.startActivity(intent)
            }
        )

        // Email Card
        ContactChannelCard(
            title = "Gmail Communications",
            value = urls.gmailAddress,
            icon = Icons.Default.Email,
            color = MaterialTheme.colorScheme.secondary,
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${urls.gmailAddress}"))
                context.startActivity(intent)
            }
        )

        // Google Maps Card
        ContactChannelCard(
            title = "Google Maps Location (Base HQ)",
            value = "Cavite, Philippines",
            icon = Icons.Default.Map,
            color = Color(0xFF34A853),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urls.googleMapsUrl))
                context.startActivity(intent)
            }
        )
    }
}

@Composable
fun ContactChannelCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color)
            }
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

// 5. Statistics & Volunteer Activity Reports Sub-screen
@Composable
fun ReportsDashboardSubScreen(
    membersList: List<MemberEntity>,
    operationsList: List<OperationEntity>,
    trainingsList: List<TrainingEntity>,
    reportsList: List<ReportEntity>
) {
    val totalMembers = membersList.size
    val activeMembers = membersList.count { it.status == "Active" }
    val totalDeployments = operationsList.count { it.type == "Deployment" }
    val totalEventsCovered = operationsList.count { it.type == "Event Standby" }
    val totalTrainings = trainingsList.size
    val patientsAssisted = reportsList.count { it.type == "Patient Care Report" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "CEMVA Organizational Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Real-time aggregated stats of disaster assistance conducted.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Stats Card Grid
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatPanelCard(label = "Total Members", count = totalMembers.toString(), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                StatPanelCard(label = "Active Responders", count = activeMembers.toString(), tint = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatPanelCard(label = "Disaster Deployments", count = totalDeployments.toString(), tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                StatPanelCard(label = "Patients Assisted", count = patientsAssisted.toString(), tint = Color.Red, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatPanelCard(label = "Trainings Certified", count = totalTrainings.toString(), tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
                StatPanelCard(label = "Events Standby", count = totalEventsCovered.toString(), tint = Color(0xFFEF6C00), modifier = Modifier.weight(1f))
            }
        }

        // Volunteer Hour summary card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Aggregated Volunteer Duty Hours", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total Duty Hours Tracked: 1,482 Hours", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Includes monsoons, marathons standbys, and certified skill refreshers.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
fun StatPanelCard(
    label: String,
    count: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(count, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = tint)
        }
    }
}

// 6. Settings Sub-screen (Google integration edits)
@Composable
fun SettingsSubScreen(viewModel: CemvaViewModel) {
    val urls by viewModel.googleUrls
    val userRole by viewModel.currentUserRole

    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val syncLogs by viewModel.syncLogs.collectAsStateWithLifecycle()
    val firebaseDbUrl by viewModel.firebaseDbUrl.collectAsStateWithLifecycle()
    val firebaseWebApiKey by viewModel.firebaseWebApiKey.collectAsStateWithLifecycle()
    val isAutoSyncEnabled by viewModel.isAutoSyncEnabled.collectAsStateWithLifecycle()

    var formUrl by remember { mutableStateOf(urls.googleFormUrl) }
    var sheetUrl by remember { mutableStateOf(urls.googleSheetsUrl) }
    var driveUrl by remember { mutableStateOf(urls.googleDriveUrl) }
    var calUrl by remember { mutableStateOf(urls.googleCalendarUrl) }
    var mapUrl by remember { mutableStateOf(urls.googleMapsUrl) }
    var emailAddr by remember { mutableStateOf(urls.gmailAddress) }
    var siteUrl by remember { mutableStateOf(urls.googleSitesUrl) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Google Services Sync Config",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Keep this app integrated with your active Google forms & spreadsheets without rewriting Kotlin code.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (userRole != UserRole.ADMIN) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Viewing Only. You must sign in as Administrator to edit integration endpoints.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = formUrl,
                onValueChange = { if (userRole == UserRole.ADMIN) formUrl = it },
                label = { Text("Recruitment Google Form Link") },
                readOnly = userRole != UserRole.ADMIN,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = sheetUrl,
                onValueChange = { if (userRole == UserRole.ADMIN) sheetUrl = it },
                label = { Text("Active Members Google Sheet Link") },
                readOnly = userRole != UserRole.ADMIN,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = driveUrl,
                onValueChange = { if (userRole == UserRole.ADMIN) driveUrl = it },
                label = { Text("Logistics & SOP Google Drive Directory") },
                readOnly = userRole != UserRole.ADMIN,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = calUrl,
                onValueChange = { if (userRole == UserRole.ADMIN) calUrl = it },
                label = { Text("Operational Schedule Google Calendar Link") },
                readOnly = userRole != UserRole.ADMIN,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = mapUrl,
                onValueChange = { if (userRole == UserRole.ADMIN) mapUrl = it },
                label = { Text("HQ Location Google Maps Link") },
                readOnly = userRole != UserRole.ADMIN,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = emailAddr,
                onValueChange = { if (userRole == UserRole.ADMIN) emailAddr = it },
                label = { Text("Official Contact Gmail Address") },
                readOnly = userRole != UserRole.ADMIN,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = siteUrl,
                onValueChange = { if (userRole == UserRole.ADMIN) siteUrl = it },
                label = { Text("Organizational Google Site URL") },
                readOnly = userRole != UserRole.ADMIN,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (userRole == UserRole.ADMIN) {
            item {
                Button(
                    onClick = {
                        viewModel.updateGoogleUrls(
                            GoogleIntegrationUrls(
                                googleFormUrl = formUrl,
                                googleSheetsUrl = sheetUrl,
                                googleDriveUrl = driveUrl,
                                googleCalendarUrl = calUrl,
                                googleMapsUrl = mapUrl,
                                gmailAddress = emailAddr,
                                googleSitesUrl = siteUrl
                            )
                        )
                        Toast.makeText(viewModel.getApplication(), "Google Workspace Services successfully synced", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save integration")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Sync Integrations")
                }
            }
        }

        // --- REAL TIME CLOUD SYNC CONFIGURATION ---
        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                "Google Firebase Real-Time Synchronization",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Provide real-time data sync with Google Firebase so multiple active devices instantly share member records, operational schedules, equipment check-outs, and PCR logs.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Sync status header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (syncStatus) {
                                            SyncStatus.SYNCING -> Color.Blue
                                            SyncStatus.SUCCESS -> Color.Green
                                            SyncStatus.ERROR -> Color.Red
                                            else -> Color.Gray
                                        }
                                    )
                            )
                            Text(
                                text = when (syncStatus) {
                                    SyncStatus.SYNCING -> "Syncing Cloud..."
                                    SyncStatus.SUCCESS -> "Online / Synced"
                                    SyncStatus.ERROR -> "Offline / Sync Error"
                                    else -> "Ready to Sync"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = lastSyncTime?.let { "Last Synced: ${it.substringAfter(" ")}" } ?: "Never Synced",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    // Firebase URL Field
                    var dbUrl by remember { mutableStateOf(firebaseDbUrl) }
                    OutlinedTextField(
                        value = dbUrl,
                        onValueChange = {
                            if (userRole == UserRole.ADMIN) {
                                dbUrl = it
                                viewModel.updateFirebaseUrl(it)
                            }
                        },
                        label = { Text("Firebase Realtime Database REST API URL") },
                        placeholder = { Text("https://your-project.firebaseio.com/") },
                        readOnly = userRole != UserRole.ADMIN,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Firebase Web API Key Field
                    var apiKey by remember { mutableStateOf(firebaseWebApiKey) }
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = {
                            if (userRole == UserRole.ADMIN) {
                                apiKey = it
                                viewModel.updateWebApiKey(it)
                            }
                        },
                        label = { Text("Firebase Auth Web API Key") },
                        placeholder = { Text("AIzaSy...") },
                        readOnly = userRole != UserRole.ADMIN,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Auto-sync toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text("Auto-Sync on Mutation", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Instantly upload new reports, check-ins, or equipment changes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = isAutoSyncEnabled,
                            onCheckedChange = { if (userRole == UserRole.ADMIN) viewModel.toggleAutoSync(it) },
                            enabled = userRole == UserRole.ADMIN
                        )
                    }

                    // Sync buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerSync()
                                Toast.makeText(viewModel.getApplication(), "Triggered cloud synchronization", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Sync now")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync Now")
                        }
                    }

                    // Sync logs expander
                    var showLogs by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLogs = !showLogs }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Real-Time Synchronization Logs (${syncLogs.size})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Icon(
                                imageVector = if (showLogs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (showLogs) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(top = 4.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (syncLogs.isEmpty()) {
                                        item {
                                            Text("No synchronization logs captured yet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(8.dp))
                                        }
                                    } else {
                                        items(syncLogs) { log ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(log.timestamp, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                Text(
                                                    text = log.message,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (log.isError) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrCodeCameraScanner(
    viewModel: CemvaViewModel,
    membersList: List<MemberEntity>,
    operationsList: List<OperationEntity>,
    onClose: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (cameraPermissionState.status.isGranted) {
        CameraScannerView(
            viewModel = viewModel,
            membersList = membersList,
            operationsList = operationsList,
            onClose = onClose
        )
    } else {
        CameraPermissionRequestView(
            onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
            onClose = onClose
        )
    }
}

@Composable
fun CameraPermissionRequestView(
    onRequestPermission: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "CEMVA needs access to your device camera to scan member digital ID QR codes and automatically validate their attendance against active duty rosters.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Camera Permission")
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go Back")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScannerView(
    viewModel: CemvaViewModel,
    membersList: List<MemberEntity>,
    operationsList: List<OperationEntity>,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Filter operations to show duty rosters or deployments
    val activeDutyRosters = remember(operationsList) {
        operationsList.filter { it.type == "Duty Roster" || it.type == "Deployment" || it.type == "Event Standby" }
            .ifEmpty { operationsList }
    }

    var selectedOperation by remember {
        mutableStateOf(activeDutyRosters.find { it.status == "Active" } ?: activeDutyRosters.firstOrNull())
    }

    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Scan result states
    var scannedResult by remember { mutableStateOf<String?>(null) }
    var scannedMember by remember { mutableStateOf<MemberEntity?>(null) }
    var isValidated by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }
    var showValidationCard by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Logging type states
    var attType by remember { mutableStateOf("Check-In") }
    var attActivity by remember { mutableStateOf("Duty") }

    val cameraExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }

    // Infinite scanning animation
    val infiniteTransition = rememberInfiniteTransition(label = "LaserTransition")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserAnimation"
    )

    fun handleQrScan(rawValue: String) {
        val matchedMember = membersList.find { it.qrCodeToken == rawValue || it.id == rawValue }
        scannedResult = rawValue
        if (matchedMember != null) {
            scannedMember = matchedMember
            val op = selectedOperation
            if (op != null) {
                // Validate if assigned to active duty roster
                val isAssigned = op.assignedPersonnel.contains(matchedMember.name, ignoreCase = true) ||
                        op.assignedPersonnel.contains(matchedMember.id, ignoreCase = true)
                isValidated = isAssigned
                attActivity = when (op.type) {
                    "Duty Roster" -> "Duty"
                    "Training" -> "Training"
                    else -> "Deployment"
                }
                validationMessage = if (isAssigned) {
                    "Volunteer is scheduled on this Duty Roster operation: \"${op.title}\"."
                } else {
                    "NOT ON ROSTER: Volunteer \"${matchedMember.name}\" is not scheduled on \"${op.title}\"."
                }
            } else {
                isValidated = true
                attActivity = "Duty"
                validationMessage = "Verified as active CEMVA Member. (No specific operations roster selected)"
            }
        } else {
            scannedMember = null
            isValidated = false
            validationMessage = "INVALID CODE: Scanned ID \"$rawValue\" does not match any registered CEMVA members."
        }
        showValidationCard = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Live camera preview layer
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val barcodeScanner = BarcodeScanning.getClient()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !isProcessing) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    if (barcodes.isNotEmpty() && !isProcessing) {
                                        val rawValue = barcodes.first().rawValue
                                        if (rawValue != null) {
                                            isProcessing = true
                                            // Post handling back to main thread
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                handleQrScan(rawValue)
                                            }
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("CameraScannerView", "Error binding camera use cases", e)
                    }
                }, androidx.core.content.ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Shading Overlay & Custom Scan Target Reticle with pulsing Laser line
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val boxSize = canvasWidth * 0.7f
            val left = (canvasWidth - boxSize) / 2
            val top = (canvasHeight - boxSize) / 2
            val right = left + boxSize
            val bottom = top + boxSize

            // 1. Draw darkened boundary masks (top, bottom, left, right)
            drawRect(color = Color.Black.copy(alpha = 0.5f), size = Size(canvasWidth, top))
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(0f, bottom),
                size = Size(canvasWidth, canvasHeight - bottom)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(0f, top),
                size = Size(left, boxSize)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(right, top),
                size = Size(canvasWidth - right, boxSize)
            )

            // 2. Draw border frame corners (Neon green)
            val cornerLength = 32.dp.toPx()
            val strokeWidth = 4.dp.toPx()
            val neonGreen = Color(0xFF10B981)

            // Top-Left corner
            drawLine(neonGreen, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
            drawLine(neonGreen, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

            // Top-Right corner
            drawLine(neonGreen, Offset(right, top), Offset(right - cornerLength, top), strokeWidth)
            drawLine(neonGreen, Offset(right, top), Offset(right, top + cornerLength), strokeWidth)

            // Bottom-Left corner
            drawLine(neonGreen, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth)
            drawLine(neonGreen, Offset(left, bottom), Offset(left, bottom - cornerLength), strokeWidth)

            // Bottom-Right corner
            drawLine(neonGreen, Offset(right, bottom), Offset(right - cornerLength, bottom), strokeWidth)
            drawLine(neonGreen, Offset(right, bottom), Offset(right, bottom - cornerLength), strokeWidth)

            // 3. Draw moving Laser Scanning Line
            val laserY = top + (boxSize * scanLineY)
            drawLine(
                color = neonGreen.copy(alpha = 0.8f),
                start = Offset(left + 8.dp.toPx(), laserY),
                end = Offset(right - 8.dp.toPx(), laserY),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Top Floating Control Bar: Target Operation Duty Roster Selector & Close Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back/Close Button
                IconButton(
                    onClick = {
                        cameraExecutor.shutdown()
                        onClose()
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
                }

                Text(
                    text = "QR Duty Scanner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Interactive Selector Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Target Duty Roster / Operations:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownExpanded = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedOperation?.title ?: "Select Operation...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .fillMaxWidth(0.9f)
                    ) {
                        activeDutyRosters.forEach { op ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(op.title, fontWeight = FontWeight.Bold)
                                        Text("${op.type} • ${op.date} • ${op.location}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedOperation = op
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Live validation results floating card (Overlayed at bottom)
        AnimatedVisibility(
            visible = showValidationCard,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Banner (Verified Success or Access Warning)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (scannedMember != null && isValidated) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (scannedMember != null && isValidated) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (scannedMember != null && isValidated) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (scannedMember != null && isValidated) "ACCESS GRANTED" else "VALIDATION ALERT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (scannedMember != null && isValidated) Color(0xFF047857) else Color(0xFFB91C1C)
                            )
                            Text(
                                text = if (scannedMember != null && isValidated) "Assigned to Duty" else "Roster Check Failed",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    HorizontalDivider()

                    // Detailed verification feedback
                    Text(
                        text = validationMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    scannedMember?.let { member ->
                        // Display verified volunteer information card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column {
                                    Text(member.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("${member.position} • ID: ${member.id}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }

                        // Logging Option Selectors (Check-In or Check-Out chips)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Check-In", "Check-Out").forEach { type ->
                                FilterChip(
                                    selected = attType == type,
                                    onClick = { attType = type },
                                    label = { Text(type, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Bottom Buttons (Action & Cancel)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dismiss/Scan again button
                        OutlinedButton(
                            onClick = {
                                scannedResult = null
                                scannedMember = null
                                showValidationCard = false
                                isProcessing = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retry Scan")
                        }

                        scannedMember?.let { member ->
                            // Confirm save button
                            Button(
                                onClick = {
                                    viewModel.recordAttendance(
                                        memberId = member.id,
                                        memberName = member.name,
                                        type = attType,
                                        activity = attActivity,
                                        method = "QR Code"
                                    )
                                    scannedResult = null
                                    scannedMember = null
                                    showValidationCard = false
                                    isProcessing = false
                                    Toast.makeText(context, "Attendance logged securely: ${member.name}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isValidated) "Log Attendance" else "Bypass & Log")
                            }
                        }
                    }
                }
            }
        }
    }
}
