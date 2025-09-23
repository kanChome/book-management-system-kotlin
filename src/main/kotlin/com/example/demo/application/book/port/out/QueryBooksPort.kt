package com.example.demo.application.book.port.out

import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.Book

interface QueryBooksPort {
    fun findByAuthorId(authorId: AuthorId): List<Book>
}
