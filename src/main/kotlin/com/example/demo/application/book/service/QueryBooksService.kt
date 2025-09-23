package com.example.demo.application.book.service

import com.example.demo.application.book.input.QueryBooksUseCase
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.service.BookQueryService
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Primary
class QueryBooksService(
    private val delegate: BookQueryService,
) : QueryBooksUseCase {
    @Transactional(readOnly = true)
    override fun findByAuthor(authorId: AuthorId): List<Book> = delegate.findByAuthor(authorId)
}
