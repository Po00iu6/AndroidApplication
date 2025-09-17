package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.VocabList
import com.example.myapplication.model.WordItem

class AllWordsActivity : AppCompatActivity() {
    private lateinit var wordsListContainer: LinearLayout
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_words)

        wordsListContainer = findViewById(R.id.words_list_container)
        btnBack = findViewById<Button>(R.id.btn_back)

        // 设置返回按钮的点击事件
        btnBack.setOnClickListener {
            finish()
        }

        // 显示所有词汇
        displayAllWords()
    }

    private fun displayAllWords() {
        // 获取所有词汇
        val allWords = VocabList.allWords

        // 遍历所有词汇并添加到界面
        for ((index, wordItem) in allWords.withIndex()) {
            // 创建单词项的布局
            val wordItemView = createWordItemView(wordItem, index + 1) // 序号从1开始
            wordsListContainer.addView(wordItemView)

            // 在每个单词项之间添加分隔线，除了最后一个
            if (index < allWords.size - 1) {
                val divider = View(this)
                val dividerParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                dividerParams.setMargins(16, 8, 16, 8)
                divider.layoutParams = dividerParams
                divider.setBackgroundColor(resources.getColor(android.R.color.darker_gray, theme))
                wordsListContainer.addView(divider)
            }
        }
    }

    private fun createWordItemView(wordItem: WordItem, position: Int): View {
        // 使用LayoutInflater创建单词项的视图
        val inflater = LayoutInflater.from(this)
        val wordItemView = inflater.inflate(R.layout.item_word, null)

        // 设置视图的布局参数
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        wordItemView.layoutParams = layoutParams

        // 获取视图中的控件
        val tvPosition: TextView = wordItemView.findViewById(R.id.tv_position)
        val tvWord: TextView = wordItemView.findViewById(R.id.tv_word)
        val tvDefinition: TextView = wordItemView.findViewById(R.id.tv_definition)

        // 设置控件内容
        tvPosition.text = "$position."
        tvWord.text = wordItem.word
        tvDefinition.text = wordItem.definition

        return wordItemView
    }
}