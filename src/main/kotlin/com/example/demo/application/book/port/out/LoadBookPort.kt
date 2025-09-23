package com.example.demo.application.book.port.out

import com.example.demo.domain.book.Book
import com.example.demo.domain.book.BookId

interface LoadBookPort {
    fun findById(id: BookId): Book?
}
