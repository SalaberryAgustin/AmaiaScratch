package com.family.scratchapp.data.models

enum class PhotoOrder { RANDOM, SEQUENTIAL }
enum class SurfaceType { DIRT, SAND, GRASS, RANDOM }
enum class AutoRevealTime(val seconds: Int) {
    THIRTY(30), SIXTY(60), NINETY(90)
}
enum class MusicTrack(val resId: Int, val displayName: String) {
    NONE(0, "Sin música"),
    TRACK_1(1, "Campanas del bosque"),
    TRACK_2(2, "Vals de las estrellas"),
    TRACK_3(3, "Canción del río")
}

data class GameSettings(
    val photoOrder: PhotoOrder = PhotoOrder.RANDOM,
    val surfaceType: SurfaceType = SurfaceType.SAND,
    val autoRevealTime: AutoRevealTime = AutoRevealTime.SIXTY,
    val musicTrack: MusicTrack = MusicTrack.TRACK_1
)
