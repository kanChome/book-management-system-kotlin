package com.example.demo

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@Testcontainers
class BmApplicationTests {

	companion object {
		private val postgresImage = DockerImageName.parse("postgres:16-alpine")

		@Container
		@JvmStatic
		val postgres = PostgreSQLContainer(postgresImage).apply {
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
	fun contextLoads() {
	}

}
