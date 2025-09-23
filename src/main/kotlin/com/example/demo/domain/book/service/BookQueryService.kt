package com.example.demo.domain.book.service

import com.example.demo.application.book.input.QueryBooksUseCase
import com.example.demo.application.book.port.out.QueryBooksPort
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.Book

class BookQueryService(
    private val queryBooksPort: QueryBooksPort,
) : QueryBooksUseCase {
    override fun findByAuthor(authorId: AuthorId): List<Book> = queryBooksPort.findByAuthorId(authorId)
}
