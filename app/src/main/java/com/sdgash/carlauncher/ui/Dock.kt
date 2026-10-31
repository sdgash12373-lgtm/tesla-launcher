package com.sdgash.carlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.sdgash.carlauncher.data.VolumeControls
import com.sdgash.carlauncher.data.VolumeState
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sdgash.carlauncher.data.AppEntry

/**
 * Bottom bar: car button (menu with all apps / settings) and volume on the left,
 * pinned apps in the middle.
 */
@Composable
fun Dock(
    height: Dp,
    apps: List<AppEntry>,
    drawerOpen: Boolean,
    onAppClick: (AppEntry) -> Unit,
    onUnpin: (AppEntry) -> Unit,
    onAppInfo: (AppEntry) -> Unit,
    onToggleDrawer: () -> Unit,
    onSettings: () -> Unit,
    onCustomizeCard: () -> Unit,
    volume: VolumeState,
    volumeControls: VolumeControls,
) {
    val iconSize = (height * 0.62f).coerceIn(40.dp, 68.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(height)
            .background(CarColors.Dock)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CarMenuButton(
            size = iconSize,
            highlighted = drawerOpen,
            onAllApps = onToggleDrawer,
            onSettings = onSettings,
            onCustomizeCard = onCustomizeCard,
        )
        VolumeControl(volume, volumeControls, iconSize)
        LazyRow(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(apps, key = { it.key }) { app ->
                AppIcon(
                    app = app,
                    iconSize = iconSize,
                    showLabel = false,
                    onClick = { onAppClick(app) },
                    menu = listOf(
                        AppMenuAction("從 Dock 移除") { onUnpin(app) },
                        AppMenuAction("App 資訊") { onAppInfo(app) },
                    ),
                )
            }
        }
        // Keeps the app icons visually centred against the left-hand controls.
        Spacer(Modifier.width(iconSize))
    }
}

/** The car logo: one tap opens a menu with All apps / Settings. */
@Composable
private fun CarMenuButton(
    size: Dp,
    highlighted: Boolean,
    onAllApps: () -> Unit,
    onSettings: () -> Unit,
    onCustomizeCard: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        DockButton(Icons.Filled.DirectionsCar, "選單", size, highlighted = highlighted || open) { open = true }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            modifier = Modifier.background(CarColors.PanelRaised),
        ) {
            CarMenuItem(Icons.Filled.Apps, "應用程式") { open = false; onAllApps() }
            CarMenuItem(Icons.Filled.Settings, "設定") { open = false; onSettings() }
            CarMenuItem(Icons.Filled.Dashboard, "自訂地圖卡片") { open = false; onCustomizeCard() }
        }
    }
}

@Composable
private fun CarMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, fontSize = 18.sp, color = CarColors.TextPrimary) },
        leadingIcon = { Icon(icon, null, tint = CarColors.TextPrimary) },
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun DockButton(
    icon: ImageVector,
    label: String,
    size: Dp,
    highlighted: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (highlighted) CarColors.PanelRaised else CarColors.Dock)
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = CarColors.TextPrimary, modifier = Modifier.size(size * 0.7f))
    }
}

/** Volume icon (tap to mute) with -/+ and the current media volume in percent. */
@Composable
private fun VolumeControl(state: VolumeState, controls: VolumeControls, size: Dp) {
    val icon = when {
        state.muted -> Icons.AutoMirrored.Filled.VolumeOff
        state.percent < 50 -> Icons.AutoMirrored.Filled.VolumeDown
        else -> Icons.AutoMirrored.Filled.VolumeUp
    }
    Row(
        Modifier
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CarColors.Panel),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallStep(Icons.Filled.Remove, "音量減", size, controls::down)
        Box(
            Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = controls::toggleMute)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon, contentDescription = "靜音",
                    tint = if (state.muted) CarColors.Red else CarColors.TextPrimary,
                    modifier = Modifier.size(size * 0.55f),
                )
                Text(
                    if (state.muted) "靜音" else "${state.percent}%",
                    color = CarColors.TextPrimary,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 52.dp),
                )
            }
        }
        SmallStep(Icons.Filled.Add, "音量加", size, controls::up)
    }
}

@Composable
private fun SmallStep(icon: ImageVector, label: String, size: Dp, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = CarColors.TextSecondary, modifier = Modifier.size(size * 0.45f))
    }
}
