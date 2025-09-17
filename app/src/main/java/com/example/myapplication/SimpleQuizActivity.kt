package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class SimpleQuizActivity : AppCompatActivity() {
    private lateinit var tvWord: TextView
    private lateinit var btnOption1: Button
    private lateinit var btnOption2: Button
    private lateinit var btnOption3: Button
    private lateinit var btnOption4: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_quiz)

        // 初始化UI控件
        tvWord = findViewById(R.id.tv_word_simple)
        btnOption1 = findViewById(R.id.btn_option1_simple)
        btnOption2 = findViewById(R.id.btn_option2_simple)
        btnOption3 = findViewById(R.id.btn_option3_simple)
        btnOption4 = findViewById(R.id.btn_option4_simple)
        btnBack = findViewById(R.id.btn_back_simple)

        // 设置简单的测试数据
        tvWord.text = "apple"
        btnOption1.text = "苹果"
        btnOption2.text = "香蕉"
        btnOption3.text = "橙子"
        btnOption4.text = "葡萄"

        // 设置返回按钮的点击事件
        btnBack.setOnClickListener {
            finish()
        }

        // 设置选项按钮的点击事件
        val optionClickListener = View.OnClickListener {
            // 简单的反馈
            val button = it as Button
            if (button.text == "苹果") {
                button.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
                // 移除回答正确提示
            } else {
                button.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
                // 移除回答错误提示
            }
        }

        btnOption1.setOnClickListener(optionClickListener)
        btnOption2.setOnClickListener(optionClickListener)
        btnOption3.setOnClickListener(optionClickListener)
        btnOption4.setOnClickListener(optionClickListener)
    }
}