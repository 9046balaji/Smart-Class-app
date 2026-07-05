package com.vfstr.smartclass.ui.screens.enrollment

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.components.MeshBackground
import com.vfstr.smartclass.ui.theme.DesignSystem
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenBulkUpload(
    vm: BulkUploadViewModel,
    onNavigateBack: () -> Unit
) {
    val uploadResult by vm.uploadResult.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val progress by vm.progress.collectAsState()
    val parsedRows by vm.parsedRows.collectAsState()

    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(uploadResult) {
        if (uploadResult != null && uploadResult!!.created > 0) {
            showConfetti = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bulk Student Upload", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            MeshBackground()

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (uploadResult == null) {
                    if (parsedRows.isEmpty()) {
                        UploadInitialStep { vm.parseCsv(it) }
                    } else {
                        PreviewAndSubmitStep(parsedRows, isLoading, progress) { vm.submitUpload() }
                    }
                } else {
                    UploadResultStep(uploadResult!!) { vm.reset() }
                }
            }

            if (showConfetti) {
                KonfettiView(
                    parties = listOf(
                        Party(
                            speed = 0f,
                            maxSpeed = 30f,
                            damping = 0.9f,
                            spread = 360,
                            colors = listOf(0x00D4FF, 0x7C3AED, 0x10B981),
                            position = Position.Relative(0.5, 0.3),
                            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
                        )
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun UploadInitialStep(onFileSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clickable { onFileSelected("student_id,name,roll_number,branch,year,section\nS001,John Doe,22L11A0501,CSAI,3rd Year,A") }
        ) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.UploadFile, null, modifier = Modifier.size(64.dp), tint = DesignSystem.Cyan)
                Spacer(Modifier.height(16.dp))
                Text("Select CSV File", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Tap to simulate file selection", fontSize = 12.sp, color = DesignSystem.TextMuted)
            }
        }
    }
}

@Composable
fun PreviewAndSubmitStep(rows: List<*>, isLoading: Boolean, progress: Float, onSubmit: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Preview (${rows.size} rows)", fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows.take(20)) { row ->
                // Simple preview card
                Card(colors = CardDefaults.cardColors(containerColor = DesignSystem.Surface)) {
                    Text(row.toString(), modifier = Modifier.padding(12.dp), fontSize = 11.sp, color = Color.White)
                }
            }
        }

        if (isLoading) {
            Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = DesignSystem.Cyan)
                Text("Uploading... ${(progress * 100).toInt()}%", fontSize = 12.sp, color = DesignSystem.Cyan, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        } else {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan)
            ) {
                Text("Confirm & Upload")
            }
        }
    }
}

@Composable
fun UploadResultStep(result: com.vfstr.smartclass.domain.models.BulkUploadResult, onReset: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(80.dp), tint = DesignSystem.Success)
        Spacer(Modifier.height(24.dp))
        Text("Upload Complete", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(16.dp))
        StatRow("Created", result.created.toString(), DesignSystem.Success)
        StatRow("Skipped", result.skipped.toString(), DesignSystem.Warning)
        StatRow("Errors", result.errors.size.toString(), DesignSystem.Danger)
        
        Spacer(Modifier.height(32.dp))
        Button(onClick = onReset, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Surface)) {
            Text("Back to Start")
        }
    }
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = DesignSystem.TextSecondary)
        Text(value, color = color, fontWeight = FontWeight.Bold)
    }
}
