package com.example.demo.application.book.port.out

/**
 * 互換用の包括ポート。段階的移行のため残置。
 * 今後は LoadBookPort / SaveBookPort / QueryBooksPort を直接利用してください。
 */
@Deprecated("Use LoadBookPort / SaveBookPort / QueryBooksPort instead")
interface BookRepository :
    LoadBookPort,
    SaveBookPort,
    QueryBooksPort
