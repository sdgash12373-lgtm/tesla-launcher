package com.sdgash.carlauncher

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Process
import androidx.compose.ui.graphics.asImageBitmap
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import com.android.resources.ScreenRatio
import com.android.resources.ScreenSize
import com.sdgash.carlauncher.data.AppEntry
import com.sdgash.carlauncher.ui.CarTheme
import com.sdgash.carlauncher.data.MediaControls
import com.sdgash.carlauncher.data.NowPlaying
import com.sdgash.carlauncher.data.VolumeControls
import com.sdgash.carlauncher.data.VolumeState
import com.sdgash.carlauncher.ui.HomeActions
import com.sdgash.carlauncher.ui.HomeContent
import com.sdgash.carlauncher.ui.HomeState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Renders the home screen on typical car-screen sizes. Run
 * `./gradlew recordPaparazziDebug`; PNGs land in app/src/test/snapshots/images.
 */
@RunWith(Parameterized::class)
class HomeScreenshotTest(private val screen: Screen) {

    data class Screen(val name: String, val w: Int, val h: Int, val density: Density) {
        override fun toString() = name
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun screens() = listOf(
            Screen("1920x720", 1920, 720, Density.MEDIUM),
            Screen("1280x480", 1280, 480, Density.MEDIUM),
            Screen("1920x720_hdpi", 1920, 720, Density.HIGH),
        )
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(
            screenWidth = screen.w,
            screenHeight = screen.h,
            xdpi = screen.density.dpiValue,
            ydpi = screen.density.dpiValue,
            density = screen.density,
            orientation = ScreenOrientation.LANDSCAPE,
            ratio = ScreenRatio.LONG,
            size = ScreenSize.LARGE,
            softButtons = false,
        ),
        theme = "android:Theme.Material.NoActionBar",
        showSystemUi = false,
        maxPercentDifference = 0.1,
    )

    // Lazy: Bitmap/UserHandle need layoutlib, which the rule only loads once a test starts.
    private val apps by lazy { listOf(
        "Google 地圖" to 0xFF34A853, "Spotify" to 0xFF1DB954, "YouTube" to 0xFFFF0000,
        "YT Music" to 0xFFE62117, "Chrome" to 0xFF4285F4, "電話" to 0xFF0F9D58,
        "設定" to 0xFF5F6368, "相機" to 0xFF455A64, "KKBOX" to 0xFF09CEF6,
        "Waze" to 0xFF33CCFF, "LINE" to 0xFF06C755, "檔案" to 0xFF1A73E8,
        "藍牙音樂" to 0xFF3E6AE1, "收音機" to 0xFFFF8F00, "行車記錄器" to 0xFF8E24AA,
        "Netflix" to 0xFFE50914,
    ).mapIndexed { i, (label, color) ->
        AppEntry(label, ComponentName("demo.app$i", "Main"), Process.myUserHandle(), fakeIcon(label, color.toInt()))
    } }

    private fun fakeIcon(label: String, color: Int) =
        Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888).also { bmp ->
            val c = Canvas(bmp)
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
            c.drawRoundRect(RectF(4f, 4f, 124f, 124f), 30f, 30f, p)
            p.color = android.graphics.Color.WHITE
            p.textSize = 60f
            p.textAlign = Paint.Align.CENTER
            c.drawText(label.take(1), 64f, 86f, p)
        }.asImageBitmap()

    private val fakeMedia = object : MediaControls {
        override fun playPause() {}
        override fun next() {}
        override fun previous() {}
        override fun openSource() {}
        override fun requestAccess() {}
    }
    private val fakeVolume = object : VolumeControls {
        override fun up() {}
        override fun down() {}
        override fun toggleMute() {}
    }
    private val actions = object : HomeActions {
        override fun launch(app: AppEntry) {}
        override fun openCardApp(app: AppEntry, bounds: android.graphics.Rect) {}
        override fun isPinned(app: AppEntry) = app in apps.take(6)
        override fun togglePin(app: AppEntry) {}
        override fun appInfo(app: AppEntry) {}
        override fun settings() {}
        override fun customizeCard() {}
        override val media: MediaControls = fakeMedia
        override val volume: VolumeControls = fakeVolume
    }

    private fun nowPlaying() = NowPlaying(
        title = "晴天",
        artist = "周杰倫",
        art = fakeIcon("♪", 0xFF8E24AA.toInt()),
        isPlaying = true,
        sourceLabel = "Spotify",
    )

    private val androidAuto by lazy {
        AppEntry("Android Auto", ComponentName("com.google.android.projection.gearhead", "Main"),
            Process.myUserHandle(), fakeIcon("A", 0xFF4285F4.toInt()))
    }

    /** Map card, music playing. */
    @Test
    fun home() = snap(drawerOpen = false, cardApp = null, playing = true)

    /** Card set to Android Auto, notification access not yet granted. */
    @Test
    fun androidAutoCard() = snap(drawerOpen = false, cardApp = androidAuto, playing = false, access = false)

    @Test
    fun appDrawer() = snap(drawerOpen = true, cardApp = null, playing = true)

    private fun snap(drawerOpen: Boolean, cardApp: AppEntry?, playing: Boolean, access: Boolean = true) {
        val state = HomeState(
            apps = apps,
            dock = apps.take(6),
            cardApp = cardApp,
            nowPlaying = if (playing) nowPlaying() else null,
            hasMediaAccess = access,
            volume = VolumeState(percent = 45, muted = false),
        )
        paparazzi.snapshot {
            CarTheme {
                HomeContent(state = state, actions = actions, drawerOpen = drawerOpen, onDrawerOpenChange = {})
            }
        }
    }
}
