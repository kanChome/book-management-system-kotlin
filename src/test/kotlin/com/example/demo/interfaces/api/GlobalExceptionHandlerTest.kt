package com.example.demo.interfaces.api

import com.example.demo.application.author.input.RegisterAuthorUseCase
import com.example.demo.domain.book.exception.MissingBookException
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(AuthorController::class)
class GlobalExceptionHandlerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @MockBean
        private lateinit var registerAuthor: RegisterAuthorUseCase

        @Test
        fun `MissingBookException は400を返す`() {
            Mockito.doThrow(MissingBookException(listOf())).`when`(registerAuthor).register(any())
            val body =
                """
                {
                  "name": "x",
                  "birthDate": "2000-01-01",
                  "bookIds": ["${UUID.randomUUID()}"]
                }
                """.trimIndent()

            mockMvc
                .perform(post("/api/authors").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
        }
    }
