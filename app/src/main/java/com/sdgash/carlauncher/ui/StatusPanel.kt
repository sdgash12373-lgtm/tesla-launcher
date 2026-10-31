package com.sdgash.carlauncher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Left-hand "vehicle" panel: gear, speed, clock and battery. */
@Composable
fun StatusPanel(modifier: Modifier = Modifier) {
    val now by rememberNow()
    val battery = rememberBatteryPercent()

    BoxWithConstraints(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CarColors.Panel)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Scale the big speed number with the panel so it fits 480px- and 720px-tall screens.
        val speedSize = (maxHeight.value * 0.28f).coerceIn(48f, 140f).sp
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Gears(active = "P")
                Spacer(Modifier.weight(1f))
                if (battery != null) {
                    Icon(Icons.Filled.BatteryStd, null, tint = CarColors.TextSecondary, modifier = Modifier.size(18.dp))
                    Text("$battery%", color = CarColors.TextSecondary, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("0", color = CarColors.TextPrimary, fontSize = speedSize, fontWeight = FontWeight.Light)
                Text("km/h", color = CarColors.TextSecondary, fontSize = 16.sp)
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(now),
                    color = CarColors.TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    SimpleDateFormat("M月d日 EEE", Locale.TAIWAN).format(now),
                    color = CarColors.TextSecondary, fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun Gears(active: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf("P", "R", "N", "D").forEach { g ->
            val on = g == active
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (on) CarColors.PanelRaised else CarColors.Panel)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    g,
                    color = if (on) CarColors.TextPrimary else CarColors.TextSecondary.copy(alpha = 0.5f),
                    fontSize = 18.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
fun rememberNow() = remember { mutableStateOf(Date()) }.also { state ->
    LaunchedEffect(Unit) {
        while (true) {
            state.value = Date()
            delay(1000L - System.currentTimeMillis() % 1000L)
        }
    }
}

/** Device battery percentage, or null when the head unit reports no battery. */
@Composable
fun rememberBatteryPercent(): Int? {
    val context = LocalContext.current
    var percent by remember { mutableStateOf<Int?>(null) }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) {
                val present = i.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false)
                val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                percent = if (present && level >= 0 && scale > 0) level * 100 / scale else null
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }
    return percent
}
