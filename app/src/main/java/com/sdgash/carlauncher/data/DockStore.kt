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

    /** App shown in the big right-hand card instead of the map (null = map). */
    var cardApp: String?
        get() = prefs.getString(KEY_CARD, null)
        set(v) = prefs.edit().putString(KEY_CARD, v).apply()

    /** Try to open the card app as a floating window over the card. */
    var cardFloating: Boolean
        get() = prefs.getBoolean(KEY_FLOAT, true)
        set(v) = prefs.edit().putBoolean(KEY_FLOAT, v).apply()

    companion object {
        private const val KEY = "pinned"
        private const val KEY_CARD = "card_app"
        private const val KEY_FLOAT = "card_floating"
        const val MAX_DOCK = 8

        /** Android Auto itself; other projection apps are found by name. */
        const val ANDROID_AUTO = "com.google.android.projection.gearhead"

        /** Matches phone-projection apps (Android Auto, CarPlay boxes, ZLink...) by label/package. */
        val PROJECTION_HINT = Regex("(?i)android ?auto|gearhead|carplay|zlink|carlink|autokit|手機互聯|互联|投屏")

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
