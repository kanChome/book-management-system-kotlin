package com.example.demo.domain.author

import java.util.UUID

data class AuthorId(
    val value: UUID,
) {
    companion object {
        fun from(value: UUID): AuthorId = AuthorId(value)
    }
}
