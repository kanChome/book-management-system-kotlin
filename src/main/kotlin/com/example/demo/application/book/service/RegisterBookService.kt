package com.example.demo.application.book.service

import com.example.demo.application.book.input.RegisterBookUseCase
import com.example.demo.domain.book.service.BookRegistrationService
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Primary
class RegisterBookService(
    private val delegate: BookRegistrationService,
) : RegisterBookUseCase {
    @Transactional
    override fun register(command: RegisterBookUseCase.RegisterBookCommand) = delegate.register(command)

    @Transactional
    override fun update(command: RegisterBookUseCase.UpdateBookCommand) = delegate.update(command)
}
