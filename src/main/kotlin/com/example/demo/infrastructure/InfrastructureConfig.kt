package com.example.demo.infrastructure

import com.example.demo.application.author.input.RegisterAuthorUseCase
import com.example.demo.application.author.port.out.AuthorRepository
import com.example.demo.application.book.input.QueryBooksUseCase
import com.example.demo.application.book.input.RegisterBookUseCase
import com.example.demo.application.book.port.out.BookRepository
import com.example.demo.infrastructure.jooq.JooqAuthorRepository
import com.example.demo.infrastructure.jooq.JooqBookRepository
import com.example.demo.infrastructure.tx.TxQueryBooksUseCase
import com.example.demo.infrastructure.tx.TxRegisterAuthorUseCase
import com.example.demo.infrastructure.tx.TxRegisterBookUseCase
import org.jooq.DSLContext
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * Infrastructure層のBean組み立て。
 * - 何を: jOOQリポジトリとTx付ユースケースのBean定義。
 * - いつ: DSLContext（= DataSource/jOOQ）が利用可能なときのみ有効。
 */
@Configuration
@EnableTransactionManagement
@ConditionalOnProperty("spring.datasource.url")
class InfrastructureConfig {
    @Bean
    fun bookRepository(dsl: DSLContext): BookRepository = JooqBookRepository(dsl)

    @Bean
    fun authorRepository(dsl: DSLContext): AuthorRepository = JooqAuthorRepository(dsl)

    @Bean
    fun registerBookUseCase(
        bookRepository: BookRepository,
        authorRepository: AuthorRepository,
    ): RegisterBookUseCase = TxRegisterBookUseCase(bookRepository, authorRepository)

    @Bean
    fun registerAuthorUseCase(
        authorRepository: AuthorRepository,
        bookRepository: BookRepository,
    ): RegisterAuthorUseCase = TxRegisterAuthorUseCase(authorRepository, bookRepository)

    @Bean
    fun queryBooksUseCase(bookRepository: BookRepository): QueryBooksUseCase = TxQueryBooksUseCase(bookRepository)
}
