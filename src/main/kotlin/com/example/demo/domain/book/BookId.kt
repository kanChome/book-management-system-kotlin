package com.example.demo.domain.book

import java.util.UUID

data class BookId(
    val value: UUID,
) {
    companion object {
        fun new(): BookId = BookId(UUID.randomUUID())

        fun from(value: UUID): BookId = BookId(value)
    }
}
