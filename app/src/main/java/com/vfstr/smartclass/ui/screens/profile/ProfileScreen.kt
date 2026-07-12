package com.vfstr.smartclass.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.domain.models.UserRole
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.components.MeshBackground
import com.vfstr.smartclass.ui.theme.DesignSystem

@Composable
fun ProfileScreen(vm: MainViewModel) {
    val currentRole by vm.currentRole.collectAsState()
    val staffProfile by vm.staffProfile.collectAsState()
    val studentProfile by vm.studentProfile.collectAsState()
    
    val localCgpa by vm.localCgpa.collectAsState()
    val localLastYearSubjects by vm.localLastYearSubjects.collectAsState()
    val activeRoleContext by vm.activeRoleContext.collectAsState()
    val studentMentor by vm.studentMentor.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadProfile()
        if (vm.currentRole.value == UserRole.student) {
            vm.loadStudentMentor()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(DesignSystem.Padding),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.SpacingLarge)
        ) {
            item {
                ProfileHeader(
                    name = if (currentRole == UserRole.student) studentProfile?.name ?: "" else staffProfile?.displayName ?: "",
                    id = if (currentRole == UserRole.student) studentProfile?.rollNo ?: "" else staffProfile?.username ?: "",
                    role = currentRole
                )
            }

            if (currentRole == UserRole.student) {
                item {
                    StudentDetailsSection(studentProfile)
                }
                item {
                    MentorDetailsSection(studentMentor)
                }
                item {
                    StudentInteractiveCards(
                        cgpa = localCgpa,
                        subjects = localLastYearSubjects,
                        onCgpaChange = { vm.updateCgpa(it) },
                        onSubjectsChange = { vm.updateLastYearSubjects(it) }
                    )
                }
            } else {
                item {
                    ContextSwitchingPanel(
                        currentRole = currentRole,
                        activeContext = activeRoleContext,
                        onContextChange = { vm.updateActiveRoleContext(it) }
                    )
                }
                item {
                    StaffDetailsSection(staffProfile)
                }
                item {
                    PermissionsMatrix(staffProfile?.permissions ?: emptyList())
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(name: String, id: String, role: UserRole?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(DesignSystem.CardBg)
                .border(2.dp, DesignSystem.Cyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = DesignSystem.Cyan)
        }
        
        Spacer(modifier = Modifier.height(DesignSystem.SpacingMedium))
        
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = DesignSystem.TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Text(
            text = id,
            style = MaterialTheme.typography.bodyMedium.copy(color = DesignSystem.TextSecondary)
        )
        
        Spacer(modifier = Modifier.height(DesignSystem.SpacingSmall))
        
        RoleBadge(role)
    }
}

@Composable
fun RoleBadge(role: UserRole?) {
    val (color, text) = when (role) {
        UserRole.superadmin -> DesignSystem.RoleSuperAdmin to "SUPER ADMIN"
        UserRole.admin -> DesignSystem.RoleAdmin to "ADMIN"
        UserRole.faculty -> DesignSystem.RoleFaculty to "FACULTY"
        UserRole.viewer -> DesignSystem.RoleViewer to "VIEWER"
        UserRole.student -> DesignSystem.RoleStudent to "STUDENT"
        else -> DesignSystem.TextMuted to "UNKNOWN"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ContextSwitchingPanel(
    currentRole: UserRole?,
    activeContext: UserRole?,
    onContextChange: (UserRole?) -> Unit
) {
    if (currentRole == UserRole.superadmin || currentRole == UserRole.admin) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "PERSPECTIVE SWITCHER",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = DesignSystem.Violet,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = DesignSystem.SpacingSmall)
            )
            
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(DesignSystem.Padding)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (activeContext == UserRole.faculty) "Faculty Mode" else "Administrative Mode",
                            style = MaterialTheme.typography.bodyLarge.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (activeContext == UserRole.faculty) "Simplifying UI for classroom focus." else "Full system access enabled.",
                            style = MaterialTheme.typography.labelSmall.copy(color = DesignSystem.TextSecondary)
                        )
                    }
                    
                    Switch(
                        checked = activeContext == UserRole.faculty,
                        onCheckedChange = { isFaculty ->
                            onContextChange(if (isFaculty) UserRole.faculty else null)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DesignSystem.Violet,
                            checkedTrackColor = DesignSystem.Violet.copy(alpha = 0.3f),
                            uncheckedThumbColor = DesignSystem.TextSecondary,
                            uncheckedTrackColor = DesignSystem.Border
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StaffDetailsSection(profile: com.vfstr.smartclass.domain.models.User?) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(DesignSystem.Padding)) {
            DetailRow(icon = Icons.Default.Email, label = "Email", value = profile?.email ?: "Not set")
            HorizontalDivider(modifier = Modifier.padding(vertical = DesignSystem.SpacingSmall), color = DesignSystem.Border)
            DetailRow(icon = Icons.Default.Business, label = "Department", value = profile?.department ?: "Not set")
            HorizontalDivider(modifier = Modifier.padding(vertical = DesignSystem.SpacingSmall), color = DesignSystem.Border)
            DetailRow(icon = Icons.Default.VerifiedUser, label = "Status", value = if (profile?.isActive == true) "Active" else "Inactive")
        }
    }
}

