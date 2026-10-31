package com.sdgash.carlauncher.data

import android.content.Context

/** Persists which apps are pinned to the bottom dock, in order. */
class DockStore(context: Context) {
    private val prefs = context.getSharedPreferences("dock", Context.MODE_PRIVATE)

    /** null means the user never edited the dock, so defaults should be used. */
    fun load(): List<String>? =
        prefs.getString(KEY, null)?.split('\n')?.filter { it.isNotBlank() }

    fun save(keys: List<String>) {
        prefs.edit().putString(KEY, keys.joinToString("\n")).apply()
    }

    companion object {
        private const val KEY = "pinned"
        const val MAX_DOCK = 8

        /** Packages we try to pin on first run if they are installed. */
        val DEFAULT_PACKAGES = listOf(
            "com.google.android.apps.maps",
            "com.waze",
            "com.spotify.music",
            "com.google.android.apps.youtube.music",
            "com.google.android.youtube",
            "com.android.chrome",
            "com.google.android.dialer",
            "com.android.dialer",
        )
    }
}
