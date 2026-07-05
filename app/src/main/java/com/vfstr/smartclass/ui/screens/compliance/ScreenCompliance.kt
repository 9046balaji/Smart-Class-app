package com.vfstr.smartclass.ui.screens.compliance

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
import com.vfstr.smartclass.ui.MainViewModel
import com.vfstr.smartclass.ui.components.GlassmorphicCard
import com.vfstr.smartclass.ui.navigation.Navigation
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.vfstr.smartclass.ui.theme.DesignSystem

import androidx.compose.material.icons.automirrored.filled.ArrowForward

@Composable
fun ScreenCompliance(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("Cycles") } // Cycles, Defaulters, Eligibility

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
                text = "R22 Compliance Management",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Review cycles and eligibility monitoring", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DesignSystem.Surface)
                    .padding(4.dp)
            ) {
                listOf("Cycles", "Defaulters", "Eligibility").forEach { tab ->
                    val isSel = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) DesignSystem.Cyan.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { activeTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) DesignSystem.Cyan else DesignSystem.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (activeTab) {
                "Cycles" -> ReviewCyclesList(vm)
                "Defaulters" -> DefaultersList(vm)
                "Eligibility" -> EligibilityOverview()
            }
        }
    }
}

@Composable
fun ReviewCyclesList(vm: MainViewModel) {
    val cycles = listOf(
        Pair("Week 4", "Completed"),
        Pair("Week 8", "Completed"),
        Pair("Week 12", "Active"),
        Pair("Week 15", "Scheduled")
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Semester Review Cycles", color = Color.White, fontWeight = FontWeight.Bold)
            Button(
                onClick = { /* Run Review */ },
                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Violet),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Run Week 12 Process", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        cycles.forEach { (week, status) ->
            GlassmorphicCard(
                modifier = Modifier.clickable { 
                    if (status == "Active" || status == "Completed") {
                        vm.currentRoute.value = Navigation.ROUTE_COMPLIANCE_DETAILED 
                    }
                }
            ) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (status == "Completed") DesignSystem.Success.copy(alpha = 0.2f) else if (status == "Active") DesignSystem.Cyan.copy(alpha = 0.2f) else DesignSystem.Surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(week.split(" ")[1], color = if (status == "Scheduled") DesignSystem.TextMuted else if (status == "Active") DesignSystem.Cyan else DesignSystem.Success, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(week, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(if (status == "Completed") "Reviewed by Admin Root" else if (status == "Active") "In progress - 84% done" else "Upcoming period", fontSize = 11.sp, color = DesignSystem.TextMuted)
                    }
                    if (status == "Active" || status == "Completed") {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = DesignSystem.Cyan, modifier = Modifier.size(20.dp))
                    } else {
                        StatusBadge(status)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Completed" -> DesignSystem.Success
        "Active" -> DesignSystem.Cyan
        else -> DesignSystem.TextMuted
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = status.uppercase(), fontSize = 9.sp, color = color, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun DefaultersList(vm: MainViewModel) {
    val defaulters = (1..10).map { "22L11A050$it" to "Student Name $it" }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Attendance Defaulters List (< 75%)", color = Color.White, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(defaulters) { (roll, name) ->
                GlassmorphicCard {
                    Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(DesignSystem.CardBg), contentAlignment = Alignment.Center) {
                            Text(name.take(1), color = DesignSystem.Danger, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(roll, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                        }
                        Text("${(60..74).random()}.${(0..9).random()}%", color = DesignSystem.Danger, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Spacer(Modifier.width(12.dp))
                        IconButton(onClick = { /* Notify */ }) {
                            Icon(Icons.Default.NotificationAdd, null, tint = DesignSystem.Warning, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EligibilityOverview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatItem("Eligible", "142", DesignSystem.Success, Modifier.weight(1f))
            StatItem("Conditional", "28", DesignSystem.Warning, Modifier.weight(1f))
            StatItem("Barred", "15", DesignSystem.Danger, Modifier.weight(1f))
        }
        
        Text("Batch Progression Trend", color = Color.White, fontWeight = FontWeight.Bold)
        GlassmorphicCard {
            Column(Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Overall Section A Mean", color = DesignSystem.TextSecondary, fontSize = 12.sp)
                    Text("82.1%", color = DesignSystem.Cyan, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.821f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = DesignSystem.Cyan,
                    trackColor = DesignSystem.Border
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    GlassmorphicCard(modifier = modifier) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = DesignSystem.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
