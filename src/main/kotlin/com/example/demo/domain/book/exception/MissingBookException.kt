package com.example.demo.domain.book.exception

import com.example.demo.domain.book.BookId

class MissingBookException(
    missingBookIds: Collection<BookId>,
) : IllegalStateException(
        "指定された書籍が見つかりません: ${missingBookIds.joinToString(", ") { it.value.toString() }}",
    )
