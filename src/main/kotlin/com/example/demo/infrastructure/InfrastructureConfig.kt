package com.example.demo.infrastructure

import com.example.demo.application.author.input.RegisterAuthorUseCase
import com.example.demo.application.author.port.out.AuthorRepository
import com.example.demo.application.book.input.QueryBooksUseCase
import com.example.demo.application.book.input.RegisterBookUseCase
import com.example.demo.application.book.port.out.LoadBookPort
import com.example.demo.application.book.port.out.QueryBooksPort
import com.example.demo.application.book.port.out.SaveBookPort
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
    fun jooqBookRepository(dsl: DSLContext): JooqBookRepository = JooqBookRepository(dsl)

    @Bean
    fun bookRepositoryPort(repo: JooqBookRepository): com.example.demo.application.book.port.out.BookRepository =
        object :
            com.example.demo.application.book.port.out.BookRepository,
            com.example.demo.application.book.port.out.LoadBookPort by repo,
            com.example.demo.application.book.port.out.SaveBookPort by repo,
            com.example.demo.application.book.port.out.QueryBooksPort by repo {}

    @Bean
    fun authorRepository(dsl: DSLContext): AuthorRepository = JooqAuthorRepository(dsl)

    @Bean
    fun saveBookPort(repo: JooqBookRepository): SaveBookPort =
        object : SaveBookPort {
            override fun save(book: com.example.demo.domain.book.Book) = repo.save(book)
        }

    @Bean
    fun loadBookPort(repo: JooqBookRepository): LoadBookPort =
        object : LoadBookPort {
            override fun findById(id: com.example.demo.domain.book.BookId) = repo.findById(id)
        }

    @Bean
    fun queryBooksPort(repo: JooqBookRepository): QueryBooksPort =
        object : QueryBooksPort {
            override fun findByAuthorId(authorId: com.example.demo.domain.author.AuthorId) = repo.findByAuthorId(authorId)
        }

    @Bean
    fun registerBookUseCase(
        saveBookPort: SaveBookPort,
        loadBookPort: LoadBookPort,
        authorRepository: AuthorRepository,
    ): RegisterBookUseCase = TxRegisterBookUseCase(saveBookPort, loadBookPort, authorRepository)

    @Bean
    fun registerAuthorUseCase(
        authorRepository: AuthorRepository,
        bookRepository: com.example.demo.application.book.port.out.BookRepository,
    ): RegisterAuthorUseCase = TxRegisterAuthorUseCase(authorRepository, bookRepository)

    @Bean
    fun queryBooksUseCase(queryBooksPort: QueryBooksPort): QueryBooksUseCase = TxQueryBooksUseCase(queryBooksPort)
}
