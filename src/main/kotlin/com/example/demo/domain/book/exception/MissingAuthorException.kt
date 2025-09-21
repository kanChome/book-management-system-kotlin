package com.example.demo.domain.book.exception

import com.example.demo.domain.author.AuthorId

class MissingAuthorException(
    missingAuthorIds: Collection<AuthorId>,
) : IllegalStateException(
        "指定された著者が見つかりません: ${missingAuthorIds.joinToString(", ") { it.value.toString() }}",
    )
