package com.example.demo.infrastructure.jooq

import com.example.demo.domain.author.Author
import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.author.port.AuthorRepository
import com.example.demo.domain.book.BookId
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
                .select(
                    DSL.field("id", UUID::class.java),
                    DSL.field("name", String::class.java),
                    DSL.field("birth_date", LocalDate::class.java),
                ).from(DSL.table("authors"))
                .where(DSL.field("id", UUID::class.java).eq(id.value))
                .fetchOne() ?: return null

        val bookIds =
            dsl
                .select(DSL.field("book_id", UUID::class.java))
                .from(DSL.table("book_authors"))
                .where(DSL.field("author_id", UUID::class.java).eq(id.value))
                .fetch(0, UUID::class.java)
                .map { BookId.from(it) }

        return Author.new(
            name = a.get("name", String::class.java),
            birthDate = a.get("birth_date", LocalDate::class.java),
            bookIds = bookIds,
            id = AuthorId.from(a.get("id", UUID::class.java)),
        )
    }

    override fun findAllByIds(ids: Collection<AuthorId>): List<Author> {
        if (ids.isEmpty()) return emptyList()

        val authorUuids = ids.map { it.value }

        val authors =
            dsl
                .select(
                    DSL.field("id", UUID::class.java),
                    DSL.field("name", String::class.java),
                    DSL.field("birth_date", LocalDate::class.java),
                ).from(DSL.table("authors"))
                .where(DSL.field("id", UUID::class.java).`in`(authorUuids))
                .fetch()

        // 関連する書籍IDをまとめて取得し、マップ化
        val booksByAuthor: Map<UUID, List<BookId>> =
            dsl
                .select(
                    DSL.field("author_id", UUID::class.java),
                    DSL.field("book_id", UUID::class.java),
                ).from(DSL.table("book_authors"))
                .where(DSL.field("author_id", UUID::class.java).`in`(authorUuids))
                .fetchGroups(
                    { it.get("author_id", UUID::class.java) },
                    { BookId.from(it.get("book_id", UUID::class.java)) },
                )

        return authors.map { rec ->
            val aid = rec.get("id", UUID::class.java)
            Author.new(
                name = rec.get("name", String::class.java),
                birthDate = rec.get("birth_date", LocalDate::class.java),
                bookIds = booksByAuthor[aid] ?: emptyList(),
                id = AuthorId.from(aid),
            )
        }
    }

    override fun save(author: Author): Author {
        val updated =
            dsl
                .update(DSL.table("authors"))
                .set(DSL.field("name", String::class.java), author.name)
                .set(DSL.field("birth_date", LocalDate::class.java), author.birthDate)
                .set(
                    DSL.field("updated_at", java.time.OffsetDateTime::class.java),
                    DSL.currentOffsetDateTime(),
                ).where(DSL.field("id", UUID::class.java).eq(author.id.value))
                .execute()

        if (updated == 0) {
            dsl
                .insertInto(DSL.table("authors"))
                .columns(
                    DSL.field("id"),
                    DSL.field("name"),
                    DSL.field("birth_date"),
                    DSL.field("created_at"),
                    DSL.field("updated_at"),
                ).values(
                    author.id.value,
                    author.name,
                    author.birthDate,
                    DSL.defaultValue(),
                    DSL.defaultValue(),
                ).execute()
        }

        return author
    }
}
