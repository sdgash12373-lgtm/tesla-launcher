package com.sdgash.carlauncher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** What is playing right now, from whichever app owns the active media session. */
data class NowPlaying(
    val title: String,
    val artist: String,
    val art: ImageBitmap?,
    val isPlaying: Boolean,
    val sourceLabel: String,
)

/** Media controls the UI can call. Kept as an interface so screenshots can use a fake. */
interface MediaControls {
    fun playPause()
    fun next()
    fun previous()
    fun openSource()
    fun requestAccess()
}

class MediaRepository(private val context: Context) : MediaControls {

    private val sessionManager = context.getSystemService(MediaSessionManager::class.java)
    private val listener = ComponentName(context, MediaListenerService::class.java)
    private val main = Handler(Looper.getMainLooper())

    private val _hasAccess = MutableStateFlow(false)
    val hasAccess: StateFlow<Boolean> = _hasAccess.asStateFlow()

    private val _nowPlaying = MutableStateFlow<NowPlaying?>(null)
    val nowPlaying: StateFlow<NowPlaying?> = _nowPlaying.asStateFlow()

    private var controller: MediaController? = null
    private var sessionsListenerRegistered = false

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            // Another app may have started playing; re-pick the best session.
            pickController(currentSessions())
        }
        override fun onSessionDestroyed() = pickController(currentSessions())
    }

    private val sessionsChanged = MediaSessionManager.OnActiveSessionsChangedListener { list ->
        pickController(list.orEmpty())
    }

    /** Call on start/resume: the user may have just granted access in Settings. */
    fun refresh() {
        val granted = NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
        _hasAccess.value = granted
        if (!granted) {
            setController(null)
            return
        }
        if (!sessionsListenerRegistered) {
            runCatching {
                sessionManager.addOnActiveSessionsChangedListener(sessionsChanged, listener, main)
                sessionsListenerRegistered = true
            }
        }
        pickController(currentSessions())
    }

    fun release() {
        if (sessionsListenerRegistered) sessionManager.removeOnActiveSessionsChangedListener(sessionsChanged)
        sessionsListenerRegistered = false
        setController(null)
    }

    private fun currentSessions(): List<MediaController> =
        runCatching { sessionManager.getActiveSessions(listener) }.getOrDefault(emptyList())

    private fun pickController(sessions: List<MediaController>) {
        val best = sessions.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: sessions.firstOrNull { it.packageName == controller?.packageName }
            ?: sessions.firstOrNull()
        if (best?.sessionToken != controller?.sessionToken) setController(best) else publish()
    }

    private fun setController(c: MediaController?) {
        controller?.unregisterCallback(controllerCallback)
        controller = c
        c?.registerCallback(controllerCallback, main)
        publish()
    }

    private fun publish() {
        val c = controller
        val meta = c?.metadata
        if (c == null || meta == null) {
            _nowPlaying.value = null
            return
        }
        val art: Bitmap? = meta.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: meta.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: meta.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
        _nowPlaying.value = NowPlaying(
            title = meta.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE).orEmpty(),
            artist = meta.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: meta.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?: meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE).orEmpty(),
            art = art?.asImageBitmap(),
            isPlaying = c.playbackState?.state == PlaybackState.STATE_PLAYING,
            sourceLabel = appLabel(c.packageName),
        )
    }

    private fun appLabel(pkg: String): String = runCatching {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)

    override fun playPause() {
        val c = controller ?: return
        if (c.playbackState?.state == PlaybackState.STATE_PLAYING) c.transportControls.pause()
        else c.transportControls.play()
    }

    override fun next() { controller?.transportControls?.skipToNext() }
    override fun previous() { controller?.transportControls?.skipToPrevious() }

    override fun openSource() {
        val c = controller ?: return
        runCatching {
            c.sessionActivity?.send() ?: context.packageManager.getLaunchIntentForPackage(c.packageName)
                ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)?.let(context::startActivity)
        }
    }

    override fun requestAccess() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
