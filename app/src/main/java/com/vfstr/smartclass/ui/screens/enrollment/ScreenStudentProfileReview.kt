package com.vfstr.smartclass.ui.screens.enrollment

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.vfstr.smartclass.domain.models.*
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.components.MeshBackground
import com.vfstr.smartclass.ui.theme.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenStudentProfileReview(
    vm: EnrollmentAdminViewModel,
    studentId: String,
    onNavigateBack: () -> Unit
) {
    var studentProfile by remember { mutableStateOf<EnrollmentStudent?>(null) }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var showReviewDialog by remember { mutableStateOf<ReviewAction?>(null) }
    
    LaunchedEffect(studentId) {
        studentProfile = vm.fetchStudentProfile(studentId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Review Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            MeshBackground()

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { ProfileHeader(studentProfile) }
                item { InfoSection("Personal Information", listOf(
                    "Mobile" to (studentProfile?.mobileNumber ?: "N/A"),
                    "Email" to (studentProfile?.email ?: "N/A"),
                    "DOB" to (studentProfile?.dateOfBirth ?: "N/A")
                )) }
                item { InfoSection("Academic Details", listOf(
                    "Branch" to (studentProfile?.branch ?: "N/A"),
                    "Year" to (studentProfile?.year ?: "N/A"),
                    "Section" to (studentProfile?.section ?: "N/A")
                )) }
                item { PhotoGallery(studentProfile?.photos ?: emptyList()) { selectedPhotoUrl = it } }
                
                item {
                    ReviewActions(
                        onApprove = { vm.approveRequest(studentId) },
                        onReject = { showReviewDialog = ReviewAction.REJECT },
                        onRevision = { showReviewDialog = ReviewAction.REVISION }
                    )
                }
            }
        }
    }

    if (selectedPhotoUrl != null) {
        FullScreenPhotoViewer(url = selectedPhotoUrl!!) { selectedPhotoUrl = null }
    }

    if (showReviewDialog != null) {
        ReviewCommentDialog(
            action = showReviewDialog!!,
            onDismiss = { showReviewDialog = null },
            onSubmit = { comment ->
                if (showReviewDialog == ReviewAction.REJECT) vm.rejectRequest(studentId, comment)
                else vm.requestRevision(studentId, comment)
                showReviewDialog = null
                onNavigateBack()
            }
        )
    }
}

@Composable
fun ProfileHeader(profile: EnrollmentStudent?) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(80.dp).clip(CircleShape).background(DesignSystem.Surface), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp), tint = DesignSystem.TextMuted)
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(profile?.name ?: "Loading...", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("ID: ${profile?.studentId ?: "..."}", fontSize = 14.sp, color = DesignSystem.Cyan)
                Text("Roll: ${profile?.rollNumber ?: "..."}", fontSize = 14.sp, color = DesignSystem.TextSecondary)
            }
        }
    }
}

@Composable
fun InfoSection(title: String, items: List<Pair<String, String>>) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = DesignSystem.Cyan, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            items.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = DesignSystem.TextSecondary, fontSize = 13.sp)
                    Text(value, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun PhotoGallery(photos: List<StudentPhoto>, onPhotoClick: (String) -> Unit) {
    Column {
        Text("Face Photos", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PhotoItem("Front", photos.find { it.photoType == PhotoType.FRONT }?.filePath, onPhotoClick, Modifier.weight(1f))
            PhotoItem("Left", photos.find { it.photoType == PhotoType.LEFT }?.filePath, onPhotoClick, Modifier.weight(1f))
            PhotoItem("Right", photos.find { it.photoType == PhotoType.RIGHT }?.filePath, onPhotoClick, Modifier.weight(1f))
        }
    }
}

@Composable
fun PhotoItem(label: String, url: String?, onClick: (String) -> Unit, modifier: Modifier) {
    Box(
        modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(12.dp))
            .background(DesignSystem.Surface)
            .clickable(enabled = url != null) { url?.let { onClick(it) } },
        contentAlignment = Alignment.Center
    ) {
        if (url != null) {
            AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(Icons.Default.NoPhotography, null, tint = DesignSystem.TextMuted)
        }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
        Text(label, Modifier.align(Alignment.BottomCenter).padding(4.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReviewActions(onApprove: () -> Unit, onReject: () -> Unit, onRevision: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onApprove, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Success)) {
            Text("Approve", fontSize = 12.sp)
        }
        Button(onClick = onRevision, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Warning)) {
            Text("Revision", fontSize = 12.sp)
        }
        Button(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Danger)) {
            Text("Reject", fontSize = 12.sp)
        }
    }
}

@Composable
fun FullScreenPhotoViewer(url: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black).clickable { onDismiss() }) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun ReviewCommentDialog(action: ReviewAction, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var comment by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (action == ReviewAction.REJECT) "Reject Application" else "Request Revision") },
        text = {
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Reason / Comments") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(comment) },
                enabled = comment.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = if (action == ReviewAction.REJECT) DesignSystem.Danger else DesignSystem.Warning)
            ) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

enum class ReviewAction { REJECT, REVISION }
