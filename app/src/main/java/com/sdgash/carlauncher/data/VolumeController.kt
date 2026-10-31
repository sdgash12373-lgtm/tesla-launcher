package com.sdgash.carlauncher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VolumeState(val percent: Int, val muted: Boolean)

interface VolumeControls {
    fun up()
    fun down()
    fun toggleMute()
}

/** Media (music) stream volume, kept in sync with hardware keys and other apps. */
class VolumeController(private val context: Context) : VolumeControls {

    private val audio = context.getSystemService(AudioManager::class.java)
    private val stream = AudioManager.STREAM_MUSIC
    private val _state = MutableStateFlow(read())
    val state: StateFlow<VolumeState> = _state.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) = update()
    }
    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) = update()
    }

    fun start() {
        // VOLUME_CHANGED_ACTION is not public API but is sent by every Android version;
        // the settings observer is a fallback for head units that customise audio.
        ContextCompat.registerReceiver(
            context, receiver, IntentFilter("android.media.VOLUME_CHANGED_ACTION"), ContextCompat.RECEIVER_EXPORTED,
        )
        context.contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, observer)
        update()
    }

    fun stop() {
        runCatching { context.unregisterReceiver(receiver) }
        context.contentResolver.unregisterContentObserver(observer)
    }

    private fun read(): VolumeState {
        val max = audio.getStreamMaxVolume(stream).coerceAtLeast(1)
        val cur = audio.getStreamVolume(stream)
        return VolumeState(percent = cur * 100 / max, muted = audio.isStreamMute(stream) || cur == 0)
    }

    private fun update() { _state.value = read() }

    private fun adjust(direction: Int) {
        runCatching { audio.adjustStreamVolume(stream, direction, 0) }
        update()
    }

    override fun up() = adjust(AudioManager.ADJUST_RAISE)
    override fun down() = adjust(AudioManager.ADJUST_LOWER)
    override fun toggleMute() = adjust(AudioManager.ADJUST_TOGGLE_MUTE)
}
