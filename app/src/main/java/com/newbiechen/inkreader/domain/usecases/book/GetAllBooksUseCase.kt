package com.newbiechen.inkreader.domain.usecases.book

import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.repositories.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取所有图书UseCase
 */
class GetAllBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    
    /**
     * 获取所有图书（响应式）
     */
    operator fun invoke(): Flow<List<Book>> {
        return bookRepository.getAllBooks()
    }
} 