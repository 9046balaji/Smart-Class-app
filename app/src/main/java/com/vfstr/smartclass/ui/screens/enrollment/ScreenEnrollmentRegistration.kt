package com.vfstr.smartclass.ui.screens.enrollment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.vfstr.smartclass.domain.models.EnrollmentRequestPayload
import com.vfstr.smartclass.domain.models.PhotoType
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.components.MeshBackground
import com.vfstr.smartclass.ui.theme.DesignSystem
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenEnrollmentRegistration(
    vm: EnrollmentViewModel,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    val isLoading by vm.isLoading.collectAsState()
    val requestState by vm.requestState.collectAsState()

    val formData = remember { mutableStateOf(EnrollmentRequestPayload("", "", "", "", "", null, null)) }
    val photos = remember { mutableStateMapOf<PhotoType, File>() }

    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(requestState) {
        if (requestState != null) {
            showConfetti = true
            // delay and onSuccess after confetti
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Student Enrollment", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = DesignSystem.TextPrimary
                )
            )
        },
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            MeshBackground()

            Column(modifier = Modifier.fillMaxSize().padding(DesignSystem.Padding)) {
                StepIndicator(currentStep = currentStep)
                
                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                            } else {
                                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                            }
                        }, label = "wizard_step"
                    ) { step ->
                        when (step) {
                            1 -> PersonalDetailsStep(formData)
                            2 -> AcademicDetailsStep(formData)
                            3 -> PhotoCaptureStep(photos)
                        }
                    }
                }
            }

            // F9: Submit/Next button always visible in BottomBar for active field accessibility
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(DesignSystem.Padding)
            ) {
                when (currentStep) {
                    1 -> Button(
                        onClick = { currentStep = 2 },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan),
                        enabled = formData.value.name.isNotBlank() && formData.value.rollNumber.isNotBlank()
                    ) {
                        Text("Next", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                    2 -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Back") }
                        Button(
                            onClick = { currentStep = 3 },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan),
                            enabled = formData.value.branch.isNotBlank() && formData.value.year.isNotBlank() && formData.value.section.isNotBlank()
                        ) { Text("Next") }
                    }
                    3 -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { currentStep = 2 },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Back") }
                        Button(
                            onClick = { vm.submitEnrollment(formData.value, photos) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Success),
                            enabled = photos.size == 3
                        ) { Text("Submit Application") }
                    }
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DesignSystem.Cyan)
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
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    onSuccess()
                }
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepItem(1, "Details", currentStep >= 1, currentStep == 1)
        Box(modifier = Modifier.width(40.dp).height(2.dp).background(if (currentStep > 1) DesignSystem.Cyan else DesignSystem.Border))
        StepItem(2, "Academic", currentStep >= 2, currentStep == 2)
        Box(modifier = Modifier.width(40.dp).height(2.dp).background(if (currentStep > 2) DesignSystem.Cyan else DesignSystem.Border))
        StepItem(3, "Photos", currentStep >= 3, currentStep == 3)
    }
}

