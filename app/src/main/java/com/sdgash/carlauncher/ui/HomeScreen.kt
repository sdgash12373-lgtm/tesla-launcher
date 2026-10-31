package com.sdgash.carlauncher.ui

import android.graphics.Rect
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sdgash.carlauncher.LauncherViewModel
import com.sdgash.carlauncher.data.AppEntry
import com.sdgash.carlauncher.data.MediaControls
import com.sdgash.carlauncher.data.NowPlaying
import com.sdgash.carlauncher.data.VolumeControls
import com.sdgash.carlauncher.data.VolumeState

/** Everything the home layout shows, so it can be rendered without real services. */
data class HomeState(
    val apps: List<AppEntry>,
    val dock: List<AppEntry>,
    val cardApp: AppEntry?,
    val nowPlaying: NowPlaying?,
    val hasMediaAccess: Boolean,
    val volume: VolumeState,
)

/** What the home layout can do. */
interface HomeActions {
    fun launch(app: AppEntry)
    fun openCardApp(app: AppEntry, bounds: Rect)
    fun isPinned(app: AppEntry): Boolean
    fun togglePin(app: AppEntry)
    fun appInfo(app: AppEntry)
    fun settings()
    fun customizeCard()
    val media: MediaControls
    val volume: VolumeControls
}

/**
 * Tesla-style home: status + music on the left (~30%), the map / Android Auto
 * card on the right, dock along the bottom. Sizes are derived from the screen so
 * it works on wide/short car screens (e.g. 1920x720, 1280x480) as well as tablets.
 */
@Composable
fun HomeScreen(
    vm: LauncherViewModel,
    drawerOpen: Boolean,
    onDrawerOpenChange: (Boolean) -> Unit,
) {
    val apps by vm.apps.collectAsStateWithLifecycle()
    val dock by vm.dock.collectAsStateWithLifecycle()
    val cardApp by vm.cardApp.collectAsStateWithLifecycle()
    val floating by vm.cardFloating.collectAsStateWithLifecycle()
    val nowPlaying by vm.media.nowPlaying.collectAsStateWithLifecycle()
    val hasMediaAccess by vm.media.hasAccess.collectAsStateWithLifecycle()
    val volume by vm.volume.state.collectAsStateWithLifecycle()
    var pickerOpen by remember { mutableStateOf(false) }

    val actions = remember(vm) {
        object : HomeActions {
            override fun launch(app: AppEntry) = vm.repo.launch(app)
            override fun openCardApp(app: AppEntry, bounds: Rect) =
                if (vm.cardFloating.value) vm.repo.launchInBounds(app, bounds) else vm.repo.launch(app)
            override fun isPinned(app: AppEntry) = vm.isPinned(app)
            override fun togglePin(app: AppEntry) = vm.togglePin(app)
            override fun appInfo(app: AppEntry) = vm.repo.openAppInfo(app)
            override fun settings() = vm.repo.openSystemSettings()
            override fun customizeCard() { pickerOpen = true }
            override val media: MediaControls = vm.media
            override val volume: VolumeControls = vm.volume
        }
    }

    HomeContent(
        state = HomeState(apps, dock, cardApp, nowPlaying, hasMediaAccess, volume),
        actions = actions,
        drawerOpen = drawerOpen,
        onDrawerOpenChange = onDrawerOpenChange,
    )

    if (pickerOpen) {
        CardAppPicker(
            candidates = vm.cardCandidates(apps),
            current = cardApp,
            floating = floating,
            supportsFloating = vm.repo.supportsFreeform,
            onPick = { vm.setCardApp(it); pickerOpen = false },
            onFloatingChange = vm::setCardFloating,
            onDismiss = { pickerOpen = false },
        )
    }
}

/** Stateless home layout; also used for screenshot tests. */
@Composable
fun HomeContent(
    state: HomeState,
    actions: HomeActions,
    drawerOpen: Boolean,
    onDrawerOpenChange: (Boolean) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize().background(CarColors.Background)) {
        val dockHeight = (maxHeight * 0.14f).coerceIn(60.dp, 104.dp)
        val gap = 10.dp
        // Read the constraints here: layout scopes can't reach outer receivers.
        val screenW = maxWidth
        val screenH = maxHeight
        val isLandscape = screenW > screenH

        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(start = gap, end = gap, top = gap)) {
                val status = @Composable { m: Modifier ->
                    StatusPanel(state.nowPlaying, state.hasMediaAccess, actions.media, m)
                }
                val card = @Composable { m: Modifier ->
                    MainCard(state.cardApp, actions::openCardApp, actions::customizeCard, m)
                }
                if (isLandscape) {
                    val panelWidth = (screenW * 0.30f).coerceIn(260.dp, 460.dp)
                    Row(Modifier.fillMaxSize()) {
                        status(Modifier.width(panelWidth).fillMaxHeight())
                        Spacer(Modifier.width(gap))
                        card(Modifier.weight(1f).fillMaxHeight())
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        status(Modifier.fillMaxWidth().height((screenH * 0.40f).coerceAtLeast(260.dp)))
                        Spacer(Modifier.height(gap))
                        card(Modifier.weight(1f).fillMaxWidth())
                    }
                }

                // The drawer covers the content area, never the dock.
                DrawerOverlay(visible = drawerOpen) {
                    AppDrawer(
                        apps = state.apps,
                        isPinned = actions::isPinned,
                        onLaunch = { actions.launch(it); onDrawerOpenChange(false) },
                        onTogglePin = actions::togglePin,
                        onAppInfo = actions::appInfo,
                        onClose = { onDrawerOpenChange(false) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Dock(
                height = dockHeight,
                apps = state.dock,
                drawerOpen = drawerOpen,
                onAppClick = { actions.launch(it); onDrawerOpenChange(false) },
                onUnpin = actions::togglePin,
                onAppInfo = actions::appInfo,
                onToggleDrawer = { onDrawerOpenChange(!drawerOpen) },
                onSettings = actions::settings,
                onCustomizeCard = actions::customizeCard,
                volume = state.volume,
                volumeControls = actions.volume,
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
