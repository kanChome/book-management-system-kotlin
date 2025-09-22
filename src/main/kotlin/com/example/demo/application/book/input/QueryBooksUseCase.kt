package com.example.demo.application.book.input

import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.Book

interface QueryBooksUseCase {
    fun findByAuthor(authorId: AuthorId): List<Book>
}
