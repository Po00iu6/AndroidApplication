package com.example.myapplication

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import com.example.myapplication.R

class SoundManager private constructor(private val context: Context) {

    private var soundEnabled = true // 控制声音是否启用
    private val audioManager: AudioManager?

    companion object {
        const val SOUND_CORRECT = 1
        const val SOUND_INCORRECT = 2

        // 使用单例模式
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context).also { instance = it }
            }
        }
    }

    init {
        // 初始化AudioManager
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        android.util.Log.d("SoundManager", "SoundManager initialized")
    }

    // 播放声音 - 优先使用MediaPlayer播放用户提供的mp3文件
    fun playSound(soundId: Int) {
        try {
            val soundName = if (soundId == SOUND_CORRECT) "correct" else "incorrect"
            
            if (!soundEnabled) {
                android.util.Log.d("SoundManager", "Sound $soundName not played - sound is disabled")
                return
            }
            
            // 优先使用MediaPlayer播放用户提供的mp3文件
            try {
                val resId = if (soundId == SOUND_CORRECT) R.raw.accepted else R.raw.error
                val mediaPlayer = MediaPlayer.create(context, resId)
                
                if (mediaPlayer != null) {
                    mediaPlayer.start()
                    android.util.Log.d("SoundManager", "MediaPlayer sound played: $soundName")
                    mediaPlayer.setOnCompletionListener { it.release() }
                    return
                } else {
                    android.util.Log.e("SoundManager", "Failed to create MediaPlayer for sound $soundName")
                }
            } catch (e: Exception) {
                android.util.Log.e("SoundManager", "Error playing sound with MediaPlayer: ${e.message}")
            }
            
            // 如果MediaPlayer播放失败，才使用AudioManager播放系统音效作为备选
            if (audioManager != null) {
                // 为正确和错误选择不同的系统音效
                val soundEffect = if (soundId == SOUND_CORRECT) {
                    AudioManager.FX_KEYPRESS_STANDARD
                } else {
                    AudioManager.FX_KEYPRESS_STANDARD
                }
                
                audioManager.playSoundEffect(soundEffect, 1.0f)
                android.util.Log.d("SoundManager", "System sound effect played: $soundName")
            } else {
                android.util.Log.e("SoundManager", "AudioManager not available")
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error in playSound: ${e.message}")
        }
    }

    // 设置声音是否启用
    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    // 检查声音是否启用
    fun isSoundEnabled(): Boolean {
        return soundEnabled
    }

    // 释放资源
    fun release() {
        instance = null
    }
    
    // 直接测试声音播放的方法
    fun testSound() {
        try {
            android.util.Log.d("SoundManager", "Testing sound playback directly")
            
            // 检查声音是否启用
            if (!soundEnabled) {
                android.util.Log.d("SoundManager", "Sound is disabled")
                // 移除声音禁用提示
                return
            }
            
            // 使用AudioManager播放系统按键音
            if (audioManager != null) {
                audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
                android.util.Log.d("SoundManager", "Test sound played via AudioManager")
                // 移除测试声音播放提示
                return
            }
            
            // 如果AudioManager不可用，显示错误信息
            android.util.Log.e("SoundManager", "AudioManager not available for test sound")
            // 移除无法播放测试声音提示
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Critical error in test sound: ${e.message}")
            // 移除播放测试声音错误提示
        }
    }
}