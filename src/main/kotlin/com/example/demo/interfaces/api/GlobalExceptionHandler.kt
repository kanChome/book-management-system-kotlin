package com.example.demo.interfaces.api

import com.example.demo.domain.author.exception.AuthorNotFoundException
import com.example.demo.domain.book.exception.BookNotFoundException
import com.example.demo.domain.book.exception.MissingAuthorException
import com.example.demo.domain.book.exception.MissingBookException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.OffsetDateTime

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    data class ApiError(
        val timestamp: OffsetDateTime = OffsetDateTime.now(),
        val status: Int,
        val error: String,
        val message: String?,
        val path: String,
        val fieldErrors: List<FieldErrorEntry> = emptyList(),
    )

    data class FieldErrorEntry(
        val field: String,
        val message: String?,
    )

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val fieldErrors =
            ex.bindingResult
                .allErrors
                .mapNotNull { err ->
                    val fe = err as? FieldError
                    FieldErrorEntry(fe?.field ?: err.objectName, err.defaultMessage)
                }
        val body =
            ApiError(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = "Validation failed",
                path = request.getDescription(false).removePrefix("uri="),
                fieldErrors = fieldErrors,
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiError(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = ex.mostSpecificCause?.message ?: ex.message,
                    path = request.getDescription(false).removePrefix("uri="),
                ),
            )

    @ExceptionHandler(
        value = [
            AuthorNotFoundException::class,
            BookNotFoundException::class,
        ],
    )
    fun handleNotFound(
        ex: RuntimeException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ApiError(
                    status = HttpStatus.NOT_FOUND.value(),
                    error = HttpStatus.NOT_FOUND.reasonPhrase,
                    message = ex.message,
                    path = request.requestURI,
                ),
            )

    @ExceptionHandler(
        value = [
            MissingAuthorException::class,
            MissingBookException::class,
            IllegalArgumentException::class,
            MethodArgumentTypeMismatchException::class,
        ],
    )
    fun handleBadRequest(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiError(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = ex.message,
                    path = request.requestURI,
                ),
            )

    @ExceptionHandler(IllegalStateException::class)
    fun handleConflict(
        ex: IllegalStateException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ApiError(
                    status = HttpStatus.CONFLICT.value(),
                    error = HttpStatus.CONFLICT.reasonPhrase,
                    message = ex.message,
                    path = request.requestURI,
                ),
            )

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> =
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiError(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                    message = "Unexpected error",
                    path = request.requestURI,
                ),
            )
}
