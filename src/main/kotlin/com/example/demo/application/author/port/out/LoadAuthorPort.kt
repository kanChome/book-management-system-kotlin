package com.example.demo.application.author.port.out

import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId

interface LoadAuthorPort {
    fun findById(id: AuthorId): Author?
}
