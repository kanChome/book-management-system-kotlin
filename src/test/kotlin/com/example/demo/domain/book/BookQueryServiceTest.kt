package com.example.demo.domain.book

import com.example.demo.application.book.input.QueryBooksUseCase
import com.example.demo.application.book.port.out.BookRepository
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.BookId
import com.example.demo.domain.book.BookStatus
import com.example.demo.domain.book.service.BookQueryService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class BookQueryServiceTest {
    private lateinit var bookRepository: FakeBookRepository
    private lateinit var service: QueryBooksUseCase

    @BeforeEach
    fun setUp() {
        bookRepository = FakeBookRepository()
        service = BookQueryService(bookRepository)
    }

    @Test
    fun `著者IDで紐づく書籍を取得できる`() {
        val authorId = AuthorId.from(UUID.randomUUID())
        val otherAuthorId = AuthorId.from(UUID.randomUUID())
        val bookA =
            Book.new(
                title = "ドメイン駆動設計",
                price = BigDecimal.valueOf(3000),
                authorIds = listOf(authorId),
                id = BookId.new(),
                status = BookStatus.UNPUBLISHED,
            )
        val bookB =
            Book.new(
                title = "Kotlin実践",
                price = BigDecimal.valueOf(2500),
                authorIds = listOf(authorId, otherAuthorId),
                id = BookId.new(),
                status = BookStatus.UNPUBLISHED,
            )
        val bookC =
            Book.new(
                title = "別著者の本",
                price = BigDecimal.valueOf(1800),
                authorIds = listOf(otherAuthorId),
                id = BookId.new(),
                status = BookStatus.UNPUBLISHED,
            )
        bookRepository.save(bookA)
        bookRepository.save(bookB)
        bookRepository.save(bookC)

        val result = service.findByAuthor(authorId)

        assertThat(result)
            .extracting<String> { it.title }
            .containsExactlyInAnyOrder("ドメイン駆動設計", "Kotlin実践")
    }

    private class FakeBookRepository : BookRepository {
        private val books = mutableListOf<Book>()

        override fun findById(id: BookId): Book? = books.firstOrNull { it.id == id }

        override fun findByAuthorId(authorId: AuthorId): List<Book> = books.filter { it.authorIds.contains(authorId) }

        override fun save(book: Book): Book {
            books.removeIf { it.id == book.id }
            books.add(book)
            return book
        }
    }
}
