package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private val SPLASH_DISPLAY_LENGTH = 3000 // 开屏显示时间3秒
    private lateinit var handler: Handler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // 初始化Handler
        handler = Handler(Looper.getMainLooper())
        
        // 设置跳过按钮点击事件
        val skipButton: Button = findViewById(R.id.skip_button)
        skipButton.setOnClickListener {
            // 移除延迟任务并直接跳转到主页面
            handler.removeCallbacksAndMessages(null)
            goToMainActivity()
        }
        
        // 延迟跳转到主页面
        handler.postDelayed({
            goToMainActivity()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
    
    private fun goToMainActivity() {
        val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 防止内存泄漏
        handler.removeCallbacksAndMessages(null)
    }
}