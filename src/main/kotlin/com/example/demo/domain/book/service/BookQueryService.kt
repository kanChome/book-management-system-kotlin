package com.example.demo.domain.book.service

import com.example.demo.application.book.input.QueryBooksUseCase
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.port.BookRepository

class BookQueryService(
    private val bookRepository: BookRepository,
) : QueryBooksUseCase {
    override fun findByAuthor(authorId: AuthorId): List<Book> = bookRepository.findByAuthorId(authorId)
}
