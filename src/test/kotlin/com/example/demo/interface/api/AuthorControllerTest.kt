package com.example.demo.interfaces.api

import com.example.demo.application.author.input.RegisterAuthorUseCase
import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.BookId
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(AuthorController::class)
class AuthorControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @MockBean
    private lateinit var registerAuthor: RegisterAuthorUseCase

    @Test
    fun `POST 著者登録 正常系`() {
        val authorId = AuthorId.from(UUID.randomUUID())
        val saved = Author.new("山田太郎", LocalDate.now().minusYears(20), emptyList(), id = authorId)
        Mockito.doReturn(saved).`when`(registerAuthor).register(any())

        val body =
            """
            {
              "name": "山田太郎",
              "birthDate": "${LocalDate.now().minusYears(20)}"
            }
            """.trimIndent()
        mockMvc.perform(post("/api/authors").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name", equalTo("山田太郎")))
    }

    @Test
    fun `PUT 著者更新 正常系`() {
        val authorId = AuthorId.from(UUID.randomUUID())
        val bookId = BookId.new()
        val updated = Author.new("更新後", LocalDate.now().minusYears(30), listOf(bookId), id = authorId)
        Mockito.doReturn(updated).`when`(registerAuthor).update(any())
        val body =
            """
            {
              "name": "更新後",
              "birthDate": "${LocalDate.now().minusYears(30)}",
              "bookIds": ["${bookId.value}"]
            }
            """.trimIndent()
        mockMvc.perform(put("/api/authors/${authorId.value}").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("更新後")))
    }
}
