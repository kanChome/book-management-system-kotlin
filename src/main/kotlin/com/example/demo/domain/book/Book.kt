package com.example.demo.domain.book

import com.example.demo.domain.author.AuthorId
import java.math.BigDecimal

class Book private constructor(
    val id: BookId,
    title: String,
    price: BigDecimal,
    authorIds: Collection<AuthorId>,
    status: BookStatus,
) {
    var title: String = title
        private set

    var price: BigDecimal = price
        private set

    private val _authorIds: MutableSet<AuthorId> = LinkedHashSet(authorIds)
    val authorIds: Set<AuthorId>
        get() = _authorIds.toSet()

    var status: BookStatus = status
        private set

    init {
        validateTitle(title)
        validatePrice(price)
        validateAuthors(authorIds)
    }

    fun updateTitle(newTitle: String) {
        validateTitle(newTitle)
        title = newTitle.trim()
    }

    fun updatePrice(newPrice: BigDecimal) {
        validatePrice(newPrice)
        price = newPrice
    }

    fun replaceAuthors(authorIds: Collection<AuthorId>) {
        validateAuthors(authorIds)
        _authorIds.apply {
            clear()
            addAll(authorIds)
        }
    }

    fun addAuthor(authorId: AuthorId) {
        _authorIds.add(authorId)
    }

    fun removeAuthor(authorId: AuthorId) {
        if (!_authorIds.contains(authorId)) {
            return
        }
        if (_authorIds.size == 1) {
            throw IllegalStateException("著者を1人未満にすることはできません。")
        }
        _authorIds.remove(authorId)
    }

    fun changeStatus(newStatus: BookStatus) {
        if (status == BookStatus.PUBLISHED && newStatus == BookStatus.UNPUBLISHED) {
            throw IllegalStateException("出版済みの書籍を未出版に戻すことはできません。")
        }
        status = newStatus
    }

    companion object {
        fun new(
            title: String,
            price: BigDecimal,
            authorIds: Collection<AuthorId>,
            id: BookId = BookId.new(),
            status: BookStatus = BookStatus.UNPUBLISHED,
        ): Book =
            Book(
                id = id,
                title = title,
                price = price,
                authorIds = authorIds,
                status = status,
            )

        private fun validateTitle(title: String) {
            require(title.isNotBlank()) { "タイトルは必須です。" }
        }

        private fun validatePrice(price: BigDecimal) {
            require(price >= BigDecimal.ZERO) { "価格は0以上である必要があります。" }
        }

        private fun validateAuthors(authorIds: Collection<AuthorId>) {
            require(authorIds.isNotEmpty()) { "書籍には最低1人の著者が必要です。" }
        }
    }
}
