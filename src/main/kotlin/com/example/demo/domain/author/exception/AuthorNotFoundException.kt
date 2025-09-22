package com.example.demo.domain.author.exception

import com.example.demo.domain.author.AuthorId

class AuthorNotFoundException(
    authorId: AuthorId,
) : IllegalStateException("著者が見つかりません: ${authorId.value}")
