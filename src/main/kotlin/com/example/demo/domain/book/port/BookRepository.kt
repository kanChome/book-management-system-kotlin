package com.example.demo.domain.book.port

import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.BookId

/**
 * 書籍集約の永続化ポート。インフラ層は本インターフェースを実装して I/O を担う。
 */
interface BookRepository {
    fun findById(id: BookId): Book?

    fun findByAuthorId(authorId: AuthorId): List<Book>

    fun save(book: Book): Book
}
