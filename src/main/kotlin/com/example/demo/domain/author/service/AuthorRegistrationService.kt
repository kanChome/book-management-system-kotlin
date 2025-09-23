package com.example.demo.domain.author.service

import com.example.demo.application.author.input.RegisterAuthorUseCase
import com.example.demo.application.author.port.out.LoadAuthorPort
import com.example.demo.application.author.port.out.SaveAuthorPort
import com.example.demo.application.book.port.out.LoadBookPort
import com.example.demo.application.book.port.out.SaveBookPort
import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.author.exception.AuthorNotFoundException
import com.example.demo.domain.book.BookId
import com.example.demo.domain.book.exception.MissingBookException
import java.util.UUID

class AuthorRegistrationService(
    private val saveAuthorPort: SaveAuthorPort,
    private val loadAuthorPort: LoadAuthorPort,
    private val saveBookPort: SaveBookPort,
    private val loadBookPort: LoadBookPort,
) : RegisterAuthorUseCase {
    override fun register(command: RegisterAuthorUseCase.RegisterAuthorCommand): Author {
        val requiredBookIds = command.bookIds.toSet()
        val books = requiredBookIds.mapNotNull { loadBookPort.findById(it) }
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
        val savedAuthor = saveAuthorPort.save(author)

        books.forEach { book ->
            if (!book.authorIds.contains(savedAuthor.id)) {
                book.addAuthor(savedAuthor.id)
                saveBookPort.save(book)
            }
        }

        return savedAuthor
    }

    override fun update(command: RegisterAuthorUseCase.UpdateAuthorCommand): Author {
        val requiredBookIds = command.bookIds.toSet()
        val books = requiredBookIds.mapNotNull { loadBookPort.findById(it) }
        val missingBookIds = requiredBookIds - books.map { it.id }.toSet()
        if (missingBookIds.isNotEmpty()) {
            throw MissingBookException(missingBookIds)
        }

        val existingAuthor = loadAuthorPort.findById(command.authorId) ?: throw AuthorNotFoundException(command.authorId)

        val updatedAuthor =
            Author.new(
                name = command.name,
                birthDate = command.birthDate,
                bookIds = requiredBookIds,
                id = existingAuthor.id,
                clock = command.clock,
            )

        val currentBookIds = existingAuthor.bookIds
        val booksToAdd = requiredBookIds - currentBookIds
        val booksToRemove = currentBookIds - requiredBookIds

        val savedAuthor = saveAuthorPort.save(updatedAuthor)

        booksToAdd.forEach { bookId ->
            val book = loadBookPort.findById(bookId)
            if (book != null && !book.authorIds.contains(savedAuthor.id)) {
                book.addAuthor(savedAuthor.id)
                saveBookPort.save(book)
            }
        }

        booksToRemove.forEach { bookId ->
            val book = loadBookPort.findById(bookId)
            if (book != null && book.authorIds.contains(savedAuthor.id)) {
                book.removeAuthor(savedAuthor.id)
                saveBookPort.save(book)
            }
        }

        return savedAuthor
    }
}
