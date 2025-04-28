package no.fintlabs.flyt.gateway.application.archive.dispatch.web;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.fintlabs.flyt.gateway.application.archive.WebUtilErrorHandler;
import no.fintlabs.flyt.gateway.application.archive.resource.web.FintArchiveResourceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;

class FintArchiveDispatchClientTest {

    WebClient fintWebClient;
    FintArchiveResourceClient fintArchiveResourceClient;
    FintArchiveDispatchClient fintArchiveDispatchClient;
    MeterRegistry meterRegistry;
    WebUtilErrorHandler webUtilErrorHandler;

    @BeforeEach
    public void setup() {
        fintWebClient = mock(WebClient.class);
        fintArchiveResourceClient = mock(FintArchiveResourceClient.class);
        meterRegistry = new SimpleMeterRegistry();
        webUtilErrorHandler = mock(WebUtilErrorHandler.class);
        fintArchiveDispatchClient = new FintArchiveDispatchClient(
                FintArchiveDispatchClientConfigurationProperties
                        .builder()
                        .postFileTimeoutMillis(130000L)
                        .postRecordTimeoutMillis(130000L)
                        .postCaseTimeoutMillis(130000L)
                        .getStatusTimeoutMillis(130000L)
                        .createdLocationPollBackoffMinDelayMillis(100L)
                        .createdLocationPollBackoffMaxDelayMillis(250L)
                        .createdLocationPollTotalTimeoutMillis(1000L)
                        .build(),
                fintWebClient,
                fintArchiveResourceClient,
                meterRegistry,
                webUtilErrorHandler
        );
    }

    @Test
    public void givenCreatedLocationOnFirstPoll_whenPollForCreatedLocation_shouldReturnWithoutRepeats() {
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        when(fintWebClient.get()).thenReturn(requestHeadersUriSpec);

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestHeadersUriSpec.uri(URI.create("testStatusUri"))).thenReturn(requestBodySpec);

        when(requestBodySpec.httpRequest(any())).thenReturn(requestBodySpec);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.toBodilessEntity()).thenAnswer(invocation -> Mono.just(ResponseEntity.created(URI.create("testCreatedUri")).build()));

        StepVerifier.create(fintArchiveDispatchClient.pollForCreatedLocation(URI.create("testStatusUri")))
                .expectNext(URI.create("testCreatedUri"))
                .expectComplete()
                .verify();

        verify(fintWebClient, times(1)).get();
    }

    @Test
    public void givenRequiredRepeatsFasterThanMaxTotalTimeout_whenPollForCreatedLocation_shouldPollForStatusLocationUntilProvided() {
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        when(fintWebClient.get()).thenReturn(requestHeadersUriSpec);

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestHeadersUriSpec.uri(URI.create("testStatusUri"))).thenReturn(requestBodySpec);

        when(requestBodySpec.httpRequest(any())).thenReturn(requestBodySpec);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        AtomicLong startTime = new AtomicLong(0);
        when(responseSpec.toBodilessEntity()).thenAnswer(invocation -> {
            if (startTime.get() == 0) {
                startTime.set(System.nanoTime());
                return Mono.just(ResponseEntity.noContent().build());
            }
            if (Duration.ofNanos(System.nanoTime()).minus(Duration.ofNanos(startTime.get()))
                        .compareTo(Duration.ofMillis(500)) < 0) {
                return Mono.just(ResponseEntity.noContent().build());
            }
            return Mono.just(ResponseEntity.created(URI.create("testCreatedUri")).build());
        });

        StepVerifier.create(fintArchiveDispatchClient.pollForCreatedLocation(URI.create("testStatusUri")))
                .expectNext(URI.create("testCreatedUri"))
                .expectComplete()
                .verify();

        verify(fintWebClient, atLeast(2)).get();
    }

    @Test
    public void givenMaxPollingTimeIsExceeded_whenPollForCreatedLocation_shouldThrowException() {
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        when(fintWebClient.get()).thenReturn(requestHeadersUriSpec);

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestHeadersUriSpec.uri(URI.create("testStatusUri"))).thenReturn(requestBodySpec);

        when(requestBodySpec.httpRequest(any())).thenReturn(requestBodySpec);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.toBodilessEntity()).thenAnswer(invocation -> Mono.just(ResponseEntity.noContent().build()));

        StepVerifier.create(fintArchiveDispatchClient.pollForCreatedLocation(URI.create("testStatusUri")))
                .expectErrorMessage("Reached max total timeout for polling created location from destination")
                .verify();

        verify(fintWebClient, atLeast(2)).get();
    }

    @Test
    public void givenErrorResponse_whenPollForCreatedLocation_shouldThrowException() {
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        when(fintWebClient.get()).thenReturn(requestHeadersUriSpec);

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestHeadersUriSpec.uri(URI.create("testStatusUri"))).thenReturn(requestBodySpec);

        when(requestBodySpec.httpRequest(any())).thenReturn(requestBodySpec);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        WebClientException webClientException = mock(WebClientException.class);
        when(webClientException.getMessage()).thenReturn("test message");
        when(responseSpec.toBodilessEntity()).thenAnswer(invocation -> Mono.error(webClientException));

        StepVerifier.create(fintArchiveDispatchClient.pollForCreatedLocation(URI.create("testStatusUri")))
                .expectErrorMessage("test message")
                .verify();

        verify(fintWebClient, times(1)).get();
    }

}
