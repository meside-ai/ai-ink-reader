package com.newbiechen.inkreader.domain.usecases.book

import javax.inject.Inject
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.UseCase

/**
 * 根据ID获取图书UseCase
 * 
 * 根据图书ID查询图书详细信息
 */
class GetBookByIdUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : UseCase<GetBookByIdUseCase.Params, Book?>() {
    
    override suspend fun execute(parameters: Params): Book? {
        if (!parameters.validate()) {
            throw IllegalArgumentException("Invalid book ID: ${parameters.bookId}")
        }
        
        return bookRepository.getBookById(parameters.bookId)
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