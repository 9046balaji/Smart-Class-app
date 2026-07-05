package com.vfstr.smartclass.ui.screens.scanner

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vfstr.smartclass.domain.models.AttendanceScanResult
import com.vfstr.smartclass.domain.models.displayName
import com.vfstr.smartclass.ui.theme.DesignSystem
import com.vfstr.smartclass.ui.components.*
import com.vfstr.smartclass.utils.geofence.GeofenceUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScanner(
    sectionId: String?,
    sessionIdFromUrl: String?,
    enrollmentMode: Boolean = false,
    enrollmentRollNo: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel(),
) {
    val geoStatus by viewModel.geoStatus.collectAsState()
    val geoDistance by viewModel.geoDistance.collectAsState()
    val scanning by viewModel.scanning.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val error by viewModel.error.collectAsState()
    val uploadingImage by viewModel.uploadingImage.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val students by viewModel.sectionStudents.collectAsState()
    val pendingStudentId by viewModel.pendingManualStudentId.collectAsState()
    val sessionId by viewModel.sessionId.collectAsState()
    val subject by viewModel.activeSessionSubject.collectAsState()
    
    // Rule: Data Saver Integration
    val mainVm: com.vfstr.smartclass.ui.MainViewModel = hiltViewModel() // Shared if needed, but we can just use another hiltViewModel if it's the same
    // Wait, MainViewModel is usually provided at top level. 
    // Let's assume ScreenScanner can access MainViewModel properties if we pass it or collect it.
    // For now, I'll use a local state or just hiltViewModel for MainViewModel too.
    val isDataSaver by mainVm.isDataSaverEnabled.collectAsState()
    val jpegQuality = if (isDataSaver) 70 else 85

    var showCamera by rememberSaveable { mutableStateOf(false) }
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }

    var manualText by rememberSaveable { mutableStateOf("") }

    val hasUnsavedChanges = manualText.isNotBlank() || pendingStudentId != null

    BackHandler(enabled = hasUnsavedChanges && !showCamera) {
        showExitConfirmation = true
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            containerColor = DesignSystem.Surface,
            title = { Text("Discard progress?", color = Color.White) },
            text = { Text("You have manual entries pending. Exiting will clear this state.", color = DesignSystem.TextSecondary) },
            confirmButton = {
                TextButton(onClick = { 
                    showExitConfirmation = false
                    onNavigateBack() 
                }) {
                    Text("Discard", color = DesignSystem.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    LaunchedEffect(sectionId, sessionIdFromUrl, enrollmentMode, enrollmentRollNo) {
        viewModel.setEnrollmentMode(enrollmentMode, enrollmentRollNo)
        if (sectionId != null) {
            viewModel.setSectionId(sectionId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startLocationTracking()
    }

    var captureTrigger by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bytes = context.contentResolver.openInputStream(it)?.readBytes()
                if (bytes != null) {
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    viewModel.handleImageUpload("data:image/jpeg;base64,$base64")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        containerColor = DesignSystem.Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        if (enrollmentMode) {
                            Text("Face Enrollment Terminal", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = DesignSystem.TextPrimary)
                            Text("Target: $enrollmentRollNo", fontSize = 11.sp, color = DesignSystem.Cyan, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Recognition Terminal", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = DesignSystem.TextPrimary)
                            Text("Target: ${sectionId ?: "N/A"}", fontSize = 11.sp, color = DesignSystem.Cyan, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    if (!enrollmentMode) {
                        TextButton(onClick = { viewModel.syncIndex() }) {
                            Text("SYNC INDEX", color = DesignSystem.Cyan, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DesignSystem.Surface)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            MeshBackground(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DesignSystem.PaddingLarge),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.SpacingLarge)
            ) {
                // Geo Status Badge
                GeoStatusBadge(geoStatus, geoDistance)

                // Session Info
                SessionInfoBanner(sessionId, subject)

                // Scanner Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ScannerActionCard(
                        icon = Icons.Default.CameraAlt,
                        label = "CAMERA",
                        color = DesignSystem.Cyan,
                        glowColor = DesignSystem.CyanGlow,
                        modifier = Modifier.weight(1f),
                        enabled = geoStatus == GeoStatus.ALLOWED && !scanning,
                        onClick = { showCamera = true }
                    )
                    ScannerActionCard(
                        icon = Icons.Default.FileUpload,
                        label = "UPLOAD",
                        color = DesignSystem.Violet,
                        glowColor = DesignSystem.VioletGlow,
                        modifier = Modifier.weight(1f),
                        enabled = geoStatus == GeoStatus.ALLOWED && !uploadingImage,
                        onClick = { launcher.launch("image/*") }
                    )
                }

                // Manual Entry (Optimized for premium feel)
                ManualEntryCard(
                    text = manualText,
                    onTextChange = { manualText = it },
                    onMark = { 
                        viewModel.handleManualMark(it)
                        manualText = ""
                    }
                )

                // Results Area
                AnimatedVisibility(
                    visible = scanResult != null || error != null || uploadingImage || scanning,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (uploadingImage || scanning) {
                            LoadingResultCard(uploadProgress.ifEmpty { "Analyzing frames..." })
                        } else if (error != null) {
                            ErrorResultCard(error!!)
                        } else if (scanResult != null) {
                            ScanResultCard(scanResult!!)
                        }
                    }
                }

                // Students List
                Text("Enrolled Section Roster (${students.size})", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(students) { student ->
                        val isPending = pendingStudentId == student.id
                        SectionStudentCard(
                            name = student.name,
                            id = student.id,
                            isPending = isPending,
                            onClick = {
                                if (isPending) viewModel.handleManualMark(student.id)
                                else viewModel.setPendingStudent(student.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCamera) {
        CameraOverlay(
            captureTrigger = captureTrigger,
            jpegQuality = jpegQuality,
            onClose = { showCamera = false },
            onCapture = { 
                viewModel.handleCapture(it)
                showCamera = false
            },
            onTriggerCapture = {
                captureTrigger++
            }
        )
    }
}

@Composable
fun SessionInfoBanner(sessionId: String?, subject: String) {
    val isFound = sessionId != null
    val color = if (isFound) DesignSystem.Cyan else DesignSystem.Warning
    
    GlassmorphicCard(
        backgroundColor = color.copy(alpha = 0.05f),
        borderColor = color.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(DesignSystem.Padding), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isFound) Icons.Default.Sensors else Icons.Default.ErrorOutline,
                null,
                tint = color,
                modifier = Modifier.size(20.dp).then(if (isFound) Modifier.pulsingGlow(color) else Modifier)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                if (isFound) "TERMINAL CONNECTED: $subject" else "OFFLINE: No active session detected",
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun Modifier.pulsingGlow(color: Color): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    return this.alpha(alpha).shadow(elevation = 8.dp, shape = CircleShape, spotColor = color)
}

@Composable
fun GeoStatusBadge(status: GeoStatus, distance: Double?) {
    val color = when (status) {
        GeoStatus.ALLOWED -> DesignSystem.Success
        GeoStatus.OUT_OF_RANGE -> DesignSystem.Danger
        else -> DesignSystem.Warning
    }
    val text = when (status) {
        GeoStatus.ALLOWED -> "VERIFIED: ON CAMPUS"
        GeoStatus.OUT_OF_RANGE -> "DENIED: OFF CAMPUS (${distance?.toInt() ?: "?"}M)"
        GeoStatus.CHECKING -> "VERIFYING LOCATION..."
        GeoStatus.DENIED -> "PERMISSION DENIED"
        GeoStatus.UNAVAILABLE -> "HARDWARE UNAVAILABLE"
        GeoStatus.IDLE -> "WAITING..."
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), letterSpacing = 1.sp)
    }
}

@Composable
fun ScannerActionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, glowColor: Color, modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    GlassmorphicCard(
        modifier = modifier.height(110.dp).clickable(enabled = enabled) { onClick() },
        borderColor = if (enabled) color.copy(alpha = 0.4f) else DesignSystem.Border,
        glowColor = if (enabled) glowColor else Color.Transparent
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (enabled) color else DesignSystem.TextMuted, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(label, color = if (enabled) Color.White else DesignSystem.TextMuted, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun ManualEntryCard(
    text: String,
    onTextChange: (String) -> Unit,
    onMark: (String) -> Unit
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth().height(90.dp)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.Center) {
            Text("MANUAL OVERRIDE ENTRY", color = DesignSystem.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = text,
                    onValueChange = { onTextChange(it.uppercase()) },
                    placeholder = { Text("ENTER ROLL NUMBER", fontSize = 12.sp, color = DesignSystem.TextMuted) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = DesignSystem.Cyan
                    ),
                    singleLine = true
                )
                IconButton(
                    onClick = { 
                        if (text.isNotBlank()) {
                            onMark(text)
                        }
                    },
                    modifier = Modifier.background(DesignSystem.Cyan.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = DesignSystem.Cyan, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun LoadingResultCard(text: String) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth(), borderColor = DesignSystem.Cyan.copy(alpha = 0.5f)) {
        Row(Modifier.padding(DesignSystem.PaddingLarge), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), color = DesignSystem.Cyan, strokeWidth = 3.dp)
            Spacer(Modifier.width(20.dp))
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun ErrorResultCard(message: String) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth(), borderColor = DesignSystem.Danger.copy(alpha = 0.5f), backgroundColor = DesignSystem.Danger.copy(alpha = 0.05f)) {
        Row(Modifier.padding(DesignSystem.PaddingLarge), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ReportProblem, null, tint = DesignSystem.Danger, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(20.dp))
            Text(message, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ScanResultCard(result: AttendanceScanResult) {
    val color = when (result.status) {
        "marked", "success" -> DesignSystem.Success
        "wrong_section" -> DesignSystem.Warning
        else -> DesignSystem.Danger
    }
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = color.copy(alpha = 0.6f),
        glowColor = color.copy(alpha = 0.15f)
    ) {
        Column(Modifier.padding(DesignSystem.PaddingLarge)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (result.status) {
                        "marked", "success" -> Icons.Default.Verified
                        "wrong_section" -> Icons.Default.Warning
                        else -> Icons.Default.Error
                    },
                    null, tint = color, modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(result.status.replace("_", " ").uppercase(), color = color, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(16.dp))
            if (result.marked_count != null && result.marked_count > 1) {
                Text("${result.marked_count} STUDENTS VERIFIED", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                result.results?.filter { it.status == "marked" }?.forEach { item ->
                    Text("• ${item.displayName()}", color = DesignSystem.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else if (result.student_name != null) {
                Text(result.student_name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("Confidence: ${(result.confidence ?: 0.0) * 100}%", color = DesignSystem.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            if (result.message != null) {
                Spacer(Modifier.height(8.dp))
                Text(result.message, color = DesignSystem.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionStudentCard(name: String, id: String, isPending: Boolean, onClick: () -> Unit) {
    GlassmorphicCard(
        modifier = Modifier.clickable { onClick() },
        borderColor = if (isPending) DesignSystem.Warning else DesignSystem.Border,
        backgroundColor = if (isPending) DesignSystem.Warning.copy(alpha = 0.1f) else DesignSystem.CardBg
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, maxLines = 1)
            Text(id, color = DesignSystem.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            if (isPending) {
                Spacer(Modifier.height(4.dp))
                Text("TAP AGAIN", color = DesignSystem.Warning, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun CameraOverlay(
    captureTrigger: Int,
    jpegQuality: Int = 85,
    onClose: () -> Unit, 
    onCapture: (String) -> Unit,
    onTriggerCapture: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_line")
    val linePos by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "line"
    )

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            captureTrigger = captureTrigger,
            jpegQuality = jpegQuality,
            onImageCaptured = onCapture
        )
        
        // Scanning brackets & Pulse Line
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(280.dp)) {
                Canvas(Modifier.fillMaxSize()) {
                    val len = 40.dp.toPx()
                    val stroke = 6.dp.toPx()
                    val color = DesignSystem.Cyan
                    // Brackets
                    drawLine(color, Offset(0f, 0f), Offset(len, 0f), strokeWidth = stroke)
                    drawLine(color, Offset(0f, 0f), Offset(0f, len), strokeWidth = stroke)
                    drawLine(color, Offset(size.width, 0f), Offset(size.width - len, 0f), strokeWidth = stroke)
                    drawLine(color, Offset(size.width, 0f), Offset(size.width, len), strokeWidth = stroke)
                    drawLine(color, Offset(0f, size.height), Offset(len, size.height), strokeWidth = stroke)
                    drawLine(color, Offset(0f, size.height), Offset(0f, size.height - len), strokeWidth = stroke)
                    drawLine(color, Offset(size.width, size.height), Offset(size.width - len, size.height), strokeWidth = stroke)
                    drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - len), strokeWidth = stroke)
                    
                    // Scanning line
                    val y = size.height * linePos
                    drawLine(
                        brush = Brush.horizontalGradient(listOf(Color.Transparent, DesignSystem.Cyan, Color.Transparent)),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        // Controls with premium styling
        Box(Modifier.fillMaxSize().padding(DesignSystem.PaddingLarge)) {
            IconButton(
                onClick = onClose, 
                modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
            
            Column(modifier = Modifier.align(Alignment.BottomCenter), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("POSITION FACE IN FRAME", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onTriggerCapture,
                    modifier = Modifier.size(84.dp).border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(Modifier.size(70.dp).clip(CircleShape).border(2.dp, Color.Black, CircleShape))
                }
            }
        }
    }
}
