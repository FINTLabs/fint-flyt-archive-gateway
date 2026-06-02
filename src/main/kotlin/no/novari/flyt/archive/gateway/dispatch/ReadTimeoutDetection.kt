package no.novari.flyt.archive.gateway.dispatch

import org.springframework.web.client.ResourceAccessException
import java.net.http.HttpTimeoutException

fun isReadTimeout(error: Throwable): Boolean =
    error is HttpTimeoutException ||
        (error is ResourceAccessException && error.cause is HttpTimeoutException)
