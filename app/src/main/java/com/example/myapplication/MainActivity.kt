package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.utils.ProgressManager

class MainActivity : AppCompatActivity() {
    private lateinit var tvProgress: TextView
    private lateinit var btnStart: Button
    private lateinit var btnReset: Button
    private lateinit var btnViewAllWords: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvProgress = findViewById(R.id.tv_progress)
        btnStart = findViewById(R.id.btn_start)
        btnReset = findViewById(R.id.btn_reset)
        btnViewAllWords = findViewById(R.id.btn_view_all_words)

        updateProgressText()

        btnStart.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            startActivity(intent)
        }

        btnReset.setOnClickListener {
            // 显示确认弹窗
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("确认重置")
            builder.setMessage("确定要重置所有数据吗？此操作不可撤销。")
            builder.setCancelable(true)
            
            // 设置确认按钮
            builder.setPositiveButton("确定") { _, _ ->
                // 执行重置所有数据操作
                ProgressManager.resetAllData(this)
                updateProgressText()
            }
            
            // 设置取消按钮
            builder.setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            
            // 创建并显示对话框
            val dialog = builder.create()
            dialog.show()
        }

        // 设置查看所有词汇按钮的点击事件
        btnViewAllWords.setOnClickListener {
            val intent = Intent(this, AllWordsActivity::class.java)
            startActivity(intent)
        }


        
        // 重置按钮功能已实现
    }

    override fun onResume() {
        super.onResume()
        updateProgressText()
    }

    private fun updateProgressText() {
        val (masteredCount, totalCount) = ProgressManager.getProgress(this)
        val reviewRound = ProgressManager.getReviewRound(this)
        
        tvProgress.text = if (reviewRound > 0) {
            // 如果已经有复习轮次，显示n轮复习和进度
            "${reviewRound}轮复习 ${masteredCount}/${totalCount}"
        } else if (ProgressManager.isRoundCompleted(this)) {
            // 如果第一轮学习完成但还没开始复习
            "1轮复习 ${masteredCount}/${totalCount}"
        } else {
            // 否则显示学习进度
            "学习单词 ${masteredCount}/${totalCount}"
        }
    }
}