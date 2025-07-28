package com.newbiechen.inkreader.domain.usecases.book

import javax.inject.Inject
import com.newbiechen.inkreader.domain.entities.Chapter
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.UseCase

/**
 * 获取图书章节列表UseCase
 * 
 * 根据图书ID获取该图书的所有章节信息
 */
class GetBookChaptersUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : UseCase<GetBookChaptersUseCase.Params, List<Chapter>>() {
    
    override suspend fun execute(parameters: Params): List<Chapter> {
        if (!parameters.validate()) {
            throw IllegalArgumentException("Invalid book ID: ${parameters.bookId}")
        }
        
        return bookRepository.getBookChapters(parameters.bookId)
    }
    
    /**
     * UseCase参数
     */
    data class Params(
        val bookId: String
    ) {
        /**
         * 验证参数有效性
         */
        fun validate(): Boolean {
            return bookId.isNotBlank()
        }
    }
} 