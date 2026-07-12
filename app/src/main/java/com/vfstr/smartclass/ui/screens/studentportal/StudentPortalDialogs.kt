package com.vfstr.smartclass.ui.screens.studentportal

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem
import java.util.Locale

@Composable
fun BacklogsDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    val summary by vm.studentBacklogs.collectAsState()
    val activeList = summary?.active_backlogs ?: emptyList()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = DesignSystem.Cyan)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Backlog Ledger", color = Color.White, fontWeight = FontWeight.Bold)
                if (activeList.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DesignSystem.Danger.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("${activeList.size} ACTIVE", color = DesignSystem.Danger, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Cleared Backlogs Count: ${summary?.cleared_backlogs_count ?: 0}",
                    color = DesignSystem.Success,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                
                HorizontalDivider(color = DesignSystem.Border)

                if (activeList.isEmpty()) {
                    Text("No active backlogs. Clean academic status!", color = DesignSystem.Success, fontSize = 12.sp)
                } else {
                    activeList.forEach { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DesignSystem.CardBg)
                                .border(androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Border), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.subject_name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(DesignSystem.Danger.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ACTIVE", color = DesignSystem.Danger, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Code: ${item.subject_code} • Failed: ${item.semester_failed}", color = DesignSystem.TextSecondary, fontSize = 10.sp)
                                Text("Re-exam: ${item.next_exam_window} • Attempts: ${item.attempts}", color = DesignSystem.TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        },
        containerColor = DesignSystem.Surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun HallTicketDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    val ticket by vm.studentHallTicket.collectAsState()
    val errorMsg by vm.hallTicketError.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.loadStudentHallTicket()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (ticket != null) {
                    TextButton(onClick = {
                        val shareText = buildString {
                            appendLine("HALL TICKET — ${ticket!!.exam_session}")
                            appendLine("Name: ${ticket!!.student_name}")
                            appendLine("Roll No: ${ticket!!.roll_no}")
                            ticket!!.department?.let { appendLine("Dept: $it") }
                            appendLine("Center: ${ticket!!.exam_center}")
                            appendLine("---")
                            ticket!!.subjects.forEach { sub ->
                                appendLine("${sub.subject_code} — ${sub.subject_name}")
                                appendLine("  Date: ${sub.exam_date} | Time: ${sub.exam_time}")
                            }
                        }
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share Hall Ticket"))
                    }) {
                        Icon(Icons.Default.Share, null, tint = DesignSystem.Cyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Share", color = DesignSystem.Cyan)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", color = DesignSystem.Cyan)
                }
            }
        },
        title = {
            Text("Examination Hall Ticket", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            if (errorMsg != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DesignSystem.Danger.copy(alpha = 0.1f))
                        .border(androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Danger.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Warning, null, tint = DesignSystem.Danger, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMsg!!,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else if (ticket == null) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DesignSystem.Cyan)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DesignSystem.Cyan.copy(alpha = 0.05f))
                            .border(androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Cyan.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Name: ${ticket?.student_name}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Roll Number: ${ticket?.roll_no}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            ticket?.department?.let { Text("Department: $it", color = DesignSystem.TextSecondary, fontSize = 11.sp) }
                            if (ticket?.year != null || ticket?.section != null) {
                                Text("Year: ${ticket?.year ?: "-"} • Section: ${ticket?.section ?: "-"}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            }
                            Text("Session: ${ticket?.exam_session}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            Text("Center: ${ticket?.exam_center}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Text("Registered Exams", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    ticket?.subjects?.forEach { sub ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DesignSystem.CardBg)
                                .border(androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Border), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(sub.subject_name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text(sub.subject_code, color = DesignSystem.Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Date: ${sub.exam_date}", color = DesignSystem.TextSecondary, fontSize = 10.sp)
                                    Text("Time: ${sub.exam_time}", color = DesignSystem.TextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = DesignSystem.Surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun FeePaymentDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    val fees by vm.studentFees.collectAsState()
    val isPaying by vm.isPayingFees.collectAsState()
    var paymentMode by remember { mutableStateOf("UPI") }
    var payAmountStr by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Validation state
    val payAmount = payAmountStr.toFloatOrNull()
    val totalDue = fees?.total_due ?: 0f
    val isOverpayment = payAmount != null && payAmount > totalDue
    val isInvalidAmount = payAmountStr.isNotEmpty() && (payAmount == null || payAmount <= 0f)

    LaunchedEffect(Unit) {
        vm.loadStudentFees()
    }

    // Confirmation dialog
    if (showConfirmation) {
        val confirmAmount = payAmount ?: totalDue
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmation = false
                        vm.payStudentFees(confirmAmount, paymentMode) {
                            android.widget.Toast.makeText(context, "Payment Processed successfully", android.widget.Toast.LENGTH_SHORT).show()
                            payAmountStr = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan)
                ) {
                    Text("Confirm Payment", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel", color = DesignSystem.TextMuted)
                }
            },
            title = { Text("Confirm Payment", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to pay ₹${String.format(Locale.US, "%.0f", confirmAmount)} via $paymentMode?",
                    color = DesignSystem.TextSecondary,
                    fontSize = 14.sp
                )
            },
            containerColor = DesignSystem.Surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (fees != null && fees!!.total_due > 0f) {
                Button(
                    onClick = { showConfirmation = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan),
                    enabled = !isPaying && !isOverpayment && !isInvalidAmount
                ) {
                    if (isPaying) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                    } else {
                        Text("Pay Dues", color = Color.Black)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = DesignSystem.TextSecondary)
            }
        },
        title = {
            Text("Fees & Payment Ledger", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            if (fees == null) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DesignSystem.Cyan)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DesignSystem.CardBg)
                            .border(androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Border), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tuition Fee Due", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                Text("₹${fees?.tuition_fee}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Examination Fee Due", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                Text("₹${fees?.exam_fee}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Registration & Other", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                Text("₹${fees?.other_fee}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Paid Aggregate", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                                Text("₹${fees?.paid_amount}", color = DesignSystem.Success, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = DesignSystem.Border)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Outstanding Balance", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("₹${fees?.total_due}", color = DesignSystem.Danger, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    if (fees!!.total_due > 0f) {
                        Text("Payment Setup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        
                        OutlinedTextField(
                            value = payAmountStr,
                            onValueChange = { payAmountStr = it },
                            modifier = Modifier.fillMaxWidth(),
                            isError = isOverpayment || isInvalidAmount,
                            supportingText = {
                                if (isOverpayment) {
                                    Text("Amount exceeds outstanding balance of ₹${String.format(Locale.US, "%.0f", totalDue)}", color = DesignSystem.Danger, fontSize = 10.sp)
                                } else if (isInvalidAmount) {
                                    Text("Enter a valid payment amount", color = DesignSystem.Danger, fontSize = 10.sp)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DesignSystem.Cyan,
                                unfocusedBorderColor = DesignSystem.Border,
                                errorBorderColor = DesignSystem.Danger,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            placeholder = { Text("Enter payment amount (Default: Net)", color = DesignSystem.TextMuted, fontSize = 11.sp) }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("UPI", "Credit Card", "Net Banking").forEach { mode ->
                                val isSelected = paymentMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) DesignSystem.Cyan.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) DesignSystem.Cyan else DesignSystem.Border), RoundedCornerShape(8.dp))
                                        .clickable { paymentMode = mode }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(mode, color = if (isSelected) DesignSystem.Cyan else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Text("Historical Receipts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    fees?.payment_history?.forEach { log ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DesignSystem.CardBg)
                                .border(androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Border), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(log.receipt_no, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Mode: ${log.payment_mode} • Date: ${log.payment_date}", color = DesignSystem.TextSecondary, fontSize = 9.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${log.amount}", color = DesignSystem.Success, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(log.status, color = DesignSystem.Success, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = DesignSystem.Surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun NotificationsDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    val notifications by vm.studentNotifications.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadStudentNotifications()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = DesignSystem.Cyan)
            }
        },
        title = {
            Text("Notification History Log", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            if (notifications.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No notifications received yet.", color = DesignSystem.TextMuted, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notif ->
                        val icon = when (notif.notification_type.lowercase()) {
                            "email" -> Icons.Default.Email
                            "sms" -> Icons.Default.Sms
                            else -> Icons.Default.Message
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DesignSystem.CardBg)
                                .border(androidx.compose.foundation.BorderStroke(1.dp, DesignSystem.Border), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(icon, null, tint = DesignSystem.Cyan, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(notif.trigger_event, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = notif.sent_at.substringBefore("T"),
                                        color = DesignSystem.TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(notif.message_content, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        containerColor = DesignSystem.Surface,
        shape = RoundedCornerShape(16.dp)
    )
}
