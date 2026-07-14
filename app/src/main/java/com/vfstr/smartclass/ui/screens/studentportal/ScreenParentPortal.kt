package com.vfstr.smartclass.ui.screens.studentportal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.vfstr.smartclass.ui.theme.DesignSystem

@Composable
fun ScreenParentPortal(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    var otpSimulated by remember { mutableStateOf(false) }

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
                text = "Parent Portal Access",
                style = MaterialTheme.typography.headlineSmall.copy(color = DesignSystem.TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(text = "Read-only access portal configuration and OTP verification", color = DesignSystem.TextSecondary, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            if (!otpSimulated) {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Configure Parent OTP Verification", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Enter the parent mobile number to register or request verification pin.", color = DesignSystem.TextSecondary, fontSize = 12.sp)

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { otpSimulated = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Cyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simulate OTP Verification", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                    item {
                        GlassmorphicCard {
                            Column(Modifier.padding(16.dp)) {
                                Text("Student Academic Summary", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(Modifier.height(12.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Student Name:", color = DesignSystem.TextSecondary, fontSize = 12.sp)
                                    Text("K. Sunil Kumar (22L11A0512)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Current Attendance:", color = DesignSystem.TextSecondary, fontSize = 12.sp)
                                    Text("82.5%", color = DesignSystem.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Status:", color = DesignSystem.TextSecondary, fontSize = 12.sp)
                                    Text("ELIGIBLE", color = DesignSystem.Success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { otpSimulated = false },
                            colors = ButtonDefaults.buttonColors(containerColor = DesignSystem.Surface, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset Parent Session", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
