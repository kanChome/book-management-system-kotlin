package com.example.demo.domain.author.service

import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.author.port.AuthorRepository
import com.example.demo.domain.book.BookId
import com.example.demo.domain.book.exception.MissingBookException
import com.example.demo.domain.book.port.BookRepository
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

class AuthorRegistrationService(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository,
) {
    fun register(command: RegisterAuthorCommand): Author {
        val requiredBookIds = command.bookIds.toSet()
        val books = requiredBookIds.mapNotNull { bookRepository.findById(it) }
        val missingBookIds = requiredBookIds - books.map { it.id }.toSet()
        if (missingBookIds.isNotEmpty()) {
            throw MissingBookException(missingBookIds)
        }

        val author =
            Author.new(
                name = command.name,
                birthDate = command.birthDate,
                bookIds = requiredBookIds,
                id = command.authorId ?: AuthorId.from(UUID.randomUUID()),
                clock = command.clock,
            )
        val savedAuthor = authorRepository.save(author)

        books.forEach { book ->
            if (!book.authorIds.contains(savedAuthor.id)) {
                book.addAuthor(savedAuthor.id)
                bookRepository.save(book)
            }
        }

        return savedAuthor
    }

    data class RegisterAuthorCommand(
        val name: String,
        val birthDate: LocalDate,
        val bookIds: Collection<BookId> = emptyList(),
        val authorId: AuthorId? = null,
        val clock: Clock = Clock.systemDefaultZone(),
    )
}
