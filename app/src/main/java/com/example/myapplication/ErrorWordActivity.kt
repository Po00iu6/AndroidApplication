package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.WordItem

class ErrorWordActivity : AppCompatActivity() {
    private lateinit var tvErrorTitle: TextView
    private lateinit var tvWord: TextView
    private lateinit var tvDefinition: TextView
    private lateinit var btnNext: Button

    // 定义一个静态常量作为返回结果的键
    companion object {
        const val RESULT_NEXT_WORD = 1001
        const val EXTRA_LOAD_NEXT_WORD = "load_next_word"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error_word)

        // 初始化UI控件
        tvErrorTitle = findViewById(R.id.tv_error_title)
        tvWord = findViewById(R.id.tv_word)
        tvDefinition = findViewById(R.id.tv_definition)
        btnNext = findViewById(R.id.btn_next)

        // 获取从上一个页面传来的单词数据
        val wordItem = intent.getSerializableExtra("WORD_ITEM") as WordItem
        
        // 获取是否需要重试的标志（表示达到了最大错误次数）
        val shouldRetryWord = intent.getBooleanExtra("SHOULD_RETRY_WORD", false)

        // 显示错词信息
        tvWord.text = wordItem.word
        tvDefinition.text = wordItem.definition
        
        // 只有在达到最大错误次数时才显示错误标题
        if (!shouldRetryWord) {
            tvErrorTitle.visibility = View.GONE
        }

        // 设置"下一步"按钮的点击事件
        btnNext.setOnClickListener {
            // 设置返回结果，指示需要加载下一个单词或重试
            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_LOAD_NEXT_WORD, !shouldRetryWord)
            resultIntent.putExtra("SHOULD_RETRY_WORD", shouldRetryWord)
            setResult(RESULT_NEXT_WORD, resultIntent)
            
            // 返回上一个页面
            finish()
        }
    }
}