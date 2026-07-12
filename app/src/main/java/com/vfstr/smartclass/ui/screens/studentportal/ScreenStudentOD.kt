package com.vfstr.smartclass.ui.screens.studentportal

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.screens.EmptyStatePlaceholder
import com.vfstr.smartclass.ui.theme.DesignSystem
import java.util.Calendar
import java.util.Locale

@Composable
fun ScreenStudentOD(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val odRequests by vm.studentODRequests.collectAsState()
    val isSubmitting by vm.isSubmittingOD.collectAsState()
    val submitSuccess by vm.odSubmitSuccess.collectAsState()

    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var durationDays by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        vm.loadStudentODRequests()
    }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess == true) {
            eventName = ""
            eventDate = ""
            reason = ""
            durationDays = 1
        }
    }

    // Calendar setup for DatePickerDialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            eventDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSystem.Padding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("On-Duty (OD) Submissions", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        
        GlassmorphicCard {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Register New Request", color = DesignSystem.Cyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Event Name / Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignSystem.Cyan,
                        unfocusedBorderColor = DesignSystem.Border
                    )
                )

                OutlinedTextField(
                    value = eventDate,
                    onValueChange = {},
                    label = { Text("Event Date (Tap to select)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = DesignSystem.Border,
                        disabledTextColor = Color.White,
                        disabledLabelColor = DesignSystem.TextSecondary
                    ),
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = DesignSystem.Cyan)
                        }
                    }
                )
                
                Column {
                    Text(
                        text = "Duration: $durationDays Day(s)",
                        color = DesignSystem.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = durationDays.toFloat(),
                        onValueChange = { durationDays = it.toInt() },
                        valueRange = 1f..7f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = DesignSystem.Cyan,
                            activeTrackColor = DesignSystem.Cyan
                        )
                    )
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason commentary") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignSystem.Cyan,
                        unfocusedBorderColor = DesignSystem.Border
                    )
                )

                if (submitSuccess == false) {
                    Text("Failed to submit request. Please try again.", color = DesignSystem.Danger, fontSize = 12.sp)
                } else if (submitSuccess == true) {
                    Text("Application submitted successfully!", color = DesignSystem.Success, fontSize = 12.sp)
                }

                Button(
                    onClick = { 
                        if (eventName.isNotEmpty() && eventDate.isNotEmpty() && reason.isNotEmpty()) {
                            vm.submitStudentODRequest("College Event", eventName, eventDate, reason)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(DesignSystem.CornerRadius),
                    enabled = !isSubmitting && eventName.isNotEmpty() && eventDate.isNotEmpty()
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Text("Submit Application", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text("OD Request History", color = Color.White, fontWeight = FontWeight.Bold)

        if (odRequests.isEmpty()) {
            EmptyStatePlaceholder(msg = "No submitted OD applications found.")
        } else {
            odRequests.forEach { req ->
                val badgeColor = when (req.status.lowercase()) {
                    "approved" -> DesignSystem.Success
                    "rejected" -> DesignSystem.Danger
                    else -> DesignSystem.Warning
                }
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(req.event_name ?: "OD Request", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Applied: ${(req.requested_at ?: req.applied_on ?: "").substringBefore("T")}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = req.status.uppercase(),
                                color = badgeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
