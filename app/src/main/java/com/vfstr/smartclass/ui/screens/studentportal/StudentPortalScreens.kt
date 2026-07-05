package com.vfstr.smartclass.ui.screens.studentportal

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.components.RadialGauge
import com.vfstr.smartclass.ui.screens.EmptyStatePlaceholder
import com.vfstr.smartclass.ui.theme.DesignSystem
import java.util.Locale

@Composable
fun ScreenStudentOverview(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val profile by vm.studentProfile.collectAsState()
    val cgpa by vm.cgpaAnimated.collectAsState()
    val isScanning by vm.isScanningForBeacon.collectAsState()

    LaunchedEffect(Unit) {
        // Only start if permissions are already granted to avoid silent crash/ANR
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasFine) {
            vm.startBleScanner()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSystem.Padding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Card with Gradient
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth().height(140.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.linearGradient(listOf(DesignSystem.Violet.copy(alpha = 0.2f), DesignSystem.Cyan.copy(alpha = 0.1f)))
            ))
            Column(modifier = Modifier.padding(DesignSystem.PaddingLarge)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("REGULATED STUDENT PORTAL", color = DesignSystem.Cyan, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    if (isScanning) {
                        ScanningIndicator()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Good Day, ${profile?.name ?: "Student"}",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Text("Roll Number: ${profile?.rollNo ?: "N/A"}", color = DesignSystem.TextSecondary, fontSize = 12.sp)
            }
        }

        // Snapshot Stats
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GlassmorphicCard(modifier = Modifier.weight(1f).height(100.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current CGPA", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f", cgpa),
                        style = MaterialTheme.typography.headlineMedium.copy(color = DesignSystem.Violet, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    )
                }
            }
            GlassmorphicCard(modifier = Modifier.weight(1f).height(100.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Overall Attendance", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "84.2%",
                        style = MaterialTheme.typography.headlineMedium.copy(color = DesignSystem.Success, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }

        Text("Recent Attendance snapshot", color = Color.White, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                RadialGauge(percentage = 82f, statusText = "CS501L", strokeColor = DesignSystem.Cyan, size = 90.dp)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                RadialGauge(percentage = 75f, statusText = "CS502T", strokeColor = DesignSystem.Violet, size = 90.dp)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                RadialGauge(percentage = 91f, statusText = "CS503P", strokeColor = DesignSystem.Success, size = 90.dp)
            }
        }
        
        HorizontalDivider(color = DesignSystem.Border)
        
        Text("Recent activity feed", color = Color.White, fontWeight = FontWeight.Bold)
        repeat(3) {
            ActivityItem()
        }
    }
}

@Composable
fun ScanningIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(DesignSystem.Success.copy(alpha = alpha))
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "SYNC ACTIVE",
            color = DesignSystem.Success.copy(alpha = alpha),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActivityItem() {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(DesignSystem.CardBg), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = DesignSystem.Success, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Attendance Marked: CSAI-3A AI", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("22 May 2026, 10:42 AM", color = DesignSystem.TextMuted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun ScreenStudentAttendance(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(DesignSystem.Padding).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Attendance Ledger", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))

        // Heatmap Matrix
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Activity Heatmap", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                repeat(4) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        repeat(7) { 
                            val alpha = (1..10).random() / 10f
                            Box(Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)).background(DesignSystem.Success.copy(alpha = alpha))) 
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        listOf("CS501L AI Lecture" to "Present", "CS503P ML Practice" to "Present", "CS504L SE Lecture" to "Absent").forEach { (subj, status) ->
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(subj, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("22 May 2026", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                    }
                    val color = if (status == "Present") DesignSystem.Success else DesignSystem.Danger
                    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(color.copy(alpha = 0.1f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(status.uppercase(), color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenStudentPerformance(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val cgpa by vm.cgpaAnimated.collectAsState()
    Column(
        modifier = modifier.fillMaxSize().padding(DesignSystem.Padding).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Performance Metrics", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold), modifier = Modifier.align(Alignment.Start))
        
        RadialGauge(percentage = (cgpa.toFloat() / 10f) * 100f, statusText = "CGPA", strokeColor = DesignSystem.Violet, size = 180.dp, strokeWidth = 12.dp)

        Text("GPA Progression Trend", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        com.vfstr.smartclass.ui.components.AnimatedBarChart(
            data = listOf(8.51f, 8.24f, 8.42f),
            colors = listOf(DesignSystem.Cyan, DesignSystem.Violet, DesignSystem.Success),
            modifier = Modifier.height(150.dp)
        )
    }
}

@Composable
fun ScreenStudentAcademics(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(DesignSystem.Padding)) {
        Text("Registered Syllabus", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(20.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(listOf("Artificial Intelligence", "Machine Learning", "Compiler Design", "Software Engineering")) { subj ->
                GlassmorphicCard {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(DesignSystem.Cyan.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Book, null, tint = DesignSystem.Cyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(subj, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("CS50${(1..4).random()}L • 4 Credits", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenStudentOD(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    var eventName by remember { mutableStateOf("") }
    var dates by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(DesignSystem.Padding).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("On-Duty Submissions", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        
        GlassmorphicCard {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Register New Request", color = DesignSystem.Cyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                OutlinedTextField(value = eventName, onValueChange = { eventName = it }, label = { Text("Event Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = dates, onValueChange = { dates = it }, label = { Text("Dates (e.g. 25-26 May)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason commentary") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))

                Button(
                    onClick = { 
                        if (eventName.isNotEmpty()) {
                            vm.submitStudentODRequest("College Event", eventName, dates, reason)
                            eventName = ""; dates = ""; reason = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(DesignSystem.CornerRadius)
                ) {
                    Text("Submit Application", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ScreenStudentCertificates(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(DesignSystem.Padding)) {
        Text("Academic Certificates", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(16.dp))
        EmptyStatePlaceholder(msg = "Upload and manage your external MOOC certificates.")
        
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CloudUpload, null, tint = DesignSystem.Cyan)
            Spacer(Modifier.width(8.dp))
            Text("Select Document")
        }
    }
}
