package com.vfstr.smartclass.ui.screens.audit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun ScreenArchival(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DesignSystem.Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(DesignSystem.Padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Semester-End Archival Console",
                    style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(text = "Manage data retention, database optimization, and cold storage", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassmorphicCard(modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Active DB Size", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("42.8 GB", color = DesignSystem.Cyan, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    GlassmorphicCard(modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Archive DB Size", color = DesignSystem.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("184.5 GB", color = DesignSystem.Success, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            item {
                GlassmorphicCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Run Clean-up Operations", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Migrate session logs older than 180 days to cold storage storage tables.", color = DesignSystem.TextSecondary, fontSize = 11.sp)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { /* Run dry run */ },
                                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Surface, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Dry Run", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { /* Run archival */ },
                                colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Danger),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Trigger Archival", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            item {
                Text("Archival History Logs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                listOf(
                    Pair("2026-06-15", "Completed: R21 B.Tech Batch Archival - 2.4M rows migrated"),
                    Pair("2025-12-20", "Completed: Semester 1 Session Logs Purge - 1.8M rows migrated"),
                    Pair("2025-06-10", "Completed: Old Override Logs Compression - 450k rows archived")
                ).forEach { (date, status) ->
                    GlassmorphicCard(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(date, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(status, color = DesignSystem.TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
