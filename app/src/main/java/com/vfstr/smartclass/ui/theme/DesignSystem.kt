package com.vfstr.smartclass.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object DesignSystem {
    // Primary Color Palette (Rule 8 & 23)
    val Background = Color(0xFF0A0F1E)
    val Surface = Color(0xFF111827)
    val CardBg = Color(0x0DFFFFFF)
    val Cyan = Color(0xFF00D4FF)
    val Violet = Color(0xFF7C3AED)
    val VignanBlue = Color(0xFF003087)
    val Success = Color(0xFF10B981) // Green
    val Warning = Color(0xFFF59E0B) // Amber
    val Danger = Color(0xFFEF4444)  // Red
    
    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF475569)
    val Border = Color(0x14FFFFFF)

    // Neon Glow Variants (New for Premium UI)
    val CyanGlow = Color(0x4000D4FF)
    val VioletGlow = Color(0x407C3AED)
    val SuccessGlow = Color(0x4010B981)
    val DangerGlow = Color(0x40EF4444)
    val WarningGlow = Color(0x40F59E0B)

    // Platform Colors (Screen 17)
    val PlatformSwayam = Color(0xFFF59E0B)
    val PlatformNptel = Color(0xFF003087)
    val PlatformCoursera = Color(0xFF00D4FF)
    val PlatformOther = Color(0xFF94A3B8)

    // Role Colors (Screen 18)
    val RoleSuperAdmin = Color(0xFFEF4444)
    val RoleAdmin = Color(0xFFF97316)
    val RoleFaculty = Color(0xFF00D4FF)
    val RoleViewer = Color(0xFF6B7280)
    val RoleStudent = Color(0xFF10B981)

    // Visual Language
    val CornerRadius = 16.dp
    val CornerRadiusLarge = 24.dp
    val Padding = 16.dp
    val PaddingLarge = 24.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 12.dp
    val SpacingLarge = 20.dp
}
