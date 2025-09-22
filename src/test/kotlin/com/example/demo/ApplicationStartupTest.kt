package com.example.demo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment

@SpringBootTest(
    properties = [
        "spring.profiles.default=local",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
    ],
)
class ApplicationStartupTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var registerAuthorUseCase: com.example.demo.application.author.input.RegisterAuthorUseCase

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var registerBookUseCase: com.example.demo.application.book.input.RegisterBookUseCase

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var queryBooksUseCase: com.example.demo.application.book.input.QueryBooksUseCase
    @Autowired
    private lateinit var environment: Environment

    @Test
    fun `application loads with local profile as default`() {
        assertThat(environment.getProperty("spring.profiles.default"))
            .describedAs("spring.profiles.default")
            .isEqualTo("local")
    }
}
