package com.newbiechen.inkreader.domain.usecases.book

import javax.inject.Inject
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.UseCase

/**
 * 获取章节内容UseCase
 * 
 * 从EPUB文件中提取指定章节的HTML内容
 */
class GetChapterContentUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : UseCase<GetChapterContentUseCase.Params, String>() {
    
    override suspend fun execute(parameters: Params): String {
        if (!parameters.validate()) {
            throw IllegalArgumentException("Invalid parameters: bookId=${parameters.bookId}, chapterIndex=${parameters.chapterIndex}")
        }
        
        // Repository的getChapterContent方法返回Result<String>，需要提取结果
        return bookRepository.getChapterContent(parameters.bookId, parameters.chapterIndex).getOrThrow()
    }
    
    /**
     * UseCase参数
     */
    data class Params(
        val bookId: String,
        val chapterIndex: Int
    ) {
        /**
         * 验证参数有效性
         */
        fun validate(): Boolean {
            return bookId.isNotBlank() && chapterIndex >= 0
        }
    }
} 