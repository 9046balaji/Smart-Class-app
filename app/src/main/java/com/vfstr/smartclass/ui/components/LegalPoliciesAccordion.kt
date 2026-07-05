package com.vfstr.smartclass.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vfstr.smartclass.ui.theme.DesignSystem

data class PolicyItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
)

@Composable
fun LegalPoliciesAccordion(
    modifier: Modifier = Modifier
) {
    val policies = listOf(
        PolicyItem(
            "Biometric Processing Consent",
            "I understand that VFSTR SmartClass transforms facial images into mathematical feature vectors. This data is used strictly for academic monitoring and matching against my enrollment status. No raw images are stored permanently after indexing.",
            Icons.Default.Security,
            DesignSystem.Success
        ),
        PolicyItem(
            "Data Privacy & DPDP Act",
            "I acknowledge that my personal identifiers (Roll Number, Email, Phone) are protected under VFSTR privacy protocols. Client-side diagnostics are obfuscated using FNV1a algorithms to ensure data privacy during transmission.",
            Icons.Default.Shield,
            DesignSystem.Cyan
        ),
        PolicyItem(
            "Academic Integrity Policy",
            "I am aware that the system tracks device parameters and location accuracy to prevent spoofing. Attempts to manipulate GPS or use suspiciously fast movement (>15 km/h) will be flagged in the audit logs for administrative review.",
            Icons.Default.Gavel,
            DesignSystem.Warning
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignSystem.CornerRadius))
            .background(DesignSystem.Surface)
    ) {
        policies.forEachIndexed { index, policy ->
            PolicyRow(policy)
            if (index < (policies.size - 1)) {
                HorizontalDivider(color = DesignSystem.Border, thickness = 1.dp)
            }
        }
    }
}

@Preview
@Composable
fun LegalPoliciesAccordionPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        LegalPoliciesAccordion()
    }
}

@Composable
fun PolicyRow(policy: PolicyItem) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = policy.icon,
                    contentDescription = null,
                    tint = policy.color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = policy.title,
                    color = DesignSystem.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = DesignSystem.TextSecondary
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = policy.description,
                    color = DesignSystem.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
