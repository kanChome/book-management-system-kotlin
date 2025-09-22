package com.example.demo.domain.book.exception

import com.example.demo.domain.book.BookId

class BookNotFoundException(
    bookId: BookId,
) : IllegalStateException("書籍が見つかりません: ${bookId.value}")
