package com.example.demo.application.author.port.out

import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId

interface LoadAuthorsPort {
    fun findAllByIds(ids: Collection<AuthorId>): List<Author>
}
