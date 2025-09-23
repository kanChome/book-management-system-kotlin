package com.example.demo.infrastructure

// Author ports are provided individually; no aggregate AuthorRepository bean
import com.example.demo.application.author.service.RegisterAuthorService
import com.example.demo.application.book.port.out.LoadBookPort
import com.example.demo.application.book.port.out.QueryBooksPort
import com.example.demo.application.book.port.out.SaveBookPort
import com.example.demo.application.book.service.QueryBooksService
import com.example.demo.application.book.service.RegisterBookService
import com.example.demo.author.adapter.out.persistence.JooqAuthorPersistenceAdapter
import com.example.demo.book.adapter.out.persistence.JooqBookPersistenceAdapter
import com.example.demo.domain.author.service.AuthorRegistrationService
import com.example.demo.domain.book.service.BookQueryService
import com.example.demo.domain.book.service.BookRegistrationService
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
    fun jooqBookRepository(dsl: DSLContext): JooqBookPersistenceAdapter = JooqBookPersistenceAdapter(dsl)

    @Bean
    fun bookRepositoryPort(repo: JooqBookPersistenceAdapter): com.example.demo.application.book.port.out.BookRepository =
        object :
            com.example.demo.application.book.port.out.BookRepository,
            com.example.demo.application.book.port.out.LoadBookPort by repo,
            com.example.demo.application.book.port.out.SaveBookPort by repo,
            com.example.demo.application.book.port.out.QueryBooksPort by repo {}

    @Bean
    fun jooqAuthorPersistenceAdapter(dsl: DSLContext): JooqAuthorPersistenceAdapter = JooqAuthorPersistenceAdapter(dsl)

    @Bean
    fun saveAuthorPort(repo: JooqAuthorPersistenceAdapter): com.example.demo.application.author.port.out.SaveAuthorPort =
        object : com.example.demo.application.author.port.out.SaveAuthorPort {
            override fun save(author: com.example.demo.domain.author.Author) = repo.save(author)
        }

    @Bean
    fun loadAuthorPort(repo: JooqAuthorPersistenceAdapter): com.example.demo.application.author.port.out.LoadAuthorPort =
        object : com.example.demo.application.author.port.out.LoadAuthorPort {
            override fun findById(id: com.example.demo.domain.author.AuthorId) = repo.findById(id)
        }

    @Bean
    fun loadAuthorsPort(repo: JooqAuthorPersistenceAdapter): com.example.demo.application.author.port.out.LoadAuthorsPort =
        object : com.example.demo.application.author.port.out.LoadAuthorsPort {
            override fun findAllByIds(ids: Collection<com.example.demo.domain.author.AuthorId>) = repo.findAllByIds(ids)
        }

    @Bean
    fun saveBookPort(repo: JooqBookPersistenceAdapter): SaveBookPort =
        object : SaveBookPort {
            override fun save(book: com.example.demo.domain.book.Book) = repo.save(book)
        }

    @Bean
    fun loadBookPort(repo: JooqBookPersistenceAdapter): LoadBookPort =
        object : LoadBookPort {
            override fun findById(id: com.example.demo.domain.book.BookId) = repo.findById(id)
        }

    @Bean
    fun queryBooksPort(repo: JooqBookPersistenceAdapter): QueryBooksPort =
        object : QueryBooksPort {
            override fun findByAuthorId(authorId: com.example.demo.domain.author.AuthorId) = repo.findByAuthorId(authorId)
        }

    @Bean
    fun bookRegistrationService(
        saveBookPort: SaveBookPort,
        loadBookPort: LoadBookPort,
        loadAuthorsPort: com.example.demo.application.author.port.out.LoadAuthorsPort,
    ) = BookRegistrationService(saveBookPort, loadBookPort, loadAuthorsPort)

    // RegisterBookService は @Service で登録される

    @Bean
    fun authorRegistrationService(
        saveAuthorPort: com.example.demo.application.author.port.out.SaveAuthorPort,
        loadAuthorPort: com.example.demo.application.author.port.out.LoadAuthorPort,
        saveBookPort: SaveBookPort,
        loadBookPort: LoadBookPort,
    ) = AuthorRegistrationService(saveAuthorPort, loadAuthorPort, saveBookPort, loadBookPort)

    // RegisterAuthorService は @Service で登録される

    @Bean
    fun bookQueryService(queryBooksPort: QueryBooksPort) = BookQueryService(queryBooksPort)

    // QueryBooksService は @Service で登録される
}
