package com.example.demo.application.author.port.out

import com.example.demo.domain.author.Author

interface SaveAuthorPort {
    fun save(author: Author): Author
}
