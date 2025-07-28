package com.newbiechen.inkreader.domain.usecases.book

import javax.inject.Inject
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.UseCase

/**
 * 添加图书UseCase
 * 
 * 处理EPUB文件导入，包括解析、验证和数据库存储
 */
class AddBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : UseCase<AddBookUseCase.Params, Book>() {
    
    override suspend fun execute(parameters: Params): Book {
        // Repository的addBook方法返回Result<Book>，需要提取结果
        return bookRepository.addBook(parameters.filePath).getOrThrow()
    }
    
    /**
     * UseCase参数
     */
    data class Params(
        val filePath: String
    ) {
        /**
         * 验证参数有效性
         */
        fun validate(): Boolean {
            return filePath.isNotBlank() && filePath.lowercase().endsWith(".epub")
        }
    }
} 