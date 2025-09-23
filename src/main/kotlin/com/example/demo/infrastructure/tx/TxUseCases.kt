package com.example.demo.infrastructure.tx

import com.example.demo.application.author.input.RegisterAuthorUseCase
import com.example.demo.application.author.port.out.AuthorRepository
import com.example.demo.application.book.input.QueryBooksUseCase
import com.example.demo.application.book.input.RegisterBookUseCase
import com.example.demo.application.book.port.out.BookRepository
import com.example.demo.application.book.port.out.LoadBookPort
import com.example.demo.application.book.port.out.QueryBooksPort
import com.example.demo.application.book.port.out.SaveBookPort
import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.author.service.AuthorRegistrationService
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.service.BookQueryService
import com.example.demo.domain.book.service.BookRegistrationService
import org.springframework.transaction.annotation.Transactional

/**
 * UseCaseのトランザクション境界をインフラ層で提供する薄いラッパ。
 * - ドメイン層へSpring依存を持ち込まないため、ここで@Transactionを付与し委譲する。
 */
open class TxRegisterBookUseCase(
    private val saveBookPort: SaveBookPort,
    private val loadBookPort: LoadBookPort,
    private val authorRepository: AuthorRepository,
) : RegisterBookUseCase {
    private val delegate = BookRegistrationService(saveBookPort, loadBookPort, authorRepository)

    @Transactional
    open override fun register(command: RegisterBookUseCase.RegisterBookCommand): Book = delegate.register(command)

    @Transactional
    open override fun update(command: RegisterBookUseCase.UpdateBookCommand): Book = delegate.update(command)
}

open class TxRegisterAuthorUseCase(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository,
) : RegisterAuthorUseCase {
    private val delegate = AuthorRegistrationService(authorRepository, bookRepository)

    @Transactional
    open override fun register(command: RegisterAuthorUseCase.RegisterAuthorCommand): Author = delegate.register(command)

    @Transactional
    open override fun update(command: RegisterAuthorUseCase.UpdateAuthorCommand): Author = delegate.update(command)
}

open class TxQueryBooksUseCase(
    private val queryBooksPort: QueryBooksPort,
) : QueryBooksUseCase {
    private val delegate = BookQueryService(queryBooksPort)

    @Transactional(readOnly = true)
    open override fun findByAuthor(authorId: AuthorId) = delegate.findByAuthor(authorId)
}
