package com.example.demo.interfaces.api

import com.example.demo.application.book.input.QueryBooksUseCase
import com.example.demo.application.book.input.RegisterBookUseCase
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.BookId
import com.example.demo.domain.book.BookStatus
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.UUID

@WebMvcTest(BookController::class)
class BookControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @MockBean
        private lateinit var registerBook: RegisterBookUseCase

        @MockBean
        private lateinit var queryBooks: QueryBooksUseCase

        @Test
        fun `POST 書籍登録 正常系`() {
            val authorId = AuthorId.from(UUID.randomUUID())
            val saved =
                Book.new(
                    title = "ドメイン駆動設計",
                    price = BigDecimal.valueOf(3000),
                    authorIds = listOf(authorId),
                    id = BookId.new(),
                    status = BookStatus.UNPUBLISHED,
                )
            Mockito.doReturn(saved).`when`(registerBook).register(any())

            val body =
                """
                {
                  "title": "ドメイン駆動設計",
                  "price": 3000,
                  "authorIds": ["${authorId.value}"]
                }
                """.trimIndent()

            mockMvc
                .perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.title", equalTo("ドメイン駆動設計")))
                .andExpect(jsonPath("$.authorIds[0]", equalTo(authorId.value.toString())))

            verify(registerBook).register(
                check { cmd ->
                    assert(cmd.title == "ドメイン駆動設計")
                    assert(cmd.authorIds.contains(authorId))
                },
            )
        }

        @Test
        fun `GET 著者IDで書籍検索 正常系`() {
            val authorId = AuthorId.from(UUID.randomUUID())
            val bookA = Book.new("A", BigDecimal.TEN, listOf(authorId))
            val bookB = Book.new("B", BigDecimal.ONE, listOf(authorId))
            Mockito.`when`(queryBooks.findByAuthor(any())).thenReturn(listOf(bookA, bookB))

            mockMvc
                .perform(get("/api/books").param("authorId", authorId.value.toString()))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Int>(2)))
                .andExpect(jsonPath("$[0].title", equalTo("A")))
                .andExpect(jsonPath("$[1].title", equalTo("B")))
        }
    }
