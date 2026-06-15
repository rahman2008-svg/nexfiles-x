package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GlassBorder,
    borderWidth: Float = 1f,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = modifier
        .clip(RoundedCornerShape(32.dp))
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x2627272A), // Premium Zinc-800 Glass Gradient
                    Color(0x1418181B)  // Zinc-900 Glass Gradient
                )
            )
        )
        .border(
            width = borderWidth.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    borderColor,
                    borderColor.copy(alpha = 0.2f)
                )
            ),
            shape = RoundedCornerShape(32.dp)
        )
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Column(
        modifier = baseModifier.padding(20.dp),
        content = content
    )
}

@Composable
fun StorageDonutChart(
    photos: Long,
    videos: Long,
    audio: Long,
    docs: Long,
    apks: Long,
    zips: Long,
    free: Long,
    modifier: Modifier = Modifier
) {
    val total = photos + videos + audio + docs + apks + zips + free
    if (total <= 0) return

    val pAng = (photos.toFloat() / total) * 360f
    val vAng = (videos.toFloat() / total) * 360f
    val aAng = (audio.toFloat() / total) * 360f
    val dAng = (docs.toFloat() / total) * 360f
    val apAng = (apks.toFloat() / total) * 360f
    val zAng = (zips.toFloat() / total) * 360f
    val fAng = (free.toFloat() / total) * 360f

    val strokeWidth = 32f

    Canvas(modifier = modifier) {
        val innerSize = size.minDimension - strokeWidth
        val topLeftOffset = Offset(
            (size.width - innerSize) / 2,
            (size.height - innerSize) / 2
        )
        val canvasSize = Size(innerSize, innerSize)

        var startAngle = -90f

        // Draw sectors
        drawArc(
            color = CyberBlue,
            startAngle = startAngle,
            sweepAngle = pAng,
            useCenter = false,
            topLeft = topLeftOffset,
            size = canvasSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += pAng

        drawArc(
            color = ActiveGreen,
            startAngle = startAngle,
            sweepAngle = vAng,
            useCenter = false,
            topLeft = topLeftOffset,
            size = canvasSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += vAng

        drawArc(
            color = DeepPurple,
            startAngle = startAngle,
            sweepAngle = aAng,
            useCenter = false,
            topLeft = topLeftOffset,
            size = canvasSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += aAng

        drawArc(
            color = SoftGold,
            startAngle = startAngle,
            sweepAngle = dAng,
            useCenter = false,
            topLeft = topLeftOffset,
            size = canvasSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += dAng

        drawArc(
            color = Neon_Pink_Fallback,
            startAngle = startAngle,
            sweepAngle = apAng,
            useCenter = false,
            topLeft = topLeftOffset,
            size = canvasSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += apAng

        drawArc(
            color = WarningOrange,
            startAngle = startAngle,
            sweepAngle = zAng,
            useCenter = false,
            topLeft = topLeftOffset,
            size = canvasSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += zAng

        drawArc(
            color = GlassBorder,
            startAngle = startAngle,
            sweepAngle = fAng,
            useCenter = false,
            topLeft = topLeftOffset,
            size = canvasSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

val Neon_Pink_Fallback = Color(0xFFEC4899)

@Composable
fun LegendItem(label: String, valStr: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.LightGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valStr,
            fontSize = 13.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MetricRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = GlassBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = Color.LightGray
            )
        }
    }
}

@Composable
fun AppBottomNavigation(
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF18181B), // Zinc-900 High Density BG
        tonalElevation = 0.dp,
        modifier = Modifier
            .navigationBarsPadding() // Proper safe area bottom padding
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .border(
                width = 1.dp,
                color = Color(0xFF27272A), // Zinc-800 Custom Border
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
    ) {
        val selectedActiveIndicatorColor = CyberBlue.copy(alpha = 0.2f)

        NavigationBarItem(
            selected = currentRoute == "home" || currentRoute == "about",
            onClick = { onTabSelected("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyberBlue,
                selectedTextColor = CyberBlue,
                unselectedIconColor = Color(0xFF64748B), // Slate-500
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = selectedActiveIndicatorColor
            )
        )
        NavigationBarItem(
            selected = currentRoute == "explorer" || currentRoute == "categories",
            onClick = { onTabSelected("explorer") },
            icon = { Icon(Icons.Default.FolderOpen, contentDescription = "Files") },
            label = { Text("Files", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyberBlue,
                selectedTextColor = CyberBlue,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = selectedActiveIndicatorColor
            )
        )
        NavigationBarItem(
            selected = currentRoute == "clean",
            onClick = { onTabSelected("clean") },
            icon = { Icon(Icons.Default.CleaningServices, contentDescription = "Clean") },
            label = { Text("Clean", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ActiveGreen,
                selectedTextColor = ActiveGreen,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = ActiveGreen.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "vault_auth" || currentRoute == "vault_dashboard",
            onClick = { onTabSelected("vault_auth") },
            icon = { Icon(Icons.Default.Lock, contentDescription = "Vault") },
            label = { Text("Vault", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DeepPurple,
                selectedTextColor = DeepPurple,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = DeepPurple.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "tools" || currentRoute == "apk_manager" || currentRoute == "share",
            onClick = { onTabSelected("tools") },
            icon = { Icon(Icons.Default.Apps, contentDescription = "Tools") },
            label = { Text("Tools", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SoftGold,
                selectedTextColor = SoftGold,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = SoftGold.copy(alpha = 0.2f)
            )
        )
    }
}

fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
