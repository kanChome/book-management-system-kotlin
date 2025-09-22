package com.example.demo.infrastructure.jooq

import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.author.port.AuthorRepository
import com.example.demo.domain.book.BookId
import com.example.demo.infrastructure.jooq.tables.references.AUTHORS
import com.example.demo.infrastructure.jooq.tables.references.BOOK_AUTHORS
import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.time.LocalDate
import java.util.UUID

/**
 * Infrastructure/JOOQ: 著者集約の永続化アダプタ。
 * - 何を: authors テーブルの読み書きと、book_authors から書籍ID集合を復元。
 * - なぜ: ドメインの AuthorRepository ポートを満たすため。
 * - メモ: 書籍との関連の書き込みは Book 側で管理（結合表更新は BookRepository に集約）。
 */
class JooqAuthorRepository(
    private val dsl: DSLContext,
) : AuthorRepository {
    override fun findById(id: AuthorId): Author? {
        val a =
            dsl
                .select(AUTHORS.ID, AUTHORS.NAME, AUTHORS.BIRTH_DATE)
                .from(AUTHORS)
                .where(AUTHORS.ID.eq(id.value))
                .fetchOne() ?: return null

        val bookIds =
            dsl
                .select(BOOK_AUTHORS.BOOK_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.AUTHOR_ID.eq(id.value))
                .fetch(BOOK_AUTHORS.BOOK_ID)
                .filterNotNull()
                .map { BookId.from(it) }

        return Author.new(
            name = a.get(AUTHORS.NAME)!!,
            birthDate = a.get(AUTHORS.BIRTH_DATE)!!,
            bookIds = bookIds,
            id = AuthorId.from(a.get(AUTHORS.ID)!!),
        )
    }

    override fun findAllByIds(ids: Collection<AuthorId>): List<Author> {
        if (ids.isEmpty()) return emptyList()

        val authorUuids = ids.map { it.value }

        val authors =
            dsl
                .select(AUTHORS.ID, AUTHORS.NAME, AUTHORS.BIRTH_DATE)
                .from(AUTHORS)
                .where(AUTHORS.ID.`in`(authorUuids))
                .fetch()

        // 関連する書籍IDをまとめて取得し、マップ化
        val booksByAuthor: Map<UUID, List<BookId>> =
            dsl
                .select(BOOK_AUTHORS.AUTHOR_ID, BOOK_AUTHORS.BOOK_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.AUTHOR_ID.`in`(authorUuids))
                .fetchGroups(
                    { it.get(BOOK_AUTHORS.AUTHOR_ID)!! },
                    { BookId.from(it.get(BOOK_AUTHORS.BOOK_ID)!!) },
                )

        return authors.map { rec ->
            val aid = rec.get(AUTHORS.ID)!!
            Author.new(
                name = rec.get(AUTHORS.NAME)!!,
                birthDate = rec.get(AUTHORS.BIRTH_DATE)!!,
                bookIds = booksByAuthor[aid] ?: emptyList(),
                id = AuthorId.from(aid),
            )
        }
    }

    override fun save(author: Author): Author {
        val updated =
            dsl
                .update(AUTHORS)
                .set(AUTHORS.NAME, author.name)
                .set(AUTHORS.BIRTH_DATE, author.birthDate)
                .set(AUTHORS.UPDATED_AT, DSL.currentOffsetDateTime())
                .where(AUTHORS.ID.eq(author.id.value))
                .execute()

        if (updated == 0) {
            dsl
                .insertInto(AUTHORS)
                .columns(AUTHORS.ID, AUTHORS.NAME, AUTHORS.BIRTH_DATE)
                .values(author.id.value, author.name, author.birthDate)
                .execute()
        }

        return author
    }
}