@Composable
fun StepItem(step: Int, label: String, isCompleted: Boolean, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isCompleted) DesignSystem.Cyan else DesignSystem.Surface)
                .border(1.dp, if (isActive) DesignSystem.Cyan else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted && !isActive) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            } else {
                Text(step.toString(), color = if (isActive || isCompleted) Color.White else DesignSystem.TextSecondary, fontWeight = FontWeight.Bold)
            }
        }
        Text(label, fontSize = 10.sp, color = if (isActive) DesignSystem.Cyan else DesignSystem.TextSecondary, modifier = Modifier.padding(top = 4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsStep(
    formData: MutableState<EnrollmentRequestPayload>
) {
    val context = LocalContext.current
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(bottom = 80.dp) // Space for bottom bar
    ) {
        item {
            OutlinedTextField(
                value = formData.value.name,
                onValueChange = { formData.value = formData.value.copy(name = it) },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
        item {
            OutlinedTextField(
                value = formData.value.rollNumber,
                onValueChange = { formData.value = formData.value.copy(rollNumber = it.uppercase()) },
                label = { Text("Roll Number") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
        item {
            OutlinedTextField(
                value = formData.value.mobileNumber ?: "",
                onValueChange = { formData.value = formData.value.copy(mobileNumber = it) },
                label = { Text("Mobile Number") },
                prefix = { Text("+91 ") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
        item {
            val datePickerState = rememberDatePickerState()
            var showDatePicker by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = formData.value.dateOfBirth ?: "",
                onValueChange = {},
                label = { Text("Date of Birth") },
                readOnly = true,
                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.CalendarToday, null) } },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { showDatePicker = true },
                shape = RoundedCornerShape(12.dp)
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val date = java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                formData.value = formData.value.copy(dateOfBirth = date.toString())
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    }
                ) { DatePicker(state = datePickerState) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicDetailsStep(
    formData: MutableState<EnrollmentRequestPayload>
) {
    val branches = listOf("CSAI", "CSE", "IT", "ECE", "EEE", "MECH", "CIVIL", "BIOTECH", "PHARMACY")
    val years = listOf("1st Year", "2nd Year", "3rd Year", "4th Year")
    val sections = listOf("A", "B", "C", "D", "E")

    var showBranchSheet by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text("Academic Details", style = MaterialTheme.typography.titleMedium, color = DesignSystem.TextPrimary)
        }
        item {
            OutlinedTextField(
                value = formData.value.branch,
                onValueChange = {},
                label = { Text("Branch") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { showBranchSheet = true },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                shape = RoundedCornerShape(12.dp)
            )
        }
        item {
            Text("Year", style = MaterialTheme.typography.labelMedium, color = DesignSystem.TextSecondary)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                years.forEachIndexed { index, year ->
                    SegmentedButton(
                        selected = formData.value.year == year,
                        onClick = { formData.value = formData.value.copy(year = year) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = years.size)
                    ) { Text(year, fontSize = 12.sp) }
                }
            }
        }
        item {
            Text("Section", style = MaterialTheme.typography.labelMedium, color = DesignSystem.TextSecondary)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                sections.forEachIndexed { index, sec ->
                    SegmentedButton(
                        selected = formData.value.section == sec,
                        onClick = { formData.value = formData.value.copy(section = sec) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = sections.size)
                    ) { Text(sec) }
                }
            }
        }
    }

    if (showBranchSheet) {
        ModalBottomSheet(onDismissRequest = { showBranchSheet = false }) {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(DesignSystem.Padding)) {
                items(branches.size) { index ->
                    val branch = branches[index]
                    ListItem(
                        headlineContent = { Text(branch) },
                        modifier = Modifier.clickable {
                            formData.value = formData.value.copy(branch = branch)
                            showBranchSheet = false
                        },
                        trailingContent = { if (formData.value.branch == branch) Icon(Icons.Default.Check, null, tint = DesignSystem.Cyan) }
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoCaptureStep(
    photos: MutableMap<PhotoType, File>
) {
    var activePhotoType by remember { mutableStateOf<PhotoType?>(null) }
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)
    ) {
        Text("Face Enrollment", style = MaterialTheme.typography.titleMedium, color = DesignSystem.TextPrimary)
        Text("3 photos required for biometric registration.", color = DesignSystem.TextSecondary, fontSize = 12.sp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PhotoSlot(PhotoType.FRONT, "Front", photos[PhotoType.FRONT], modifier = Modifier.weight(1f)) { activePhotoType = PhotoType.FRONT }
            PhotoSlot(PhotoType.LEFT, "Left", photos[PhotoType.LEFT], modifier = Modifier.weight(1f)) { activePhotoType = PhotoType.LEFT }
            PhotoSlot(PhotoType.RIGHT, "Right", photos[PhotoType.RIGHT], modifier = Modifier.weight(1f)) { activePhotoType = PhotoType.RIGHT }
        }
    }

    if (activePhotoType != null) {
        CameraBottomSheet(
            type = activePhotoType!!,
            onDismiss = { activePhotoType = null },
            onCaptured = { file ->
                photos[activePhotoType!!] = file
                activePhotoType = null
            }
        )
    }
}

@Composable
fun PhotoSlot(
    type: PhotoType,
    label: String,
    file: File?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(16.dp))
                .background(DesignSystem.Surface)
                .border(1.dp, if (file != null) DesignSystem.Success else DesignSystem.Border, RoundedCornerShape(16.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (file != null) {
                val bitmap = remember(file) { BitmapFactory.decodeFile(file.absolutePath) }
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                Icon(Icons.Default.CheckCircle, null, tint = DesignSystem.Success, modifier = Modifier.size(24.dp).align(Alignment.TopEnd).padding(8.dp))
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, null, tint = DesignSystem.TextMuted)
                    Text(label, fontSize = 10.sp, color = DesignSystem.TextMuted)
                }
            }
        }
        Text(type.name, fontSize = 10.sp, color = DesignSystem.TextSecondary, modifier = Modifier.padding(top = 4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraBottomSheet(
    type: PhotoType,
    onDismiss: () -> Unit,
    onCaptured: (File) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    
    var brightnessMessage by remember { mutableStateOf("Position your face") }
    var isBrightnessOk by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        
        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(ContextCompat.getMainExecutor(context)) { image ->
                    val buffer = image.planes[0].buffer
                    val data = ByteArray(buffer.remaining())
                    buffer.get(data)
                    val pixels = data.map { it.toInt() and 0xFF }
                    val avg = if (pixels.isNotEmpty()) pixels.average() else 0.0
                    
                    if (avg < 30) {
                        brightnessMessage = "Too Dark"
                        isBrightnessOk = false
                    } else if (avg > 240) {
                        brightnessMessage = "Too Bright"
                        isBrightnessOk = true // Technically too bright but let's say post-capture rejects it? No, rule says reject.
                        isBrightnessOk = false
                    } else {
                        brightnessMessage = "Perfect Lighting"
                        isBrightnessOk = true
                    }
                    image.close()
                }
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageCapture, analyzer)
        } catch (e: Exception) {}
    }

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.fillMaxHeight(0.9f)) {
        Column(modifier = Modifier.fillMaxSize().padding(DesignSystem.Padding), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Capture ${type.name} Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.weight(1f).aspectRatio(3f/4f).clip(RoundedCornerShape(24.dp)).border(2.dp, if (isBrightnessOk) DesignSystem.Cyan else DesignSystem.Danger, RoundedCornerShape(24.dp))) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
                
                // Guidance Overlay
                Box(Modifier.fillMaxSize().border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)))
                
                Box(Modifier.align(Alignment.BottomCenter).padding(16.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(brightnessMessage, color = if (isBrightnessOk) DesignSystem.Success else DesignSystem.Danger, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
            FloatingActionButton(
                onClick = {
                    if (isBrightnessOk) {
                        val file = File(context.cacheDir, "temp_${type.name}.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) { onCaptured(file) }
                            override fun onError(exception: ImageCaptureException) {}
                        })
                    }
                },
                containerColor = if (isBrightnessOk) DesignSystem.Cyan else DesignSystem.Surface,
                modifier = Modifier.size(72.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Camera, null, modifier = Modifier.size(32.dp), tint = if (isBrightnessOk) Color.White else DesignSystem.TextMuted)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}


