package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.SoundManager
import com.example.myapplication.model.WordItem
import com.example.myapplication.model.VocabList
import kotlin.random.Random

class ErrorWordRetryActivity : AppCompatActivity() {
    private lateinit var tvErrorTitle: TextView
    private lateinit var tvWord: TextView
    private lateinit var btnOption1: Button
    private lateinit var btnOption2: Button
    private lateinit var btnOption3: Button
    private lateinit var btnOption4: Button
    private lateinit var btnReset: Button
    private lateinit var soundManager: SoundManager
    
    // 定义常量
    companion object {
        const val RESULT_RETRY_COMPLETED = 2001
        const val EXTRA_WORD_ITEM = "word_item"
        const val EXTRA_IS_CORRECT = "is_correct"
    }
    
    private var currentWord: WordItem? = null
    private var retryErrorCount: Int = 0
    private val MAX_RETRY_ERRORS = 3
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error_word_retry)
        
        // 初始化UI控件
        tvErrorTitle = findViewById(R.id.tv_error_title_retry)
        tvWord = findViewById(R.id.tv_word_retry)
        btnOption1 = findViewById(R.id.btn_option1_retry)
        btnOption2 = findViewById(R.id.btn_option2_retry)
        btnOption3 = findViewById(R.id.btn_option3_retry)
        btnOption4 = findViewById(R.id.btn_option4_retry)
        btnReset = findViewById(R.id.btn_reset_retry)
        
        // 初始化声音管理器
        soundManager = SoundManager.getInstance(this)
        
        // 获取从上一个页面传来的单词数据
        currentWord = intent.getSerializableExtra("WORD_ITEM") as WordItem
        
        // 显示错词信息
        if (currentWord != null) {
            tvWord.text = currentWord!!.word
            
            // 生成选项
            val options = generateOptions(currentWord!!)
            
            // 设置选项按钮的文本
            btnOption1.text = options[0]
            btnOption2.text = options[1]
            btnOption3.text = options[2]
            btnOption4.text = options[3]
        }
        
        // 设置选项按钮的点击事件
        btnOption1.setOnClickListener {
            handleOptionClick(btnOption1)
        }
        btnOption2.setOnClickListener {
            handleOptionClick(btnOption2)
        }
        btnOption3.setOnClickListener {
            handleOptionClick(btnOption3)
        }
        btnOption4.setOnClickListener {
            handleOptionClick(btnOption4)
        }
        
        // 设置重置按钮的点击事件
        btnReset.setOnClickListener {
            resetRetryPage()
        }
    }
    
    private fun generateOptions(correctWord: WordItem): List<String> {
        val options = mutableListOf<String>()
        options.add(correctWord.definition)
        
        // 从其他单词中随机选择3个干扰项
        val otherWords = VocabList.allWords.filter { it.id != correctWord.id }
        val random = Random
        
        while (options.size < 4) {
            val randomIndex = random.nextInt(otherWords.size)
            val option = otherWords[randomIndex].definition
            if (!options.contains(option)) {
                options.add(option)
            }
        }
        
        // 打乱选项顺序
        options.shuffle()
        return options
    }
    
    private fun handleOptionClick(button: Button) {
        val selectedOption = button.text.toString()
        
        if (currentWord != null) {
            if (selectedOption == currentWord!!.definition) {
                // 回答正确
                button.backgroundTintList = resources.getColorStateList(android.R.color.holo_green_light, theme)
                button.setTextColor(resources.getColor(android.R.color.black, theme))
                
                // 播放正确声音
                soundManager.playSound(SoundManager.SOUND_CORRECT)
                
                // 禁用所有按钮
                disableAllButtons()
                
                // 显示成功提示
                // 移除回答正确提示
                
                // 设置返回结果，指示回答正确
                val resultIntent = Intent()
                resultIntent.putExtra(EXTRA_IS_CORRECT, true)
                setResult(RESULT_RETRY_COMPLETED, resultIntent)
                
                // 延迟返回上一个页面
                button.postDelayed({
                    finish()
                }, 1500)
            } else {
                // 回答错误，增加错误次数
                retryErrorCount++
                
                // 播放错误声音
                soundManager.playSound(SoundManager.SOUND_INCORRECT)
                
                // 将错误选项设为红色
                button.backgroundTintList = resources.getColorStateList(android.R.color.holo_red_light, theme)
                button.setTextColor(resources.getColor(android.R.color.white, theme))
                button.isEnabled = false
                
                // 显示错误提示
                // 移除错误重试次数提示
                
                // 如果达到最大错误次数，自动重置页面
                if (retryErrorCount >= MAX_RETRY_ERRORS) {
                    // 移除最大错误次数提示
                    
                    // 自动重置选项
                    resetRetryPage()
                    
                    // 显示提示信息
                    // 移除重新选择答案提示
                }
            }
        }
    }
    
    private fun disableAllButtons() {
        btnOption1.isEnabled = false
        btnOption2.isEnabled = false
        btnOption3.isEnabled = false
        btnOption4.isEnabled = false
    }
    
    private fun resetRetryPage() {
        // 重置错误计数器
        retryErrorCount = 0
        
        // 隐藏重置按钮
        btnReset.visibility = View.GONE
        
        // 重置按钮状态
        resetButtons()
        
        // 重新生成选项
        if (currentWord != null) {
            val options = generateOptions(currentWord!!)
            btnOption1.text = options[0]
            btnOption2.text = options[1]
            btnOption3.text = options[2]
            btnOption4.text = options[3]
        }
        
        // 显示提示
        // 移除重新选择正确含义提示
    }
    
    private fun resetButtons() {
        val buttons = listOf(btnOption1, btnOption2, btnOption3, btnOption4)
        for (button in buttons) {
            button.backgroundTintList = resources.getColorStateList(android.R.color.white, theme)
            button.setTextColor(resources.getColor(android.R.color.black, theme))
            button.isEnabled = true
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 释放声音管理器资源
        soundManager.release()
    }
}