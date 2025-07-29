package com.newbiechen.inkreader.domain.usecases.book

import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.UseCase
import javax.inject.Inject

/**
 * 根据ID获取图书UseCase
 */
class GetBookByIdUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : UseCase<String, Book?>() {
    
    override suspend fun execute(parameters: String): Book? {
        return bookRepository.getBookById(parameters)
    }
} 