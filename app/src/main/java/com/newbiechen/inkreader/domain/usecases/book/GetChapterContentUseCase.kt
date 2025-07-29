package com.newbiechen.inkreader.domain.usecases.book

import com.newbiechen.inkreader.domain.entities.Chapter
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.UseCase
import javax.inject.Inject

/**
 * 获取章节内容UseCase参数
 */
data class GetChapterContentParams(
    val bookId: String,
    val chapterId: String
)

/**
 * 获取章节内容UseCase
 */
class GetChapterContentUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : UseCase<GetChapterContentParams, Chapter?>() {
    
    override suspend fun execute(parameters: GetChapterContentParams): Chapter? {
        val chapters = bookRepository.getBookChapters(parameters.bookId)
        return chapters.find { it.chapterId == parameters.chapterId }
    }
} 