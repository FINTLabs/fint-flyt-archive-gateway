package no.novari.flyt.archive.gateway.resource.sak

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Without a handler, errors are forwarded to `/error`, where the security filter chain from
 * flyt-web-resource-server answers 401. `ResponseStatusException` is handled by the base class.
 */
@RestControllerAdvice(assignableTypes = [InternalClientCaseController::class])
class InternalClientCaseExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = KotlinLogging.logger {}

    @ExceptionHandler(InvalidArchiveInstanceIdException::class)
    fun handleInvalidArchiveInstanceId(exception: InvalidArchiveInstanceIdException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.message)

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(exception: Exception): ProblemDetail {
        log.error(exception) { "Unexpected error in internal client sak API" }
        return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
