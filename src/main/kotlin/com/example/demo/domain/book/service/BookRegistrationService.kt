package com.example.demo.domain.book.service

import com.example.demo.application.author.port.out.AuthorRepository
import com.example.demo.application.book.input.RegisterBookUseCase
import com.example.demo.application.book.port.out.BookRepository
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.BookId
import com.example.demo.domain.book.BookStatus
import com.example.demo.domain.book.exception.BookNotFoundException
import com.example.demo.domain.book.exception.MissingAuthorException

class BookRegistrationService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) : RegisterBookUseCase {
    override fun register(command: RegisterBookUseCase.RegisterBookCommand): Book {
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
                id = BookId.new(),
                status = command.status,
            )
        return bookRepository.save(book)
    }

    override fun update(command: RegisterBookUseCase.UpdateBookCommand): Book {
        val requiredAuthorIds = command.authorIds.toSet()
        val existingAuthorIds = authorRepository.findAllByIds(requiredAuthorIds).map { it.id }.toSet()
        val missingAuthorIds = requiredAuthorIds - existingAuthorIds
        if (missingAuthorIds.isNotEmpty()) {
            throw MissingAuthorException(missingAuthorIds)
        }

        val book = bookRepository.findById(command.bookId) ?: throw BookNotFoundException(command.bookId)

        book.updateTitle(command.title)
        book.updatePrice(command.price)
        book.replaceAuthors(requiredAuthorIds)

        if (book.status != command.status) {
            book.changeStatus(command.status)
        }

        return bookRepository.save(book)
    }
}
