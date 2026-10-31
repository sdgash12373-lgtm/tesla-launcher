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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdgash.carlauncher.data.MediaControls
import com.sdgash.carlauncher.data.NowPlaying
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Left-hand panel: clock and battery on top, speed in the middle and the
 * now-playing music card at the bottom (replaces the gear selector, since
 * the head unit has no gear sensor).
 */
@Composable
fun StatusPanel(
    nowPlaying: NowPlaying?,
    hasMediaAccess: Boolean,
    media: MediaControls,
    modifier: Modifier = Modifier,
) {
    val now by rememberNow()
    val battery = rememberBatteryPercent()

    BoxWithConstraints(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CarColors.Panel)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Scale the speed number with the panel so it fits 480px- and 720px-tall screens.
        val speedSize = (maxHeight.value * 0.20f).coerceIn(40f, 120f).sp
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(now),
                    color = CarColors.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    SimpleDateFormat("M月d日 EEE", Locale.TAIWAN).format(now),
                    color = CarColors.TextSecondary, fontSize = 14.sp,
                )
                if (battery != null) {
                    Spacer(Modifier.width(10.dp))
                    Icon(Icons.Filled.BatteryStd, null, tint = CarColors.TextSecondary, modifier = Modifier.size(16.dp))
                    Text("$battery%", color = CarColors.TextSecondary, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("0", color = CarColors.TextPrimary, fontSize = speedSize, fontWeight = FontWeight.Light)
                Text("km/h", color = CarColors.TextSecondary, fontSize = 15.sp)
            }
            Spacer(Modifier.weight(1f))
            MusicCard(nowPlaying, hasMediaAccess, media, Modifier.fillMaxWidth())
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
    if (LocalInspectionMode.current) return 87  // previews / screenshot tests
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
