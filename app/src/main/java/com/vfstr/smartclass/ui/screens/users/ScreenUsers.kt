package com.vfstr.smartclass.ui.screens.users

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
import com.vfstr.smartclass.domain.models.User
import com.vfstr.smartclass.domain.models.UserRole
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

@Composable
fun ScreenUsers(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val users = listOf(
        User("1", "admin_rao", "Dr. Rao", UserRole.admin, "CSAI", "rao@vignan.ac.in", emptyList(), true),
        User("2", "superadmin", "System Root", UserRole.superadmin, "IT", "root@vignan.ac.in", emptyList(), true),
        User("3", "faculty_vani", "Mrs. Vani", UserRole.faculty, "CSAI", "vani@vignan.ac.in", emptyList(), true),
        User("4", "viewer_guest", "External Auditor", UserRole.viewer, null, "guest@auditor.com", emptyList(), false)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background,
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Add user */ }, containerColor = DesignSystem.Cyan) {
                Icon(Icons.Default.PersonAdd, null, tint = Color.Black)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(DesignSystem.Padding)
        ) {
            Text(
                text = "Administrative Access",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Manage system users and role permissions", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(users) { user ->
                    UserCard(user)
                }
            }
        }
    }
}

@Composable
fun UserCard(user: User) {
    val roleColor = when (user.role) {
        UserRole.superadmin -> DesignSystem.RoleSuperAdmin
        UserRole.admin -> DesignSystem.RoleAdmin
        UserRole.faculty -> DesignSystem.RoleFaculty
        UserRole.student -> DesignSystem.RoleStudent
        UserRole.viewer -> DesignSystem.RoleViewer
    }

    GlassmorphicCard {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(roleColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Text(user.displayName.take(1), color = roleColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(user.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("@${user.username} • ${user.department ?: "System Admin"}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(roleColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(user.role.name.uppercase(), fontSize = 9.sp, color = roleColor, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(8.dp))
                Switch(
                    checked = user.isActive,
                    onCheckedChange = { /* Toggle */ },
                    colors = SwitchDefaults.colors(checkedThumbColor = DesignSystem.Success, checkedTrackColor = DesignSystem.Success.copy(alpha = 0.3f))
                )
            }
        }
    }
}
