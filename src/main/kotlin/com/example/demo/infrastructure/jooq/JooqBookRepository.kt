package com.example.demo.infrastructure.jooq

import com.example.demo.domain.author.AuthorId
import com.example.demo.domain.book.Book
import com.example.demo.domain.book.BookId
import com.example.demo.domain.book.BookStatus
import com.example.demo.domain.book.port.BookRepository
import com.example.demo.infrastructure.jooq.tables.references.BOOKS
import com.example.demo.infrastructure.jooq.tables.references.BOOK_AUTHORS
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
                .select(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.STATUS)
                .from(BOOKS)
                .where(BOOKS.ID.eq(id.value))
                .fetchOne() ?: return null

        val authorIds =
            dsl
                .select(BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(id.value))
                .fetch(BOOK_AUTHORS.AUTHOR_ID)
                .filterNotNull()
                .map { AuthorId.from(it) }

        return Book.new(
            title = b.get(BOOKS.TITLE)!!,
            price = b.get(BOOKS.PRICE)!!,
            authorIds = authorIds,
            id = BookId.from(b.get(BOOKS.ID)!!),
            status = BookStatus.valueOf(b.get(BOOKS.STATUS)!!),
        )
    }

    override fun findByAuthorId(authorId: AuthorId): List<Book> {
        // 著者に紐づく書籍IDを取得 → 各書籍をfindByIdで再構築（まずは分かりやすさ優先）。
        val bookIds =
            dsl
                .select(BOOK_AUTHORS.BOOK_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId.value))
                .fetch(BOOK_AUTHORS.BOOK_ID)
                .filterNotNull()
                .distinct()
                .map { BookId.from(it) }

        return bookIds.mapNotNull { findById(it) }
    }

    override fun save(book: Book): Book {
        // 1) books を upsert（update→0件ならinsert）。
        val updated =
            dsl
                .update(BOOKS)
                .set(BOOKS.TITLE, book.title)
                .set(BOOKS.PRICE, book.price)
                .set(BOOKS.STATUS, book.status.name)
                .set(BOOKS.UPDATED_AT, DSL.currentOffsetDateTime())
                .where(BOOKS.ID.eq(book.id.value))
                .execute()

        if (updated == 0) {
            dsl
                .insertInto(BOOKS)
                .columns(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.STATUS)
                .values(book.id.value, book.title, book.price, book.status.name)
                .execute()
        }

        // 2) book_authors の差分反映。
        val currentAuthorIds =
            dsl
                .select(BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(book.id.value))
                .fetch(BOOK_AUTHORS.AUTHOR_ID)
                .filterNotNull()
                .toSet()

        val desiredAuthorIds = book.authorIds.map { it.value }.toSet()

        val toAdd = desiredAuthorIds - currentAuthorIds
        val toRemove = currentAuthorIds - desiredAuthorIds

        if (toRemove.isNotEmpty()) {
            dsl
                .deleteFrom(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(book.id.value))
                .and(BOOK_AUTHORS.AUTHOR_ID.`in`(toRemove))
                .execute()
        }
        toAdd.forEach { authorUuid ->
            dsl
                .insertInto(BOOK_AUTHORS)
                .columns(BOOK_AUTHORS.BOOK_ID, BOOK_AUTHORS.AUTHOR_ID)
                .values(book.id.value, authorUuid)
                .onConflict(BOOK_AUTHORS.BOOK_ID, BOOK_AUTHORS.AUTHOR_ID)
                .doNothing()
                .execute()
        }

        return book
    }
}
