package com.newbiechen.inkreader.domain.usecases.book

import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.UseCase
import javax.inject.Inject

/**
 * 添加图书UseCase
 */
class AddBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : UseCase<Book, Book>() {
    
    override suspend fun execute(parameters: Book): Book {
        val result = bookRepository.addBook(parameters)
        return result.getOrThrow()
    }
} 