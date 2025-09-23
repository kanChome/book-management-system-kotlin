package com.example.demo.application.author.port.out

import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId

/**
 * 著者集約の永続化ポート（Outgoing Port）。
 * インフラ層が実装し、ユースケースから注入される。
 */
interface AuthorRepository {
    fun findById(id: AuthorId): Author?

    fun findAllByIds(ids: Collection<AuthorId>): List<Author>

    fun save(author: Author): Author
}
