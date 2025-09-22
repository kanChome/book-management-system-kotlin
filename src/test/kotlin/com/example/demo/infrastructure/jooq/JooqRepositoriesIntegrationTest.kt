package com.example.demo.infrastructure.jooq

import com.example.demo.application.author.input.RegisterAuthorUseCase
import com.example.demo.application.book.input.QueryBooksUseCase
import com.example.demo.application.book.input.RegisterBookUseCase
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.BookStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@Testcontainers
class JooqRepositoriesIntegrationTest
    @Autowired
    constructor(
        private val registerBook: RegisterBookUseCase,
        private val registerAuthor: RegisterAuthorUseCase,
        private val queryBooks: QueryBooksUseCase,
    ) {
        companion object {
            private val postgresImage = DockerImageName.parse("postgres:16-alpine")

            @Container
            @JvmStatic
            val postgres =
                PostgreSQLContainer(postgresImage).apply {
                    withDatabaseName("book_management")
                    withUsername("postgres")
                    withPassword("password")
                }

            @JvmStatic
            @DynamicPropertySource
            fun overrideDataSourceProps(registry: DynamicPropertyRegistry) {
                registry.add("spring.datasource.url", postgres::getJdbcUrl)
                registry.add("spring.datasource.username", postgres::getUsername)
                registry.add("spring.datasource.password", postgres::getPassword)
            }
        }

        @Test
        fun `書籍の登録と著者紐付け・検索が実DBで成功する`() {
            // 著者を2名登録（書籍は紐付けない）
            val authorIdA = AuthorId.from(UUID.randomUUID())
            val authorIdB = AuthorId.from(UUID.randomUUID())
            registerAuthor.register(
                RegisterAuthorUseCase.RegisterAuthorCommand(
                    name = "山田太郎",
                    birthDate = LocalDate.now().minusYears(40),
                    authorId = authorIdA,
                ),
            )
            registerAuthor.register(
                RegisterAuthorUseCase.RegisterAuthorCommand(
                    name = "佐藤花子",
                    birthDate = LocalDate.now().minusYears(35),
                    authorId = authorIdB,
                ),
            )

            // 書籍登録（著者Aのみ）
            val book =
                registerBook.register(
                    RegisterBookUseCase.RegisterBookCommand(
                        title = "Kotlin実践",
                        price = BigDecimal.valueOf(2800),
                        authorIds = listOf(authorIdA),
                        status = BookStatus.UNPUBLISHED,
                    ),
                )
            assertThat(book.authorIds).containsExactly(authorIdA)

            // 著者Aで検索
            val booksByA = queryBooks.findByAuthor(authorIdA)
            assertThat(booksByA.map { it.title }).contains("Kotlin実践")

            // 書籍を著者Bへ差し替えて更新＋公開
            val updated =
                registerBook.update(
                    RegisterBookUseCase.UpdateBookCommand(
                        bookId = book.id,
                        title = "Kotlin実践 改訂版",
                        price = BigDecimal.valueOf(3000),
                        authorIds = listOf(authorIdB),
                        status = BookStatus.PUBLISHED,
                    ),
                )
            assertThat(updated.title).isEqualTo("Kotlin実践 改訂版")
            assertThat(updated.authorIds).containsExactly(authorIdB)
            assertThat(updated.status).isEqualTo(BookStatus.PUBLISHED)

            // 検索面の整合確認
            val booksByB = queryBooks.findByAuthor(authorIdB)
            assertThat(booksByB.map { it.title }).contains("Kotlin実践 改訂版")
            val booksByA2 = queryBooks.findByAuthor(authorIdA)
            assertThat(booksByA2.map { it.title }).doesNotContain("Kotlin実践", "Kotlin実践 改訂版")
        }
    }
