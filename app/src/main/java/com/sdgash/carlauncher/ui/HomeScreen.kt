package com.sdgash.carlauncher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sdgash.carlauncher.LauncherViewModel

/**
 * Tesla-style home: status panel on the left (~30%), map on the right,
 * dock along the bottom. Sizes are derived from the screen so it works on
 * wide/short car screens (e.g. 1920x720, 1280x480) as well as tablets.
 */
@Composable
fun HomeScreen(
    vm: LauncherViewModel,
    drawerOpen: Boolean,
    onDrawerOpenChange: (Boolean) -> Unit,
) {
    val apps by vm.apps.collectAsStateWithLifecycle()
    val dock by vm.dock.collectAsStateWithLifecycle()
    val repo = vm.repo

    BoxWithConstraints(Modifier.fillMaxSize().background(CarColors.Background)) {
        val dockHeight = (maxHeight * 0.14f).coerceIn(60.dp, 104.dp)
        val gap = 10.dp
        // Read the constraints here: layout scopes can't reach outer receivers.
        val screenW = maxWidth
        val screenH = maxHeight
        val isLandscape = screenW > screenH

        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(start = gap, end = gap, top = gap)) {
                if (isLandscape) {
                    val panelWidth = (screenW * 0.30f).coerceIn(220.dp, 460.dp)
                    Row(Modifier.fillMaxSize()) {
                        StatusPanel(Modifier.width(panelWidth).fillMaxHeight())
                        Spacer(Modifier.width(gap))
                        MapPlaceholder(Modifier.weight(1f).fillMaxHeight())
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        StatusPanel(Modifier.fillMaxWidth().height((screenH * 0.35f).coerceAtLeast(200.dp)))
                        Spacer(Modifier.height(gap))
                        MapPlaceholder(Modifier.weight(1f).fillMaxWidth())
                    }
                }

                // The drawer covers the content area, never the dock.
                DrawerOverlay(visible = drawerOpen) {
                    AppDrawer(
                        apps = apps,
                        isPinned = vm::isPinned,
                        onLaunch = { repo.launch(it); onDrawerOpenChange(false) },
                        onTogglePin = vm::togglePin,
                        onAppInfo = repo::openAppInfo,
                        onClose = { onDrawerOpenChange(false) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Dock(
                height = dockHeight,
                apps = dock,
                drawerOpen = drawerOpen,
                onCar = { onDrawerOpenChange(false) },
                onAppClick = { repo.launch(it); onDrawerOpenChange(false) },
                onUnpin = vm::togglePin,
                onAppInfo = repo::openAppInfo,
                onToggleDrawer = { onDrawerOpenChange(!drawerOpen) },
                onSettings = repo::openSystemSettings,
            )
        }
    }
}

/** Kept outside any layout scope so the scope-free AnimatedVisibility overload is used. */
@Composable
private fun DrawerOverlay(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it / 3 } + fadeIn(),
        exit = slideOutVertically { it / 3 } + fadeOut(),
    ) {
        content()
    }
}
