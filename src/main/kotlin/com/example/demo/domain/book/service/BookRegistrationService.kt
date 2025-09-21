package com.example.demo.domain.book.service

import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.author.port.AuthorRepository
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.BookId
import com.example.demo.domain.book.BookStatus
import com.example.demo.domain.book.exception.MissingAuthorException
import com.example.demo.domain.book.port.BookRepository
import java.math.BigDecimal

class BookRegistrationService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {
    fun register(command: RegisterBookCommand): Book {
        val requiredAuthorIds = command.authorIds.toSet()
        val existingAuthorIds = authorRepository.findAllByIds(requiredAuthorIds).map { it.id }.toSet()
        val missingAuthorIds = requiredAuthorIds - existingAuthorIds
        if (missingAuthorIds.isNotEmpty()) {
            throw MissingAuthorException(missingAuthorIds)
        }

        val book =
            Book.new(
                title = command.title,
                price = command.price,
                authorIds = command.authorIds,
                id = command.bookId ?: BookId.new(),
                status = command.status,
            )
        return bookRepository.save(book)
    }

    data class RegisterBookCommand(
        val title: String,
        val price: BigDecimal,
        val authorIds: Collection<AuthorId>,
        val bookId: BookId? = null,
        val status: BookStatus = BookStatus.UNPUBLISHED,
    )
}
