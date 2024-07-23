package com.correct.correctsoc.helper

import android.content.Context
import android.media.MediaPlayer

class AudioUtils {
    private lateinit var mediaPlayer: MediaPlayer
    private var isMediaInitialized = false

    companion object {
        private var audioUtils: AudioUtils? = null

        fun getInstance(): AudioUtils {
            if (audioUtils == null) {
                audioUtils = AudioUtils()
            }
            return audioUtils as AudioUtils
        }
    }

    fun playAudio(context: Context, fileRes: Int) {
        mediaPlayer = MediaPlayer.create(context, fileRes)
        isMediaInitialized = true
        mediaPlayer.start()
    }

    fun isAudioPlaying():Boolean {
        return if (isMediaInitialized) {
            mediaPlayer.isPlaying
        } else {
            false
        }
    }

    fun releaseMedia() {
        if (this::mediaPlayer.isInitialized) {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
            }catch (e : Exception) {
                e.stackTrace
            }
            mediaPlayer.release()
        }
    }
}