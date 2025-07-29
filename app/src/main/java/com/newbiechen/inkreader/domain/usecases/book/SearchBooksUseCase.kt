package com.newbiechen.inkreader.domain.usecases.book

import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.UseCase
import javax.inject.Inject

/**
 * 搜索图书UseCase
 */
class SearchBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : UseCase<String, List<Book>>() {
    
    override suspend fun execute(parameters: String): List<Book> {
        return bookRepository.searchBooks(parameters)
    }
} 