@Composable
fun StudentDetailsSection(profile: com.vfstr.smartclass.domain.models.Student?) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(DesignSystem.Padding)) {
            DetailRow(icon = Icons.Default.Email, label = "Email", value = profile?.email ?: "Not set")
            HorizontalDivider(modifier = Modifier.padding(vertical = DesignSystem.SpacingSmall), color = DesignSystem.Border)
            DetailRow(icon = Icons.Default.Business, label = "Department", value = profile?.department ?: "Not set")
            HorizontalDivider(modifier = Modifier.padding(vertical = DesignSystem.SpacingSmall), color = DesignSystem.Border)
            DetailRow(icon = Icons.Default.CalendarToday, label = "Year & Section", value = "${profile?.year ?: ""} Year, Sec ${profile?.section ?: ""}")
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, tint = DesignSystem.TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(DesignSystem.SpacingMedium))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = DesignSystem.TextMuted))
            Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PermissionsMatrix(permissions: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SYSTEM AUTHORIZATIONS",
            style = MaterialTheme.typography.labelSmall.copy(
                color = DesignSystem.Cyan,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(bottom = DesignSystem.SpacingSmall)
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            permissions.forEach { perm ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DesignSystem.CardBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = perm, color = DesignSystem.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            if (permissions.isEmpty()) {
                Text(text = "No specific permissions assigned.", color = DesignSystem.TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

@Composable
fun StudentInteractiveCards(
    cgpa: Float,
    subjects: String,
    onCgpaChange: (Float) -> Unit,
    onSubjectsChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(DesignSystem.SpacingLarge)) {
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = DesignSystem.Cyan.copy(alpha = 0.3f),
            glowColor = DesignSystem.Cyan.copy(alpha = 0.1f)
        ) {
            Column(modifier = Modifier.padding(DesignSystem.Padding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = DesignSystem.Cyan)
                    Spacer(modifier = Modifier.width(DesignSystem.SpacingSmall))
                    Text(text = "CGPA (LOCAL)", style = MaterialTheme.typography.titleMedium.copy(color = DesignSystem.Cyan, fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(DesignSystem.SpacingSmall))
                TextField(
                    value = if (cgpa == 0f) "" else cgpa.toString(),
                    onValueChange = { 
                        it.toFloatOrNull()?.let { valCgpa ->
                            if (valCgpa <= 10.0) onCgpaChange(valCgpa)
                        } ?: run { if (it.isEmpty()) onCgpaChange(0f) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = DesignSystem.TextPrimary,
                        unfocusedTextColor = DesignSystem.TextPrimary,
                        cursorColor = DesignSystem.Cyan,
                        focusedIndicatorColor = DesignSystem.Cyan,
                        unfocusedIndicatorColor = DesignSystem.Border
                    ),
                    placeholder = { Text("Enter current CGPA (e.g. 8.5)", color = DesignSystem.TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }

        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = DesignSystem.Violet.copy(alpha = 0.3f),
            glowColor = DesignSystem.Violet.copy(alpha = 0.1f)
        ) {
            Column(modifier = Modifier.padding(DesignSystem.Padding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Book, contentDescription = null, tint = DesignSystem.Violet)
                    Spacer(modifier = Modifier.width(DesignSystem.SpacingSmall))
                    Text(text = "LAST-YEAR SUBJECTS", style = MaterialTheme.typography.titleMedium.copy(color = DesignSystem.Violet, fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(DesignSystem.SpacingSmall))
                TextField(
                    value = subjects,
                    onValueChange = onSubjectsChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = DesignSystem.TextPrimary,
                        unfocusedTextColor = DesignSystem.TextPrimary,
                        cursorColor = DesignSystem.Violet,
                        focusedIndicatorColor = DesignSystem.Violet,
                        unfocusedIndicatorColor = DesignSystem.Border
                    ),
                    placeholder = { Text("List subjects (e.g. AI, ML, OS)", color = DesignSystem.TextMuted) }
                )
            }
        }
    }
}

@Composable
fun MentorDetailsSection(mentor: com.vfstr.smartclass.data.remote.api.MentorDto?) {
    if (mentor == null) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ACADEMIC ADVISOR & COUNSELOR",
            style = MaterialTheme.typography.labelSmall.copy(
                color = DesignSystem.Cyan,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(bottom = DesignSystem.SpacingSmall)
        )
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DesignSystem.Cyan.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, null, tint = DesignSystem.Cyan, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(mentor.mentor_name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(mentor.department, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = DesignSystem.Border)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DesignSystem.CardBg)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, null, tint = DesignSystem.Cyan, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(mentor.mentor_email, color = Color.White, fontSize = 10.sp, maxLines = 1)
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DesignSystem.CardBg)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, null, tint = DesignSystem.Cyan, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(mentor.mentor_phone, color = Color.White, fontSize = 10.sp, maxLines = 1)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Office Hours: " + mentor.office_hours,
                    color = DesignSystem.TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
