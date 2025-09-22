package com.example.demo.application.book.input

import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.BookId
import com.example.demo.domain.book.BookStatus
import java.math.BigDecimal

/**
 * 書籍登録・更新ユースケースの入力ポート。
 */
interface RegisterBookUseCase {
    fun register(command: RegisterBookCommand): Book

    data class RegisterBookCommand(
        val title: String,
        val price: BigDecimal,
        val authorIds: Collection<AuthorId>,
        val bookId: BookId? = null,
        val status: BookStatus = BookStatus.UNPUBLISHED,
    )
}
