package com.example.demo.application.book.port.out

import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.BookId

/**
 * 書籍集約の永続化ポート（Outgoing Port）。
 * インフラ層が実装し、アプリケーション/ドメインからは本ポートを介して永続化I/Oを行う。
 */
interface BookRepository {
    fun findById(id: BookId): Book?

    fun findByAuthorId(authorId: AuthorId): List<Book>

    fun save(book: Book): Book
}
