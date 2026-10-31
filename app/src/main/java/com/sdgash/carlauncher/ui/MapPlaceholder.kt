package com.sdgash.carlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Stand-in for the map until a real map SDK is wired in. */
@Composable
fun MapPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1B1D22))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val road = Color(0xFF2A2D34)
            val minor = Color(0xFF22252B)
            val step = 90.dp.toPx()
            var x = step / 2
            while (x < size.width) {
                drawLine(minor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 3.dp.toPx())
                x += step
            }
            var y = step / 2
            while (y < size.height) {
                drawLine(minor, Offset(0f, y), Offset(size.width, y), strokeWidth = 3.dp.toPx())
                y += step
            }
            drawLine(road, Offset(0f, size.height * 0.62f), Offset(size.width, size.height * 0.38f), strokeWidth = 12.dp.toPx())
            drawLine(road, Offset(size.width * 0.55f, 0f), Offset(size.width * 0.45f, size.height), strokeWidth = 10.dp.toPx())
            // "You are here" puck
            drawCircle(CarColors.Accent.copy(alpha = 0.25f), radius = 22.dp.toPx(), center = center)
            drawCircle(Color.White, radius = 10.dp.toPx(), center = center)
            drawCircle(CarColors.Accent, radius = 7.dp.toPx(), center = center)
        }
        Text(
            "地圖（下一步接入）",
            color = CarColors.TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
        )
    }
}
