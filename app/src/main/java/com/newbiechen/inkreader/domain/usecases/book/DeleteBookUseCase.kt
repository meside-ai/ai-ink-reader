package com.newbiechen.inkreader.domain.usecases.book

import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.UseCase
import javax.inject.Inject

/**
 * 删除图书UseCase
 */
class DeleteBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : UseCase<String, Unit>() {
    
    override suspend fun execute(parameters: String) {
        val result = bookRepository.deleteBook(parameters)
        result.getOrThrow()
    }
} 