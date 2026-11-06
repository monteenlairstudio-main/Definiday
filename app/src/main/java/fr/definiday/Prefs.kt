package fr.definiday

import android.content.Context

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("definiday", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = sp.getBoolean("enabled", true)
        set(v) = sp.edit().putBoolean("enabled", v).apply()
    var hour: Int
        get() = sp.getInt("hour", 8)
        set(v) = sp.edit().putInt("hour", v).apply()
    var minute: Int
        get() = sp.getInt("minute", 0)
        set(v) = sp.edit().putInt("minute", v).apply()
    var durationMinutes: Int
        get() = sp.getInt("duration", 120)
        set(v) = sp.edit().putInt("duration", v).apply()
    var wallpaper: Boolean
        get() = sp.getBoolean("wallpaper", false)
        set(v) = sp.edit().putBoolean("wallpaper", v).apply()
    /** When our lock-screen wallpaper must be removed; 0 = none of ours is set. */
    var wallpaperEndAt: Long
        get() = sp.getLong("wallpaperEndAt", 0)
        set(v) = sp.edit().putLong("wallpaperEndAt", v).apply()
    var wallpaperId: Int
        get() = sp.getInt("wallpaperId", -1)
        set(v) = sp.edit().putInt("wallpaperId", v).apply()
}
