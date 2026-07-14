package com.family.scratchapp.audio

import android.content.Context
import android.media.MediaPlayer
import com.family.scratchapp.R
import com.family.scratchapp.data.models.MusicTrack

class ScratchAudioManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    private val trackResIds = mapOf(
        MusicTrack.TRACK_1 to R.raw.music_track_1,
        MusicTrack.TRACK_2 to R.raw.music_track_2,
        MusicTrack.TRACK_3 to R.raw.music_track_3
    )

    fun startMusic(track: MusicTrack) {
        if (track == MusicTrack.NONE) return
        stopMusic()
        val resId = trackResIds[track] ?: return
        mediaPlayer = MediaPlayer.create(context, resId)?.apply {
            isLooping = true
            setVolume(0.6f, 0.6f)
            start()
        }
    }

    fun stopMusic() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    fun release() = stopMusic()
}
