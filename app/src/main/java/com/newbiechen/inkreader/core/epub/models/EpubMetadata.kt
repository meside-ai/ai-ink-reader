package com.newbiechen.inkreader.core.epub.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EpubMetadata(
    val title: String,
    val author: String,
    val publisher: String? = null,
    val language: String = "zh-CN",
    val identifier: String? = null,
    val description: String? = null,
    val subjects: List<String> = emptyList(),
    val publishDate: String? = null,
    val rights: String? = null,
    val coverImagePath: String? = null,
    val totalChapters: Int = 0,
    val wordCount: Int = 0
) : Parcelable

@Parcelize
data class EpubChapter(
    val id: String,
    val title: String,
    val href: String,
    val order: Int,
    val htmlContent: String = "",
    val wordCount: Int = 0
) : Parcelable

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val warnings: List<String> = emptyList()
) 