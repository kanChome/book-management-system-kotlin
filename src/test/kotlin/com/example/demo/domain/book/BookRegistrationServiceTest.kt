package com.example.demo.domain.book

import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.author.port.AuthorRepository
import com.example.demo.domain.book.exception.MissingAuthorException
import com.example.demo.domain.book.port.BookRepository
import com.example.demo.domain.book.service.BookRegistrationService
import com.example.demo.domain.book.service.BookRegistrationService.RegisterBookCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class BookRegistrationServiceTest {
    private lateinit var authorRepository: FakeAuthorRepository
    private lateinit var bookRepository: FakeBookRepository
    private lateinit var service: BookRegistrationService

    @BeforeEach
    fun setUp() {
        authorRepository = FakeAuthorRepository()
        bookRepository = FakeBookRepository()
        service = BookRegistrationService(bookRepository, authorRepository)
    }

    @Nested
    inner class RegisterBook {
        @Test
        fun `全著者が存在する場合は書籍を保存する`() {
            val authorId = AuthorId.from(UUID.randomUUID())
            authorRepository.authors[authorId] =
                Author.new(
                    name = "山田太郎",
                    birthDate = LocalDate.now().minusYears(40),
                    id = authorId,
                )

            val command =
                RegisterBookCommand(
                    title = "クリーンアーキテクチャ入門",
                    price = BigDecimal.valueOf(3200),
                    authorIds = listOf(authorId),
                )

            val saved = service.register(command)

            assertThat(bookRepository.savedBooks).hasSize(1)
            assertThat(saved.title).isEqualTo("クリーンアーキテクチャ入門")
            assertThat(saved.authorIds).containsExactly(authorId)
        }

        @Test
        fun `存在しない著者が含まれる場合は例外`() {
            val existingId = AuthorId.from(UUID.randomUUID())
            authorRepository.authors[existingId] =
                Author.new(
                    name = "山田太郎",
                    birthDate = LocalDate.now().minusYears(40),
                    id = existingId,
                )

            val missingId = AuthorId.from(UUID.randomUUID())
            val command =
                RegisterBookCommand(
                    title = "失敗する書籍",
                    price = BigDecimal.TEN,
                    authorIds = listOf(existingId, missingId),
                )

            assertThatThrownBy { service.register(command) }
                .isInstanceOf(MissingAuthorException::class.java)
                .hasMessageContaining(missingId.value.toString())
            assertThat(bookRepository.savedBooks).isEmpty()
        }
    }

    private class FakeAuthorRepository : AuthorRepository {
        val authors = mutableMapOf<AuthorId, Author>()

        override fun findById(id: AuthorId): Author? = authors[id]

        override fun findAllByIds(ids: Collection<AuthorId>): List<Author> = ids.mapNotNull { authors[it] }

        override fun save(author: Author): Author {
            authors[author.id] = author
            return author
        }
    }

    private class FakeBookRepository : BookRepository {
        val savedBooks = mutableListOf<Book>()

        override fun findById(id: BookId): Book? = savedBooks.firstOrNull { it.id == id }

        override fun findByAuthorId(authorId: AuthorId): List<Book> = savedBooks.filter { it.authorIds.contains(authorId) }

        override fun save(book: Book): Book {
            savedBooks.removeIf { it.id == book.id }
            savedBooks.add(book)
            return book
        }
    }
}
