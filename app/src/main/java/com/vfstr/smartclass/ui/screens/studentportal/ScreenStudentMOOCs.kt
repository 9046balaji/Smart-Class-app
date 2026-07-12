package com.vfstr.smartclass.ui.screens.studentportal

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.School
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
import androidx.lifecycle.viewModelScope
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.screens.EmptyStatePlaceholder
import com.vfstr.smartclass.ui.theme.DesignSystem
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun ScreenStudentCertificates(
    vm: MainViewModel,
    modifier: Modifier = Modifier,
    moocVm: MoocViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    val enrollments by moocVm.enrollments.collectAsState()
    val isLoading by moocVm.isLoading.collectAsState()
    val isEnrolling by moocVm.isEnrolling.collectAsState()

    var showEnrollDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        moocVm.loadMOOCs()
    }

    Column(modifier = modifier.fillMaxSize().padding(DesignSystem.Padding)) {
        Text("Academic Certificates", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(8.dp))
        Text("R22 Regulation requires 9 elective credits via MOOCs", color = DesignSystem.TextSecondary, fontSize = 12.sp)
        
        Spacer(Modifier.height(16.dp))

        // Progress bar for R22 requirement
        val completedCredits = enrollments.filter { it.completion_status?.lowercase() == "completed" || it.status?.lowercase() == "completed" }.sumOf { it.credits ?: 0 }
        GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("MOOC Degree Progress", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { completedCredits.toFloat() / 9f },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = DesignSystem.Violet,
                        trackColor = DesignSystem.Border
                    )
                    Spacer(Modifier.width(16.dp))
                    Text("$completedCredits / 9 Credits", color = DesignSystem.Violet, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }

        if (isLoading) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(3) {
                    com.vfstr.smartclass.ui.components.ShimmerSkeleton(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(12.dp)))
                }
            }
        } else if (enrollments.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                EmptyStatePlaceholder(msg = "Upload and manage your external MOOC certificates.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(enrollments) { item ->
                    GlassmorphicCard {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(DesignSystem.Violet.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, null, tint = DesignSystem.Violet, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.course_name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${item.platform ?: "MOOC"} • ${item.credits ?: 0} Credits", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            }
                            val statusStr = item.completion_status ?: item.status ?: "enrolled"
                            val statusColor = if (statusStr.equals("completed", true)) DesignSystem.Success else DesignSystem.Cyan
                            Text(statusStr.uppercase(), color = statusColor, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { showEnrollDialog = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CloudUpload, null)
            Spacer(Modifier.width(8.dp))
            Text("Register New MOOC", fontWeight = FontWeight.ExtraBold)
        }
    }

    if (showEnrollDialog) {
        var courseName by remember { mutableStateOf("") }
        var platform by remember { mutableStateOf("Coursera") }
        var courseCode by remember { mutableStateOf("") }
        var credits by remember { mutableStateOf(3) }
        var enrollDate by remember { mutableStateOf("") }
        var semester by remember { mutableStateOf(5) }
        var academicYear by remember { mutableStateOf("2025-2026") }

        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            context,
            { _, y, m, d -> enrollDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        AlertDialog(
            onDismissRequest = { showEnrollDialog = false },
            title = { Text("Register MOOC Elective") },
            containerColor = DesignSystem.Surface,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Course Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Platform dropdown options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Swayam", "NPTEL", "Coursera", "edX").forEach { p ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (platform == p) DesignSystem.Cyan.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(1.dp, if (platform == p) DesignSystem.Cyan else DesignSystem.Border, RoundedCornerShape(8.dp))
                                    .clickable { platform = p }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(p, color = if (platform == p) DesignSystem.Cyan else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Course Code (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = credits.toString(),
                            onValueChange = { credits = it.toIntOrNull() ?: 1 },
                            label = { Text("Credits") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = semester.toString(),
                            onValueChange = { semester = it.toIntOrNull() ?: 5 },
                            label = { Text("Semester") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = enrollDate,
                        onValueChange = {},
                        label = { Text("Enrollment Date") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePicker.show() },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledBorderColor = DesignSystem.Border,
                            disabledLabelColor = DesignSystem.TextSecondary
                        ),
                        trailingIcon = {
                            IconButton(onClick = { datePicker.show() }) {
                                Icon(Icons.Default.CalendarToday, null, tint = DesignSystem.Cyan)
                            }
                        }
                    )

                    OutlinedTextField(
                        value = academicYear,
                        onValueChange = { academicYear = it },
                        label = { Text("Academic Year") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (courseName.isNotEmpty() && enrollDate.isNotEmpty()) {
                            moocVm.enrollMOOC(
                                courseName = courseName,
                                platform = platform,
                                courseCode = courseCode.ifEmpty { null },
                                credits = credits,
                                enrollmentDate = enrollDate,
                                semester = semester,
                                academicYear = academicYear,
                                onSuccess = { showEnrollDialog = false }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan, contentColor = Color.Black),
                    enabled = !isEnrolling && courseName.isNotEmpty() && enrollDate.isNotEmpty()
                ) {
                    if (isEnrolling) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black)
                    } else {
                        Text("Register", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnrollDialog = false }) {
                    Text("Cancel", color = DesignSystem.TextMuted)
                }
            }
        )
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class MoocViewModel @javax.inject.Inject constructor(
    private val repository: com.vfstr.smartclass.data.repositories.AppRepository
) : androidx.lifecycle.ViewModel() {
    private val _enrollments = kotlinx.coroutines.flow.MutableStateFlow<List<com.vfstr.smartclass.data.remote.api.MOOCEnrollmentDto>>(emptyList())
    val enrollments: kotlinx.coroutines.flow.StateFlow<List<com.vfstr.smartclass.data.remote.api.MOOCEnrollmentDto>> = _enrollments
    
    private val _isLoading = kotlinx.coroutines.flow.MutableStateFlow(true)
    val isLoading: kotlinx.coroutines.flow.StateFlow<Boolean> = _isLoading
    
    private val _isEnrolling = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isEnrolling: kotlinx.coroutines.flow.StateFlow<Boolean> = _isEnrolling
    
    fun loadMOOCs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _enrollments.value = repository.getStudentMOOCs()
            } catch (e: Exception) {
                // Return empty on failure for now
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun enrollMOOC(
        courseName: String,
        platform: String,
        courseCode: String?,
        credits: Int?,
        enrollmentDate: String?,
        semester: Int?,
        academicYear: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isEnrolling.value = true
            try {
                val payload = com.vfstr.smartclass.data.remote.api.StudentMOOCEnrollPayload(
                    course_name = courseName,
                    platform = platform,
                    course_code = courseCode,
                    credits = credits,
                    enrollment_date = enrollmentDate,
                    semester = semester,
                    academic_year = academicYear
                )
                val result = repository.enrollStudentMOOC(payload)
                if (result != null) {
                    loadMOOCs()
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isEnrolling.value = false
            }
        }
    }
}
