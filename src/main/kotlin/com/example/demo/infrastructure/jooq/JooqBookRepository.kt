package com.example.demo.infrastructure.jooq

import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.BookId
import com.example.demo.domain.book.BookStatus
import com.example.demo.domain.book.port.BookRepository
import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.math.BigDecimal
import java.util.UUID

/**
 * Infrastructure/JOOQ: 書籍集約の永続化アダプタ。
 * - 何を: books / book_authors の読み書き。
 * - なぜ: ドメインの BookRepository ポートをPostgres実装で満たすため。
 * - 方針: 単純で明示的なSQLを優先（生成メタモデル未生成でもビルドできるようにDSLの文字列参照を使用）。
 *   将来的にjOOQ生成コード（Tables.*）へ置換可能。
 */
class JooqBookRepository(
    private val dsl: DSLContext,
) : BookRepository {
    override fun findById(id: BookId): Book? {
        val b =
            dsl
                .select(
                    DSL.field("id", UUID::class.java),
                    DSL.field("title", String::class.java),
                    DSL.field("price", BigDecimal::class.java),
                    DSL.field("status", String::class.java),
                ).from(DSL.table("books"))
                .where(DSL.field("id", UUID::class.java).eq(id.value))
                .fetchOne() ?: return null

        val authorIds =
            dsl
                .select(DSL.field("author_id", UUID::class.java))
                .from(DSL.table("book_authors"))
                .where(DSL.field("book_id", UUID::class.java).eq(id.value))
                .fetch(0, UUID::class.java)
                .map { AuthorId.from(it) }

        return Book.new(
            title = b.get("title", String::class.java),
            price = b.get("price", BigDecimal::class.java),
            authorIds = authorIds,
            id = BookId.from(b.get("id", UUID::class.java)),
            status = BookStatus.valueOf(b.get("status", String::class.java)),
        )
    }

    override fun findByAuthorId(authorId: AuthorId): List<Book> {
        // 著者に紐づく書籍IDを取得 → 各書籍をfindByIdで再構築（まずは分かりやすさ優先）。
        val bookIds =
            dsl
                .select(DSL.field("book_id", UUID::class.java))
                .from(DSL.table("book_authors"))
                .where(DSL.field("author_id", UUID::class.java).eq(authorId.value))
                .fetch(0, UUID::class.java)
                .distinct()
                .map { BookId.from(it) }

        return bookIds.mapNotNull { findById(it) }
    }

    override fun save(book: Book): Book {
        // 1) books を upsert（update→0件ならinsert）。
        val updated =
            dsl
                .update(DSL.table("books"))
                .set(DSL.field("title", String::class.java), book.title)
                .set(DSL.field("price", BigDecimal::class.java), book.price)
                .set(DSL.field("status", String::class.java), book.status.name)
                .set(
                    DSL.field("updated_at", java.time.OffsetDateTime::class.java),
                    DSL.currentOffsetDateTime(),
                ).where(DSL.field("id", UUID::class.java).eq(book.id.value))
                .execute()

        if (updated == 0) {
            dsl
                .insertInto(DSL.table("books"))
                .columns(
                    DSL.field("id"),
                    DSL.field("title"),
                    DSL.field("price"),
                    DSL.field("status"),
                    DSL.field("created_at"),
                    DSL.field("updated_at"),
                ).values(
                    book.id.value,
                    book.title,
                    book.price,
                    book.status.name,
                    DSL.defaultValue(), // DB側DEFAULT(NOW())
                    DSL.defaultValue(),
                ).execute()
        }

        // 2) book_authors の差分反映。
        val currentAuthorIds =
            dsl
                .select(DSL.field("author_id", UUID::class.java))
                .from(DSL.table("book_authors"))
                .where(DSL.field("book_id", UUID::class.java).eq(book.id.value))
                .fetch(0, UUID::class.java)
                .toSet()

        val desiredAuthorIds = book.authorIds.map { it.value }.toSet()

        val toAdd = desiredAuthorIds - currentAuthorIds
        val toRemove = currentAuthorIds - desiredAuthorIds

        if (toRemove.isNotEmpty()) {
            dsl
                .deleteFrom(DSL.table("book_authors"))
                .where(DSL.field("book_id", UUID::class.java).eq(book.id.value))
                .and(DSL.field("author_id", UUID::class.java).`in`(toRemove))
                .execute()
        }
        toAdd.forEach { authorUuid ->
            dsl
                .insertInto(DSL.table("book_authors"))
                .columns(DSL.field("book_id"), DSL.field("author_id"))
                .values(book.id.value, authorUuid)
                .onConflict(DSL.field("book_id"), DSL.field("author_id"))
                .doNothing()
                .execute()
        }

        return book
    }
}
