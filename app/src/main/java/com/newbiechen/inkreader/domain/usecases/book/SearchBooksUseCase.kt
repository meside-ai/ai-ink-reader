package com.newbiechen.inkreader.domain.usecases.book

import javax.inject.Inject
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.UseCase

/**
 * 搜索图书UseCase
 * 
 * 根据关键词搜索图书，支持标题和作者匹配
 */
class SearchBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : UseCase<SearchBooksUseCase.Params, List<Book>>() {
    
    override suspend fun execute(parameters: Params): List<Book> {
        if (!parameters.validate()) {
            return emptyList()
        }
        
        // 预处理查询关键词
        val processedQuery = parameters.query.trim()
        if (processedQuery.isEmpty()) {
            return emptyList()
        }
        
        return bookRepository.searchBooks(processedQuery)
    }
    
    /**
     * UseCase参数
     */
    data class Params(
        val query: String
    ) {
        /**
         * 验证参数有效性
         */
        fun validate(): Boolean {
            return query.isNotBlank() && query.trim().length >= 1
        }
        
        /**
         * 获取清理后的查询字符串
         */
        fun getCleanQuery(): String {
            return query.trim()
        }
    }
} 