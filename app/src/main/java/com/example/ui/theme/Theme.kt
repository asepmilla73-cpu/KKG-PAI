package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = EmeraldDeep,
    primaryContainer = DarkEmeraldContainer,
    onPrimaryContainer = EmeraldContainer,
    secondary = DarkAmberAccent,
    onSecondary = DarkAmberContainer,
    secondaryContainer = DarkAmberContainer,
    onSecondaryContainer = AmberContainer,
    tertiary = InfoBlue,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFA7C4B5),
    outline = DarkOutline,
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF7F1D1D)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldDeep,
    secondary = AmberAccent,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = AmberDark,
    tertiary = InfoBlue,
    onTertiary = Color.White,
    tertiaryContainer = InfoContainer,
    onTertiaryContainer = Color(0xFF075985),
    background = SurfaceBackground,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = OutlineSubtle,
    outlineVariant = OutlineStrong,
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = ExpenseContainer,
    onErrorContainer = Color(0xFF991B1B)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun SimKkgTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
