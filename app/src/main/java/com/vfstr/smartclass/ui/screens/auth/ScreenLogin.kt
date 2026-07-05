package com.vfstr.smartclass.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.*
import com.vfstr.smartclass.ui.theme.DesignSystem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenLogin(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("staff") } // staff, student
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    var rollNum by remember { mutableStateOf("") }
    var selectedDob by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val authErrMsg by vm.authError.collectAsState()
    val authLoadingState by vm.authLoading.collectAsState()
    val hasBiometric by vm.hasBiometricToken.collectAsState()
    
    // DEBUG LOG
    LaunchedEffect(hasBiometric) {
        android.util.Log.d("ScreenLogin", "hasBiometricToken: $hasBiometric")
    }

    val context = LocalContext.current

    var cardEntered by remember { mutableStateOf(false) }
    val entryOffset by animateDpAsState(
        targetValue = if (cardEntered) 0.dp else 600.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(Unit) {
        cardEntered = true
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDob ?: Instant.now().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDob = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("OK", color = DesignSystem.Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = DesignSystem.TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DesignSystem.Background),
        contentAlignment = Alignment.Center
    ) {
        // Combined Premium Background
        MeshBackground(modifier = Modifier.fillMaxSize())
        ParticleBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.PaddingLarge)
                .offset(y = entryOffset),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(DesignSystem.VignanBlue, RoundedCornerShape(22.dp))
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(22.dp), spotColor = DesignSystem.VignanBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VF",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SmartClass",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Cyan,
                    letterSpacing = 1.2.sp
                )
            )
            Text(
                text = "VFSTR University • Portals v4.2",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DesignSystem.TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    TabItem(
                        label = "Staff Login",
                        isActive = activeTab == "staff",
                        activeColor = DesignSystem.Violet,
                        modifier = Modifier.weight(1f)
                    ) { activeTab = "staff" }
                    
                    TabItem(
                        label = "Student Login",
                        isActive = activeTab == "student",
                        activeColor = DesignSystem.Cyan,
                        modifier = Modifier.weight(1f)
                    ) { activeTab = "student" }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Crossfade(targetState = activeTab, label = "tab_switches") { tab ->
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (authErrMsg != null) DesignSystem.Danger else DesignSystem.Border,
                    glowColor = if (authErrMsg != null) DesignSystem.Danger.copy(alpha = 0.2f) else Color.Transparent
                ) {
                    Column(modifier = Modifier.padding(DesignSystem.PaddingLarge)) {
                        if (tab == "staff") {
                            InputFieldLabel("Username / Faculty Email")
                            TextField(
                                value = username,
                                onValueChange = { username = it },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DesignSystem.Cyan) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0x0AFFFFFF),
                                    unfocusedContainerColor = Color(0x0AFFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = DesignSystem.Cyan
                                ),
                                placeholder = { Text("rao", color = DesignSystem.TextMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("staff_username_input")
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            InputFieldLabel("Password / Pin")
                            TextField(
                                value = password,
                                onValueChange = { password = it },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DesignSystem.Cyan) },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = DesignSystem.TextSecondary
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0x0AFFFFFF),
                                    unfocusedContainerColor = Color(0x0AFFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = DesignSystem.Cyan
                                ),
                                placeholder = { Text("••••", color = DesignSystem.TextMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("staff_password_input")
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Switch(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = DesignSystem.Violet, checkedTrackColor = DesignSystem.Violet.copy(alpha = 0.3f))
                                )
                                Spacer(modifier = Modifier.width(DesignSystem.SpacingMedium))
                                Text(
                                    text = "Stay authorized on this device",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = DesignSystem.TextSecondary)
                                )
                            }
                        } else {
                            InputFieldLabel("Student Roll Number")
                            TextField(
                                value = rollNum,
                                onValueChange = { rollNum = it.uppercase() },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = DesignSystem.Violet) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0x0AFFFFFF),
                                    unfocusedContainerColor = Color(0x0AFFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = DesignSystem.Violet
                                ),
                                placeholder = { Text("22L11A0501", color = DesignSystem.TextMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("student_roll_input")
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            InputFieldLabel("Date of Birth")
                            Surface(
                                onClick = { showDatePicker = true },
                                color = Color(0x0AFFFFFF),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = DesignSystem.Violet, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = if (selectedDob != null) {
                                            Instant.ofEpochMilli(selectedDob!!)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
                                                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                        } else {
                                            "Select date to verify..."
                                        },
                                        color = if (selectedDob != null) Color.White else DesignSystem.TextMuted,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        authErrMsg?.let { err ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Verification Failed: $err",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DesignSystem.Danger,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (activeTab == "staff") {
                                        vm.loginStaff(username, password)
                                    } else {
                                        val dobStr = if (selectedDob != null) {
                                            Instant.ofEpochMilli(selectedDob!!)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
                                                .format(DateTimeFormatter.ISO_DATE)
                                        } else ""
                                        vm.loginStudent(rollNum.trim(), dobStr)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("login_submit_button"),
                                enabled = !authLoadingState,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(DesignSystem.CornerRadius)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(DesignSystem.Cyan, DesignSystem.Violet)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (authLoadingState) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                                    } else {
                                        Text(
                                            text = "Authorize & Enter",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = hasBiometric,
                                enter = expandHorizontally() + fadeIn(),
                                exit = shrinkHorizontally() + fadeOut()
                            ) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(DesignSystem.CornerRadius))
                                        .background(DesignSystem.Cyan.copy(alpha = 0.1f))
                                        .border(1.dp, DesignSystem.Cyan.copy(alpha = 0.3f), RoundedCornerShape(DesignSystem.CornerRadius))
                                        .clickable {
                                            val activity = context as? FragmentActivity
                                            if (activity != null) {
                                                vm.initiateBiometricLogin(activity)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Biometric Login",
                                        tint = DesignSystem.Cyan,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "VIGNAN UNIVERSITY SECURE TERMINAL",
                style = MaterialTheme.typography.labelSmall.copy(color = DesignSystem.TextMuted, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun TabItem(label: String, isActive: Boolean, activeColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(DesignSystem.CornerRadius))
            .background(if (isActive) activeColor.copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (isActive) Color.White else DesignSystem.TextSecondary
            )
        )
    }
}

@Composable
fun InputFieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            color = DesignSystem.TextSecondary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
