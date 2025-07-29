package com.newbiechen.inkreader.domain.usecases.book

import com.newbiechen.inkreader.domain.entities.Chapter
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.domain.usecases.base.UseCase
import javax.inject.Inject

/**
 * 获取图书章节UseCase
 */
class GetBookChaptersUseCase @Inject constructor(
    private val bookRepository: BookRepository
) : UseCase<String, List<Chapter>>() {
    
    override suspend fun execute(parameters: String): List<Chapter> {
        return bookRepository.getBookChapters(parameters)
    }
} 