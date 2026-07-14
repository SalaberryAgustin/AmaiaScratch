package com.family.scratchapp.data

import android.content.Context
import com.family.scratchapp.data.models.*

class SettingsPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("scratch_settings", Context.MODE_PRIVATE)

    var photoOrder: PhotoOrder
        get() = PhotoOrder.valueOf(prefs.getString("photo_order", PhotoOrder.RANDOM.name)!!)
        set(v) = prefs.edit().putString("photo_order", v.name).apply()

    var surfaceType: SurfaceType
        get() = SurfaceType.valueOf(prefs.getString("surface_type", SurfaceType.SAND.name)!!)
        set(v) = prefs.edit().putString("surface_type", v.name).apply()

    var autoRevealTime: AutoRevealTime
        get() = AutoRevealTime.valueOf(prefs.getString("auto_reveal", AutoRevealTime.SIXTY.name)!!)
        set(v) = prefs.edit().putString("auto_reveal", v.name).apply()

    var musicTrack: MusicTrack
        get() = MusicTrack.valueOf(prefs.getString("music_track", MusicTrack.TRACK_1.name)!!)
        set(v) = prefs.edit().putString("music_track", v.name).apply()

    fun getGameSettings() = GameSettings(photoOrder, surfaceType, autoRevealTime, musicTrack)
}
