package com.example.demo.application.author.service

import com.example.demo.application.author.input.RegisterAuthorUseCase
import com.example.demo.domain.author.service.AuthorRegistrationService
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Primary
class RegisterAuthorService(
    private val delegate: AuthorRegistrationService,
) : RegisterAuthorUseCase {
    @Transactional
    override fun register(command: RegisterAuthorUseCase.RegisterAuthorCommand) = delegate.register(command)

    @Transactional
    override fun update(command: RegisterAuthorUseCase.UpdateAuthorCommand) = delegate.update(command)
}
