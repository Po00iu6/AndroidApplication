package com.example.myapplication.utils

import android.content.Context
import com.example.myapplication.model.VocabList
import com.example.myapplication.model.WordItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ProgressManager {
    private const val PREF_NAME = "VocabProgress"
    private const val KEY_MASTERED_WORDS = "mastered_words"
    private const val KEY_REVIEW_ROUND = "review_round"

    // 保存和获取已掌握的单词
    fun saveMasteredWords(context: Context, masteredIds: List<Int>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_MASTERED_WORDS, Gson().toJson(masteredIds))
            .apply()
    }

    fun getMasteredWords(context: Context): List<Int> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_MASTERED_WORDS, "[]") ?: "[]"
        return Gson().fromJson(json, object : TypeToken<List<Int>>() {}.type)
    }

    // 添加QuizActivity中使用的方法
    fun getMasteredWordIds(context: Context): List<Int> {
        return getMasteredWords(context)
    }

    fun addMasteredWord(context: Context, wordId: Int) {
        val currentMasteredWords = getMasteredWords(context).toMutableList()
        if (!currentMasteredWords.contains(wordId)) {
            currentMasteredWords.add(wordId)
            saveMasteredWords(context, currentMasteredWords)
            refreshWordMastery(context)
        }
    }

    fun refreshWordMastery(context: Context) {
        val masteredIds = getMasteredWords(context)
        VocabList.allWords.forEach { word ->
            word.mastered = masteredIds.contains(word.id)
        }
    }

    fun getProgress(context: Context): Pair<Int, Int> {
        refreshWordMastery(context)
        // 获取单词总数
        val totalWords = VocabList.allWords.size
        // 获取已掌握的单词数
        val masteredCount = getMasteredWords(context).size
        return Pair(masteredCount, totalWords)
    }

    fun isRoundCompleted(context: Context): Boolean {
        val (masteredCount, totalCount) = getProgress(context)
        return masteredCount == totalCount
    }

    fun resetProgress(context: Context) {
        saveMasteredWords(context, emptyList())
        refreshWordMastery(context)
    }

    // 重置所有软件数据，包括进度和复习轮次
    fun resetAllData(context: Context) {
        // 重置已掌握的单词
        saveMasteredWords(context, emptyList())
        
        // 重置复习轮次
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_REVIEW_ROUND, 0).apply()
        
        // 更新单词掌握状态
        refreshWordMastery(context)
    }

    // 复习轮次相关方法
    fun getReviewRound(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_REVIEW_ROUND, 0)
    }

    fun incrementReviewRound(context: Context) {
        val currentRound = getReviewRound(context) + 1
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_REVIEW_ROUND, currentRound).apply()
        // 重置已掌握的单词，开始新的复习轮次
        resetProgress(context)
    }


}