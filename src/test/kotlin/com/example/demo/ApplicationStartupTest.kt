package com.example.demo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment

@SpringBootTest(properties = [
    "spring.profiles.default=local",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
])
class ApplicationStartupTest {

    @Autowired
    private lateinit var environment: Environment

    @Test
    fun `application loads with local profile as default`() {
        assertThat(environment.getProperty("spring.profiles.default"))
            .describedAs("spring.profiles.default")
            .isEqualTo("local")
    }
}
