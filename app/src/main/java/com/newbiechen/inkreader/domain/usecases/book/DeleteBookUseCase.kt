package com.newbiechen.inkreader.domain.usecases.book

import javax.inject.Inject
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.UseCase

/**
 * 删除图书UseCase
 * 
 * 执行图书的软删除操作，保留数据完整性
 */
class DeleteBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : UseCase<DeleteBookUseCase.Params, Unit>() {
    
    override suspend fun execute(parameters: Params): Unit {
        if (!parameters.validate()) {
            throw IllegalArgumentException("Invalid book ID: ${parameters.bookId}")
        }
        
        // Repository的deleteBook方法返回Result<Unit>，需要提取结果
        bookRepository.deleteBook(parameters.bookId).getOrThrow()
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