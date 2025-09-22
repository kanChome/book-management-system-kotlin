package com.example.demo.interfaces.api

import com.example.demo.application.book.input.QueryBooksUseCase
import com.example.demo.application.book.input.RegisterBookUseCase
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.BookId
import com.example.demo.domain.book.BookStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/books")
class BookController(
    private val registerBook: RegisterBookUseCase,
    private val queryBooks: QueryBooksUseCase,
) {
    data class RegisterBookRequest(
        @field:NotBlank val title: String,
        @field:NotNull @field:PositiveOrZero val price: BigDecimal,
        @field:NotEmpty val authorIds: List<UUID>,
        val status: BookStatus? = null,
    )

    data class UpdateBookRequest(
        @field:NotBlank val title: String,
        @field:NotNull @field:PositiveOrZero val price: BigDecimal,
        @field:NotEmpty val authorIds: List<UUID>,
        @field:NotNull val status: BookStatus,
    )

    data class BookResponse(
        val id: UUID,
        val title: String,
        val price: BigDecimal,
        val authorIds: List<UUID>,
        val status: BookStatus,
    ) {
        companion object {
            fun from(book: Book) =
                BookResponse(
                    id = book.id.value,
                    title = book.title,
                    price = book.price,
                    authorIds = book.authorIds.map { it.value },
                    status = book.status,
                )
        }
    }

    @PostMapping
    fun register(@Valid @RequestBody req: RegisterBookRequest): ResponseEntity<BookResponse> {
        val cmd =
            RegisterBookUseCase.RegisterBookCommand(
                title = req.title,
                price = req.price,
                authorIds = req.authorIds.map { AuthorId.from(it) },
                status = req.status ?: BookStatus.UNPUBLISHED,
            )
        val saved = registerBook.register(cmd)
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.from(saved))
    }

    @PutMapping("/{id}")
    fun update(
        @org.springframework.web.bind.annotation.PathVariable id: UUID,
        @Valid @RequestBody req: UpdateBookRequest,
    ): ResponseEntity<BookResponse> {
        val cmd =
            RegisterBookUseCase.UpdateBookCommand(
                bookId = BookId.from(id),
                title = req.title,
                price = req.price,
                authorIds = req.authorIds.map { AuthorId.from(it) },
                status = req.status,
            )
        val updated = registerBook.update(cmd)
        return ResponseEntity.ok(BookResponse.from(updated))
    }

    @GetMapping
    fun findByAuthor(@RequestParam authorId: UUID): List<BookResponse> =
        queryBooks.findByAuthor(AuthorId.from(authorId)).map { BookResponse.from(it) }
}
