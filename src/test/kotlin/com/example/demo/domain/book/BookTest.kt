package com.example.demo.domain.book

import com.example.demo.domain.author.AuthorId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class BookTest {
    @Nested
    inner class Creation {
        @Test
        fun `タイトルが空文字の場合は例外`() {
            assertThatThrownBy {
                Book.new(
                    title = " ",
                    price = BigDecimal.ZERO,
                    authorIds = listOf(AuthorId.from(UUID.randomUUID())),
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("タイトル")
        }

        @Test
        fun `価格がマイナスの場合は例外`() {
            assertThatThrownBy {
                Book.new(
                    title = "Kotlin入門",
                    price = BigDecimal.valueOf(-1),
                    authorIds = listOf(AuthorId.from(UUID.randomUUID())),
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("価格")
        }

        @Test
        fun `著者が空の場合は例外`() {
            assertThatThrownBy {
                Book.new(
                    title = "Kotlin入門",
                    price = BigDecimal.TEN,
                    authorIds = emptyList(),
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("著者")
        }
    }

    @Test
    fun `著者を削除して0人になる場合は例外`() {
        val authorId = AuthorId.from(UUID.randomUUID())
        val book =
            Book.new(
                title = "ドメイン駆動設計",
                price = BigDecimal.valueOf(3000),
                authorIds = listOf(authorId),
            )

        assertThatThrownBy { book.removeAuthor(authorId) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("著者")
    }

    @Test
    fun `出版済みから未出版に戻すことはできない`() {
        val authors = listOf(AuthorId.from(UUID.randomUUID()))
        val book =
            Book.new(
                title = "ドメイン実践",
                price = BigDecimal.valueOf(2800),
                authorIds = authors,
                status = BookStatus.PUBLISHED,
            )

        assertThatThrownBy { book.changeStatus(BookStatus.UNPUBLISHED) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("出版済み")
    }

    @Test
    fun `ステータス変更が成功すると状態が更新される`() {
        val authors = listOf(AuthorId.from(UUID.randomUUID()))
        val book =
            Book.new(
                title = "ドメイン実践",
                price = BigDecimal.valueOf(2800),
                authorIds = authors,
                status = BookStatus.UNPUBLISHED,
            )

        book.changeStatus(BookStatus.PUBLISHED)

        assertThat(book.status).isEqualTo(BookStatus.PUBLISHED)
    }
}
