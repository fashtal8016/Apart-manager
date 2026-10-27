package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Face 1: Classic Management Colors (Navy, Slate, Crisp White)
val ClassicPrimary = Color(0xFF1E3A8A)
val ClassicSecondary = Color(0xFF3B82F6)
val ClassicTertiary = Color(0xFF475569)
val ClassicBackground = Color(0xFFF8FAFC)
val ClassicSurface = Color(0xFFFFFFFF)
val ClassicSurfaceVariant = Color(0xFFF1F5F9)

// Face 2: Modern & Luxury Colors (Dark Charcoal, Soft Gold, Premium White)
val LuxuryPrimary = Color(0xFFD4AF37)
val LuxurySecondary = Color(0xFFE5C158)
val LuxuryTertiary = Color(0xFF94A3B8)
val LuxuryBackground = Color(0xFF0F172A)
val LuxurySurface = Color(0xFF1E293B)
val LuxurySurfaceVariant = Color(0xFF334155)

// Face 3: Minimal & Fast Colors (Clean Slate, Indigo/Mint, Airy)
val MinimalPrimary = Color(0xFF2563EB)
val MinimalSecondary = Color(0xFF10B981)
val MinimalTertiary = Color(0xFF64748B)
val MinimalBackground = Color(0xFFF9FAFB)
val MinimalSurface = Color(0xFFFFFFFF)
val MinimalSurfaceVariant = Color(0xFFF3F4F6)

// Face 4: Professional Financial Colors (Emerald Green, Teal, Deep Slate)
val FinancePrimary = Color(0xFF047857)
val FinanceSecondary = Color(0xFF0D9488)
val FinanceTertiary = Color(0xFF059669)
val FinanceBackground = Color(0xFFF0FDF4)
val FinanceSurface = Color(0xFFFFFFFF)
val FinanceSurfaceVariant = Color(0xFFDCFCE7)

// 10 Color Accent Presets
data class AccentPreset(val name: String, val primary: Color, val hex: String)

val AccentPresets = listOf(
    AccentPreset("سرمه‌ای کلاسیک", Color(0xFF1E3A8A), "#1E3A8A"),
    AccentPreset("طلایی لوکس", Color(0xFFD4AF37), "#D4AF37"),
    AccentPreset("سبز مالی زمردی", Color(0xFF047857), "#047857"),
    AccentPreset("آبی فیروزه‌ای", Color(0xFF0284C7), "#0284C7"),
    AccentPreset("تیل اقیانوسی", Color(0xFF0D9488), "#0D9488"),
    AccentPreset("بنفش سلطنتی", Color(0xFF7C3AED), "#7C3AED"),
    AccentPreset("یاقوتی تیره", Color(0xFFBE123C), "#BE123C"),
    AccentPreset("کهربایی گرم", Color(0xFFD97706), "#D97706"),
    AccentPreset("زیتونی مدرن", Color(0xFF4D7C0F), "#4D7C0F"),
    AccentPreset("دودی تیتانیومی", Color(0xFF334155), "#334155")
)

fun getFaceColorScheme(faceId: Int, isDark: Boolean, customAccent: Color? = null): ColorScheme {
    val accent = customAccent ?: when (faceId) {
        1 -> ClassicPrimary
        2 -> LuxuryPrimary
        3 -> MinimalPrimary
        4 -> FinancePrimary
        else -> ClassicPrimary
    }

    return when (faceId) {
        2 -> { // Luxury Face (Default Dark/Rich)
            darkColorScheme(
                primary = accent,
                onPrimary = Color.Black,
                primaryContainer = Color(0xFF2E2608),
                onPrimaryContainer = Color(0xFFFDE047),
                secondary = LuxurySecondary,
                onSecondary = Color.Black,
                background = LuxuryBackground,
                onBackground = Color(0xFFF8FAFC),
                surface = LuxurySurface,
                onSurface = Color(0xFFF8FAFC),
                surfaceVariant = LuxurySurfaceVariant,
                onSurfaceVariant = Color(0xFFCBD5E1)
            )
        }
        3 -> { // Minimal Face
            if (isDark) {
                darkColorScheme(
                    primary = accent,
                    background = Color(0xFF111827),
                    surface = Color(0xFF1F2937),
                    surfaceVariant = Color(0xFF374151)
                )
            } else {
                lightColorScheme(
                    primary = accent,
                    onPrimary = Color.White,
                    background = MinimalBackground,
                    surface = MinimalSurface,
                    surfaceVariant = MinimalSurfaceVariant
                )
            }
        }
        4 -> { // Financial Face
            if (isDark) {
                darkColorScheme(
                    primary = accent,
                    background = Color(0xFF062016),
                    surface = Color(0xFF0E3827),
                    surfaceVariant = Color(0xFF144D36),
                    secondary = FinanceSecondary
                )
            } else {
                lightColorScheme(
                    primary = accent,
                    onPrimary = Color.White,
                    background = Color(0xFFF7FDF9),
                    surface = Color.White,
                    surfaceVariant = Color(0xFFE8F5E9),
                    secondary = FinanceSecondary
                )
            }
        }
        else -> { // Classic Management Face (1)
            if (isDark) {
                darkColorScheme(
                    primary = accent,
                    background = Color(0xFF0F172A),
                    surface = Color(0xFF1E293B),
                    surfaceVariant = Color(0xFF334155)
                )
            } else {
                lightColorScheme(
                    primary = accent,
                    onPrimary = Color.White,
                    background = ClassicBackground,
                    surface = ClassicSurface,
                    surfaceVariant = ClassicSurfaceVariant
                )
            }
        }
    }
}
