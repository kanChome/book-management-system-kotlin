package com.example.demo.interfaces.api

import com.example.demo.application.author.input.RegisterAuthorUseCase
import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.BookId
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/authors")
class AuthorController(
    private val registerAuthor: RegisterAuthorUseCase,
) {
    data class RegisterAuthorRequest(
        @field:NotBlank val name: String,
        @field:NotNull val birthDate: LocalDate,
        val bookIds: List<UUID> = emptyList(),
    )

    data class UpdateAuthorRequest(
        @field:NotBlank val name: String,
        @field:NotNull val birthDate: LocalDate,
        val bookIds: List<UUID> = emptyList(),
    )

    data class AuthorResponse(
        val id: UUID,
        val name: String,
        val birthDate: LocalDate,
        val bookIds: List<UUID>,
    ) {
        companion object {
            fun from(author: Author) =
                AuthorResponse(
                    id = author.id.value,
                    name = author.name,
                    birthDate = author.birthDate,
                    bookIds = author.bookIds.map { it.value },
                )
        }
    }

    @PostMapping
    fun register(
        @Valid @RequestBody req: RegisterAuthorRequest,
    ): ResponseEntity<AuthorResponse> {
        val cmd =
            RegisterAuthorUseCase.RegisterAuthorCommand(
                name = req.name,
                birthDate = req.birthDate,
                bookIds = req.bookIds.map { BookId.from(it) },
            )
        val saved = registerAuthor.register(cmd)
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthorResponse.from(saved))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody req: UpdateAuthorRequest,
    ): ResponseEntity<AuthorResponse> {
        val cmd =
            RegisterAuthorUseCase.UpdateAuthorCommand(
                authorId = AuthorId.from(id),
                name = req.name,
                birthDate = req.birthDate,
                bookIds = req.bookIds.map { BookId.from(it) },
            )
        val updated = registerAuthor.update(cmd)
        return ResponseEntity.ok(AuthorResponse.from(updated))
    }
}
