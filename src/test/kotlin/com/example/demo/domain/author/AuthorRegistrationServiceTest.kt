package com.example.demo.domain.author

import com.example.demo.application.author.input.RegisterAuthorUseCase
import com.example.demo.domain.author.port.AuthorRepository
import com.example.demo.domain.author.service.AuthorRegistrationService
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.BookId
import com.example.demo.domain.book.exception.MissingBookException
import com.example.demo.domain.book.port.BookRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class AuthorRegistrationServiceTest {
    private lateinit var authorRepository: FakeAuthorRepository
    private lateinit var bookRepository: FakeBookRepository
    private lateinit var service: RegisterAuthorUseCase

    @BeforeEach
    fun setUp() {
        authorRepository = FakeAuthorRepository()
        bookRepository = FakeBookRepository()
        service = AuthorRegistrationService(authorRepository, bookRepository)
    }

    @Nested
    inner class RegisterAuthor {
        @Test
        fun `紐付ける書籍が存在すれば著者と書籍が更新される`() {
            val bookId = BookId.new()
            bookRepository.books[bookId] =
                Book.new(
                    title = "Kotlin入門",
                    price = BigDecimal.valueOf(2800),
                    authorIds = listOf(AuthorId.from(UUID.randomUUID())),
                    id = bookId,
                )

            val command =
                RegisterAuthorUseCase.RegisterAuthorCommand(
                    name = "山田太郎",
                    birthDate = LocalDate.now().minusYears(30),
                    bookIds = listOf(bookId),
                )

            val saved = service.register(command)

            assertThat(authorRepository.authors).hasSize(1)
            assertThat(saved.bookIds).containsExactly(bookId)
            assertThat(bookRepository.books[bookId]?.authorIds).contains(saved.id)
        }

        @Test
        fun `存在しない書籍IDが含まれる場合は例外`() {
            val missingBookId = BookId.new()
            val command =
                RegisterAuthorUseCase.RegisterAuthorCommand(
                    name = "山田太郎",
                    birthDate = LocalDate.now().minusYears(30),
                    bookIds = listOf(missingBookId),
                )

            assertThatThrownBy { service.register(command) }
                .isInstanceOf(MissingBookException::class.java)
                .hasMessageContaining(missingBookId.value.toString())
            assertThat(authorRepository.authors).isEmpty()
        }
    }

    @Test
    fun `既存著者を更新できる`() {
        val originalBookId = BookId.new()
        val newBookId = BookId.new()
        val originalAuthorId = AuthorId.from(UUID.randomUUID())
        val coAuthorId = AuthorId.from(UUID.randomUUID())

        bookRepository.books[originalBookId] =
            Book.new(
                title = "旧書籍",
                price = BigDecimal.valueOf(2000),
                authorIds = listOf(originalAuthorId, coAuthorId),
                id = originalBookId,
            )
        bookRepository.books[newBookId] =
            Book.new(
                title = "新書籍",
                price = BigDecimal.valueOf(1800),
                authorIds = listOf(coAuthorId),
                id = newBookId,
            )

        val existingAuthor =
            Author.new(
                name = "既存著者",
                birthDate = LocalDate.now().minusYears(45),
                bookIds = listOf(originalBookId),
                id = originalAuthorId,
            )
        authorRepository.authors[originalAuthorId] = existingAuthor

        val command =
            RegisterAuthorUseCase.UpdateAuthorCommand(
                authorId = originalAuthorId,
                name = "更新後著者",
                birthDate = LocalDate.now().minusYears(46),
                bookIds = listOf(newBookId),
            )

        val updated = service.update(command)

        assertThat(updated.name).isEqualTo("更新後著者")
        assertThat(updated.bookIds).containsExactly(newBookId)
        assertThat(bookRepository.books[newBookId]?.authorIds).contains(originalAuthorId)
        assertThat(bookRepository.books[originalBookId]?.authorIds).doesNotContain(originalAuthorId)
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
        val books = mutableMapOf<BookId, Book>()

        override fun findById(id: BookId): Book? = books[id]

        override fun findByAuthorId(authorId: AuthorId): List<Book> = books.values.filter { it.authorIds.contains(authorId) }

        override fun save(book: Book): Book {
            books[book.id] = book
            return book
        }
    }
}
