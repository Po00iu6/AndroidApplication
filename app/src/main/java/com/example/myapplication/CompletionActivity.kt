package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.ProgressManager

class CompletionActivity : AppCompatActivity() {
    private lateinit var tvCompletionMessage: TextView
    private lateinit var tvCompletionSubtitle: TextView
    private lateinit var btnBackToHome: Button
    private lateinit var btnStartReview: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completion)

        // 初始化UI控件
        tvCompletionMessage = findViewById(R.id.tv_completion_message)
        tvCompletionSubtitle = findViewById(R.id.tv_completion_subtitle)
        btnBackToHome = findViewById(R.id.btn_back_to_home)
        btnStartReview = findViewById(R.id.btn_start_review)

        // 获取是否是复习模式的参数
        val isReview = intent.getBooleanExtra("IS_REVIEW", false)
        val reviewRound = ProgressManager.getReviewRound(this)

        // 设置完成信息
        if (isReview) {
            tvCompletionMessage.text = "恭喜完成${reviewRound}轮复习！"
            tvCompletionSubtitle.text = "您已经成功巩固了所学的词！"
            btnStartReview.text = "开始下一轮复习"
        } else {
            tvCompletionMessage.text = "恭喜掌握了所有的词！"
            tvCompletionSubtitle.text = "您已经成功学习了所有文言文实词虚词！"
            btnStartReview.text = "开始复习"
        }

        // 设置返回主页按钮的点击事件
        btnBackToHome.setOnClickListener {
            finish()
        }

        // 设置开始复习按钮的点击事件
        btnStartReview.setOnClickListener {
            if (!isReview) {
                // 如果是第一次完成，增加复习轮次
                ProgressManager.incrementReviewRound(this)
            }
            
            // 跳转到复习模式的QuizActivity
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("IS_REVIEW", true)
            startActivity(intent)
            finish()
        }
    }
}