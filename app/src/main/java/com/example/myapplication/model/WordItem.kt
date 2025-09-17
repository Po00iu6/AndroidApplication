package com.example.myapplication.model

import java.io.Serializable

data class WordItem(
    val id: Int,
    val word: String,
    val definition: String,
    var mastered: Boolean
) : Serializable