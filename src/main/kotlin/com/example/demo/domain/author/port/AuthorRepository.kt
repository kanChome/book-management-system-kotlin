package com.example.demo.domain.author.port

import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId

/**
 * 著者集約の永続化ポート。インフラ層が実装し、ユースケース層へ注入される。
 */
interface AuthorRepository {
    fun findById(id: AuthorId): Author?

    fun findAllByIds(ids: Collection<AuthorId>): List<Author>

    fun save(author: Author): Author
}
