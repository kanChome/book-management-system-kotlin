package com.example.demo.application.book.port.out

import com.example.demo.domain.book.Book

interface SaveBookPort {
    fun save(book: Book): Book
}
