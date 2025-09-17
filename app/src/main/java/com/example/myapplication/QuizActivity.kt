package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import java.util.ArrayDeque
import java.util.Queue
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.ForegroundColorSpan
import android.graphics.Color
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.WordItem
import com.example.myapplication.model.VocabList
import com.example.myapplication.utils.ProgressManager
import kotlin.random.Random

class QuizActivity : AppCompatActivity() {
    private lateinit var tvWord: TextView
    private lateinit var btnOption1: Button
    private lateinit var btnOption2: Button
    private lateinit var btnOption3: Button
    private lateinit var btnOption4: Button
    private lateinit var tvRemaining: TextView
    private lateinit var tvProgress: TextView
    private lateinit var btnBack: Button
    private lateinit var errorOverlay: View
    private lateinit var tvWordInfo: TextView
    private lateinit var tvCorrectDefinition: TextView
    private lateinit var btnCloseOverlay: Button
    private lateinit var btnNext: Button
    private lateinit var soundManager: SoundManager
    
    // 定义请求码常量
    private val REQUEST_CODE_ERROR_WORD = 100
    private val REQUEST_CODE_ERROR_WORD_RETRY = 200
    // 错误次数上限
    private val MAX_ERROR_COUNT = 3
    
    private var sampleWords: List<WordItem>? = null
    private var currentWord: WordItem? = null
    // 错误次数计数器
    private var errorCount: Int = 0
    // 存储已点击的错误选项
    private val selectedWrongOptions = mutableSetOf<String>()
    // 存储选项和对应单词的映射关系
    private var optionWordMap: Map<String, String> = emptyMap()
    // 标记是否是第一次点击选项
    private var isFirstAttempt: Boolean = true
    // 标记当前是否是复习模式
    private var isReview: Boolean = false
    // 复习轮次
    private var reviewRound: Int = 0
    
    // 单词错误次数映射
    private val wordErrorCounts = mutableMapOf<Int, Int>() // key: word.id, value: 错误次数
    // 存储当前单词列表
    private var currentWordList: List<WordItem> = emptyList()
    
    // 单词分组队列 - 每10个词一组
    private val wordGroups = mutableListOf<ArrayDeque<WordItem>>()
    // 当前正在使用的单词队列
    private var currentWordQueue: ArrayDeque<WordItem>? = null
    
    // 标记当前是否正在错词再选流程中
    private var isInRetryFlow = false
    
    // 标记当前单词是否已经执行过队列操作（用于防止同一页面多次错误导致的重复队列操作）
    private var queueOperationPerformed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // 初始化UI控件
        tvWord = findViewById(R.id.tv_word)
        btnOption1 = findViewById(R.id.btn_option1)
        btnOption2 = findViewById(R.id.btn_option2)
        btnOption3 = findViewById(R.id.btn_option3)
        btnOption4 = findViewById(R.id.btn_option4)
        tvRemaining = findViewById(R.id.tv_remaining)
        tvProgress = findViewById(R.id.tv_progress)
        btnBack = findViewById(R.id.btn_back)
        errorOverlay = findViewById(R.id.error_overlay)
        tvWordInfo = findViewById(R.id.tv_word_info)
        tvCorrectDefinition = findViewById(R.id.tv_correct_definition)
        btnCloseOverlay = findViewById(R.id.btn_close_overlay)
        btnNext = findViewById(R.id.btn_next)
        
        // 初始化声音管理器
        soundManager = SoundManager.getInstance(this)
        
        // 直接在Activity中添加一个测试声音按钮（使用简单的方式）
        val btnTestSound = Button(this)
        btnTestSound.text = "测试声音"
        btnTestSound.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        
        // 安全地找到主LinearLayout并添加测试按钮
            try {
                val contentView = findViewById<View>(android.R.id.content) as ViewGroup
                if (contentView.childCount > 0) {
                    val firstChild = contentView.getChildAt(0)
                    if (firstChild is LinearLayout) {
                        // 添加测试按钮到主布局
                        firstChild.addView(btnTestSound)
                    } else {
                        android.util.Log.e("QuizActivity", "First child is not LinearLayout")
                    }
                } else {
                    android.util.Log.e("QuizActivity", "Content view has no children")
                }
            } catch (e: Exception) {
                android.util.Log.e("QuizActivity", "Error adding test sound button: ${e.message}")
            }
        
        // 设置测试声音按钮点击事件
        btnTestSound.setOnClickListener {
            // 直接使用SoundManager测试声音播放
            soundManager.testSound()
        }

        // 获取是否是复习模式的参数
        isReview = intent.getBooleanExtra("IS_REVIEW", false)
        // 获取当前复习轮次
        reviewRound = ProgressManager.getReviewRound(this)
        
