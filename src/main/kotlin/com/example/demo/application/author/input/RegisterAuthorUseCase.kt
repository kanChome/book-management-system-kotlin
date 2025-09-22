package com.example.demo.application.author.input

import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.BookId
import java.time.Clock
import java.time.LocalDate

interface RegisterAuthorUseCase {
    fun register(command: RegisterAuthorCommand): Author

    data class RegisterAuthorCommand(
        val name: String,
        val birthDate: LocalDate,
        val bookIds: Collection<BookId> = emptyList(),
        val authorId: AuthorId? = null,
        val clock: Clock = Clock.systemDefaultZone(),
    )
}
