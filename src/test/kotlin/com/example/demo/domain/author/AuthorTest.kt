package com.example.demo.domain.author

import com.example.demo.domain.book.BookId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class AuthorTest {
    @Test
    fun `名前が空白の場合は例外`() {
        assertThatThrownBy {
            Author.new(
                name = " ",
                birthDate = LocalDate.now().minusYears(20),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("著者名")
    }

    @Test
    fun `生年月日が未来の場合は例外`() {
        assertThatThrownBy {
            Author.new(
                name = "山田太郎",
                birthDate = LocalDate.now().plusDays(1),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("生年月日")
    }

    @Test
    fun `書籍を追加すると著者が保持する書籍IDが増える`() {
        val author =
            Author.new(
                name = "山田太郎",
                birthDate = LocalDate.now().minusYears(30),
            )
        val bookId = BookId.from(UUID.randomUUID())

        author.addBook(bookId)

        assertThat(author.bookIds).contains(bookId)
    }

    @Test
    fun `書籍を削除すると著者が保持する書籍IDから除外される`() {
        val bookId = BookId.from(UUID.randomUUID())
        val author =
            Author.new(
                name = "山田太郎",
                birthDate = LocalDate.now().minusYears(30),
                bookIds = setOf(bookId),
            )

        author.removeBook(bookId)

        assertThat(author.bookIds).doesNotContain(bookId)
    }
}
