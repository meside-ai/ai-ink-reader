package com.newbiechen.inkreader.domain.usecases.book

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.NoParameterFlowUseCase

/**
 * 获取所有图书UseCase
 * 
 * 返回响应式的图书列表Flow，支持实时数据更新
 */
class GetAllBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : NoParameterFlowUseCase<List<Book>>() {
    
    override suspend fun execute(parameters: Unit): Flow<List<Book>> {
        return bookRepository.getAllBooks()
    }
} 