        // 初始化单词分组和队列
        initializeWordGroups()

        // 加载第一个单词
        loadNextWord()
    }
    
    // 初始化单词列表和队列
    private fun initializeWordGroups() {
        // 首先过滤掉已掌握的单词
        val masteredWordIds = ProgressManager.getMasteredWordIds(this)
        val unmasteredWords = VocabList.allWords.toList()
            .filter { word -> !masteredWordIds.contains(word.id) }
            .sortedBy { it.id } // 按照ID排序
        
        // 初始化当前单词列表
        currentWordList = unmasteredWords
        
        // 保存sampleWords以供其他地方使用
        sampleWords = unmasteredWords
        
        // 清空错误次数映射
        wordErrorCounts.clear()
        
        // 清空单词分组队列
        wordGroups.clear()
        currentWordQueue = null
        
        // 将单词按每10个一组进行分组
        val groupSize = 10
        for (i in 0 until unmasteredWords.size step groupSize) {
            val endIndex = minOf(i + groupSize, unmasteredWords.size)
            val groupWords = unmasteredWords.subList(i, endIndex)
            
            // 创建队列并添加单词
            val wordQueue = ArrayDeque<WordItem>()
            wordQueue.addAll(groupWords)
            wordGroups.add(wordQueue)
        }
        
        // 设置当前队列
        if (wordGroups.isNotEmpty()) {
            currentWordQueue = wordGroups.removeAt(0)
        }

        // 设置返回按钮的点击事件
        btnBack.setOnClickListener {
            finish()
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
        
        // 继续按钮的点击事件
        btnCloseOverlay.setOnClickListener {
            errorOverlay.visibility = View.GONE
            loadNextWord()
        }
        
        // 下一步按钮的点击事件
        btnNext.setOnClickListener {
            loadNextWord()
        }
        

    }
    
    // 处理从ErrorWordActivity返回的结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_ERROR_WORD && resultCode == ErrorWordActivity.RESULT_NEXT_WORD) {
            // 检查是否需要加载下一个单词或跳转到错词再选页面
            val shouldLoadNextWord = data?.getBooleanExtra(ErrorWordActivity.EXTRA_LOAD_NEXT_WORD, false) ?: false
            val shouldRetryWord = data?.getBooleanExtra("SHOULD_RETRY_WORD", false) ?: false
            
            if (shouldRetryWord && currentWord != null) {
                // 标记进入错词再选流程
                isInRetryFlow = true
                
                // 跳转到错词再选页面
                val intent = Intent(this, ErrorWordRetryActivity::class.java)
                intent.putExtra("WORD_ITEM", currentWord)
                startActivityForResult(intent, REQUEST_CODE_ERROR_WORD_RETRY)
            } else if (shouldLoadNextWord) {
                // 加载下一个单词
                loadNextWord()
            }
        } else if (requestCode == REQUEST_CODE_ERROR_WORD_RETRY && resultCode == ErrorWordRetryActivity.RESULT_RETRY_COMPLETED) {
            // 从错词再选页面返回
            val isCorrect = data?.getBooleanExtra(ErrorWordRetryActivity.EXTRA_IS_CORRECT, false) ?: false
            
            if (isCorrect) {
                // 回答正确，继续下一个单词
                isInRetryFlow = false // 退出错词再选流程
                loadNextWord()
            } else {
                // 回答错误，重新显示错词再选页面
                if (currentWord != null) {
                    val intent = Intent(this, ErrorWordRetryActivity::class.java)
                    intent.putExtra("WORD_ITEM", currentWord)
                    startActivityForResult(intent, REQUEST_CODE_ERROR_WORD_RETRY)
                }
            }
        }
    }
    
    private fun loadNextWord() {
        try {
            resetOptionButtons()
            
            // 重置错误计数器和已选错误选项
            errorCount = 0
            selectedWrongOptions.clear()
            isFirstAttempt = true
            queueOperationPerformed = false // 重置队列操作标志
            
            // 隐藏下一步按钮
            btnNext.visibility = View.GONE
            
            // 检查是否所有单词都已掌握
            if (currentWordList.isEmpty()) {
                // 所有单词都已掌握，显示完成信息
                showCompletionMessage()
                return
            }

            // 根据错误次数加权选择下一个单词
            currentWord = selectWordByWeight()

            // 显示单词
            currentWord?.let { word ->
                tvWord.text = word.word
                
                try {
                    // 生成选项和选项单词映射
                    val options = generateOptions(word)
                    
                    // 安全地设置选项按钮的文本
                    if (options.size >= 4) {
                        btnOption1.text = options[0]
                        btnOption2.text = options[1]
                        btnOption3.text = options[2]
                        btnOption4.text = options[3]
                    } else {
                        android.util.Log.e("QuizActivity", "Not enough options generated")
                        // 如果选项不足，显示错误信息
                        // 移除生成选项失败提示
                    }
                } catch (e: Exception) {
                    android.util.Log.e("QuizActivity", "Error generating options: ${e.message}")
                    // 移除生成选项错误提示
                }
            }

            // 更新显示
            if (isReview) {
                tvRemaining.text = "${reviewRound}轮复习 - 文言词汇"
            } else {
                tvRemaining.text = "学习模式 - 文言词汇"
            }
            updateProgressDisplay()
        } catch (e: Exception) {
            // 移除加载词语失败提示
        }
    }
    
    private fun generateOptions(correctWord: WordItem): List<String> {
        val options = mutableListOf<String>()
        val optionMap = mutableMapOf<String, String>()
        
        try {
            // 添加正确选项
            options.add(correctWord.definition)
            optionMap[correctWord.definition] = correctWord.word
            
            // 生成错误选项
            val random = Random
            val allWords = VocabList.allWords.toList()
            
            // 尝试添加错误选项，但避免无限循环
            var attempts = 0
            val maxAttempts = 100 // 防止无限循环
            
            while (options.size < 4 && attempts < maxAttempts) {
                attempts++
                
                if (allWords.isEmpty()) {
                    android.util.Log.e("QuizActivity", "No words available for generating options")
                    break
                }
                
                val randomWord = allWords[random.nextInt(allWords.size)]
                // 确保错误选项不是正确选项，并且不重复
                if (randomWord.definition != correctWord.definition && !options.contains(randomWord.definition)) {
                    options.add(randomWord.definition)
                    optionMap[randomWord.definition] = randomWord.word
                }
            }
            
            // 如果选项不足，填充占位符
            while (options.size < 4) {
                val placeholder = "选项${options.size + 1}"
                options.add(placeholder)
                optionMap[placeholder] = "未知"
            }
            
            // 洗牌选项
            options.shuffle()
            
            // 保存选项和单词的映射关系
            optionWordMap = optionMap
        } catch (e: Exception) {
            android.util.Log.e("QuizActivity", "Exception in generateOptions: ${e.message}")
            // 返回默认选项以防崩溃
            return listOf("选项1", "选项2", "选项3", "选项4")
        }
        
        return options
    }
    
    private fun handleOptionClick(button: Button) {
        val selectedOption = button.text.toString().split('\n')[0] // 只取第一行文本
        val correctDefinition = currentWord?.definition ?: ""
        val wordToProcess = currentWord // 保存当前要处理的单词

        if (selectedOption == correctDefinition) {
            // 回答正确，将按钮设为#DEEFE7
            button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DEEFE7"))
            button.setTextColor(resources.getColor(android.R.color.black, theme))

            // 播放正确声音
            soundManager.playSound(SoundManager.SOUND_CORRECT)

            // 禁用所有按钮
            disableAllOptionButtons()

            // 处理单词答对的情况
            wordToProcess?.let { handleWordCorrect(it) }

            // 一次性答对，0.5秒后下一题；否则（答错后答对）跳转到错词页面
            if (isFirstAttempt) {
                Handler(Looper.getMainLooper()).postDelayed({
                    loadNextWord()
                }, 500) // 500毫秒 = 0.5秒
            } else {
                // 答错后答对，跳转到错词页面
                wordToProcess?.let {
                    try {
                        val intent = Intent(this, ErrorWordActivity::class.java)
                        intent.putExtra("WORD_ITEM", it)
                        intent.putExtra(ErrorWordActivity.EXTRA_LOAD_NEXT_WORD, true)
                        startActivityForResult(intent, REQUEST_CODE_ERROR_WORD)
                    } catch (e: Exception) {
                        android.util.Log.e("QuizActivity", "Error starting ErrorWordActivity: ${e.message}")
                        // 移除启动错词页面失败提示
                        
                        // 如果启动失败，至少要能继续下一个单词
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadNextWord()
                        }, 500)
                    }
                }
            }
        } else {
                // 回答错误，增加错误次数
                errorCount++
                selectedWrongOptions.add(selectedOption)

                // 播放错误声音
                soundManager.playSound(SoundManager.SOUND_INCORRECT)

            // 将错误选项设为#F1CFD0
            button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1CFD0"))
            button.setTextColor(resources.getColor(android.R.color.black, theme))
            // 缩小错误选项的字体
            button.textSize = 14f

            // 标记为非首次尝试
            if (isFirstAttempt) {
                isFirstAttempt = false
            }

            // 在错误选项中将正确单词放在上方，释义放在下方
            val originalText = selectedOption
            val correctWord = optionWordMap[selectedOption] ?: ""

            // 创建SpannableString，正确单词在上，释义在下
            val spannableString = SpannableString("$correctWord\n$originalText")

            // 正确单词颜色保持黑色
            // 设置第二行文本（释义）为#66697A颜色
            val secondLineStart = correctWord.length + 1
            spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#66697A")), secondLineStart, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            button.text = spannableString

            // 移除错误提示弹窗

            // 处理单词答错的情况
            wordToProcess?.let { handleWordIncorrect(it) }

            // 如果达到最大错误次数，跳转到错误单词页面
            if (errorCount >= MAX_ERROR_COUNT) {
                wordToProcess?.let { word ->
                    val intent = Intent(this, ErrorWordActivity::class.java)
                    intent.putExtra("WORD_ITEM", wordToProcess)
                    intent.putExtra("SHOULD_RETRY_WORD", true)
                    startActivityForResult(intent, REQUEST_CODE_ERROR_WORD)
                }
            }
        }
    }
    
    private fun resetOptionButtons() {
        // 重置所有选项按钮的样式
        val buttons = listOf(btnOption1, btnOption2, btnOption3, btnOption4)
        buttons.forEach {
            it.backgroundTintList = resources.getColorStateList(android.R.color.white, theme)
            it.setTextColor(resources.getColor(android.R.color.black, theme))
            it.isEnabled = true
        }
    }
    
    private fun disableAllOptionButtons() {
        // 禁用所有选项按钮
        btnOption1.isEnabled = false
        btnOption2.isEnabled = false
        btnOption3.isEnabled = false
        btnOption4.isEnabled = false
    }
    
    // 复习队列（暂时保留，但当前实现主要使用单词分组队列）
    private val reviewQueue = mutableListOf<WordItem>()
    
    // 从队列中选择单词
    private fun selectWordByWeight(): WordItem? {
        if (currentWordList.isEmpty()) return null
        
        // 检查当前队列是否为空，如果为空则尝试获取下一个队列
        if (currentWordQueue == null || currentWordQueue!!.isEmpty()) {
            if (wordGroups.isNotEmpty()) {
                currentWordQueue = wordGroups.removeAt(0)
            } else {
                // 如果所有队列都已处理完，返回null
                return null
            }
        }
        
        // 从当前队列中获取单词但不移除
        return currentWordQueue?.peekFirst()
    }
    
    // 处理单词答对的情况
    private fun handleWordCorrect(word: WordItem) {
        // 如果是一次性答对，从队列中移除单词并标记为已掌握
        if (isFirstAttempt) {
            // 从队列中弹出单词
            currentWordQueue?.pollFirst()
            
            try {
                // 标记为已掌握
                ProgressManager.addMasteredWord(this, word.id)

                // 从当前单词列表中移除这个单词
                currentWordList = currentWordList.filter { it.id != word.id }
                
                // 从错误次数映射中移除
                wordErrorCounts.remove(word.id)

                // 更新剩余单词列表
                sampleWords = sampleWords?.filter { it.id != word.id }
            } catch (e: Exception) {
                android.util.Log.e("QuizActivity", "Error marking word as mastered: ${e.message}")
            }
        }
    }
    
    // 处理单词答错的情况
    private fun handleWordIncorrect(word: WordItem) {
        // 只有在非错词再选流程中，并且当前单词还没有执行过队列操作时，才执行队列操作
        if (!isInRetryFlow && !queueOperationPerformed) {
            // 从队列中弹出单词并重新插入到队尾
            currentWordQueue?.pollFirst()
            currentWordQueue?.offerLast(word)
            
            // 标记已经执行过队列操作
            queueOperationPerformed = true
        }
        
        // 增加该单词的错误次数
        val currentErrors = wordErrorCounts.getOrDefault(word.id, 0)
        wordErrorCounts[word.id] = currentErrors + 1
    }
    
    private fun updateProgressDisplay() {
        // 更新进度显示
        val (masteredCount, totalCount) = ProgressManager.getProgress(this)
        tvProgress.text = "进度：$masteredCount/$totalCount"
    }
    
    private fun showCompletionMessage() {
        // 创建跳转到CompletionActivity的意图
        val intent = Intent(this, CompletionActivity::class.java)
        intent.putExtra("IS_REVIEW", isReview)
        startActivity(intent)
        finish() // 结束当前活动，防止用户返回到空的QuizActivity
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 释放声音管理器资源
        soundManager.release()
    }
}