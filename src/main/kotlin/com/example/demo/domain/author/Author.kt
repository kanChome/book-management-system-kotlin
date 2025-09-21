package com.example.demo.domain.author

import com.example.demo.domain.book.BookId
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

class Author private constructor(
    val id: AuthorId,
    name: String,
    val birthDate: LocalDate,
    bookIds: Collection<BookId>,
    private val clock: Clock,
) {
    var name: String = name
        private set

    private val _bookIds: MutableSet<BookId> = LinkedHashSet(bookIds)
    val bookIds: Set<BookId>
        get() = _bookIds.toSet()

    init {
        validateName(name)
        validateBirthDate(birthDate, clock)
    }

    fun rename(newName: String) {
        validateName(newName)
        name = newName.trim()
    }

    fun addBook(bookId: BookId) {
        _bookIds.add(bookId)
    }

    fun removeBook(bookId: BookId) {
        _bookIds.remove(bookId)
    }

    companion object {
        fun new(
            name: String,
            birthDate: LocalDate,
            bookIds: Collection<BookId> = emptyList(),
            id: AuthorId = AuthorId.from(UUID.randomUUID()),
            clock: Clock = Clock.systemDefaultZone(),
        ): Author {
            validateName(name)
            validateBirthDate(birthDate, clock)
            return Author(
                id = id,
                name = name,
                birthDate = birthDate,
                bookIds = bookIds,
                clock = clock,
            )
        }

        private fun validateName(name: String) {
            require(name.isNotBlank()) { "著者名は必須です。" }
        }

        private fun validateBirthDate(birthDate: LocalDate, clock: Clock) {
            require(birthDate.isBefore(LocalDate.now(clock))) { "生年月日は現在より過去の日付である必要があります。" }
        }
    }
}
