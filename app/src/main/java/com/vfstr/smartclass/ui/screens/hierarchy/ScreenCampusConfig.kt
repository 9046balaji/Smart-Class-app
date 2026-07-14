package com.vfstr.smartclass.ui.screens.hierarchy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.theme.DesignSystem

data class CampusNode(
    val id: String,
    val name: String,
    val code: String,
    val departments: Int,
    val status: String
)

@Composable
fun ScreenCampusConfig(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val campuses = listOf(
        CampusNode("1", "Vadlamudi Main Campus", "VFSTR-VAD", 14, "Active"),
        CampusNode("2", "Hyderabad Ext Center", "VFSTR-HYD", 4, "Active"),
        CampusNode("3", "Vizag Extension", "VFSTR-VSKP", 2, "Proposed")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(DesignSystem.Padding)
        ) {
            Text(
                text = "Campus Namespace Configurator",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Manage university campus nodes, departments and school trees", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(campuses) { item ->
                    GlassmorphicCard {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Code: ${item.code} | Departments: ${item.departments}", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (item.status == "Active") DesignSystem.Success.copy(alpha = 0.1f) else DesignSystem.Cyan.copy(alpha = 0.1f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(item.status.uppercase(), fontSize = 9.sp, color = if (item.status == "Active") DesignSystem.Success else DesignSystem.Cyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
