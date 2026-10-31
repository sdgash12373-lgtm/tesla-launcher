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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sdgash.carlauncher.data.AppEntry

/** Bottom bar: car button, pinned apps in the middle, app drawer and settings on the right. */
@Composable
fun Dock(
    height: Dp,
    apps: List<AppEntry>,
    drawerOpen: Boolean,
    onCar: () -> Unit,
    onAppClick: (AppEntry) -> Unit,
    onUnpin: (AppEntry) -> Unit,
    onAppInfo: (AppEntry) -> Unit,
    onToggleDrawer: () -> Unit,
    onSettings: () -> Unit,
) {
    val iconSize = (height * 0.55f).coerceIn(36.dp, 60.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(height)
            .background(CarColors.Dock)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DockButton(Icons.Filled.DirectionsCar, "車輛", iconSize, onClick = onCar)
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
        DockButton(Icons.Filled.Apps, "所有 App", iconSize, highlighted = drawerOpen, onClick = onToggleDrawer)
        DockButton(Icons.Filled.Settings, "設定", iconSize, onClick = onSettings)
    }
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
