package no.fintlabs.flyt.gateway.application.archive.dispatch.web;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.fintlabs.flyt.gateway.application.archive.WebUtilErrorHandler;
import no.fintlabs.flyt.gateway.application.archive.resource.web.FintArchiveResourceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
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
                4,
                100L,
                250L,
                fintWebClient,
                fintArchiveResourceClient,
                meterRegistry,
                webUtilErrorHandler
        );
    }

    @Test
    public void givenCreatedLocationOnFirstPoll_shouldReturnWithoutRepeats() {
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        when(fintWebClient.get()).thenReturn(requestHeadersUriSpec);

        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(RequestHeadersSpec.class);
        when(requestHeadersUriSpec.uri(URI.create("testStatusUri"))).thenReturn(requestHeadersSpec);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.toBodilessEntity()).thenAnswer(invocation -> Mono.just(ResponseEntity.created(URI.create("testCreatedUri")).build()));

        StepVerifier.create(fintArchiveDispatchClient.pollForCreatedLocation(URI.create("testStatusUri")))
                .expectNext(URI.create("testCreatedUri"))
                .expectComplete()
                .verify();

        verify(fintWebClient, times(1)).get();
    }

    @Test
    public void givenRequiredAttemptsBelowMaxRepeats_shouldPollForStatusLocationUntilProvided() {
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        when(fintWebClient.get()).thenReturn(requestHeadersUriSpec);

        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(RequestHeadersSpec.class);
        when(requestHeadersUriSpec.uri(URI.create("testStatusUri"))).thenReturn(requestHeadersSpec);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        AtomicInteger attempts = new AtomicInteger();
        when(responseSpec.toBodilessEntity()).thenAnswer(invocation -> {
            if (attempts.incrementAndGet() < 3) {
                return Mono.just(ResponseEntity.noContent().build());
            }
            return Mono.just(ResponseEntity.created(URI.create("testCreatedUri")).build());
        });

        StepVerifier.create(fintArchiveDispatchClient.pollForCreatedLocation(URI.create("testStatusUri")))
                .expectNext(URI.create("testCreatedUri"))
                .expectComplete()
                .verify();

        verify(fintWebClient, times(3)).get();
    }

    @Test
    public void givenMaxPollingAttempts_shouldThrowException() {
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        when(fintWebClient.get()).thenReturn(requestHeadersUriSpec);

        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(RequestHeadersSpec.class);
        when(requestHeadersUriSpec.uri(URI.create("testStatusUri"))).thenReturn(requestHeadersSpec);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.toBodilessEntity()).thenAnswer(invocation -> Mono.just(ResponseEntity.noContent().build()));

        StepVerifier.create(fintArchiveDispatchClient.pollForCreatedLocation(URI.create("testStatusUri")))
                .expectErrorMessage("Reached max number of retries for polling created location from destination")
                .verify();

        verify(fintWebClient, times(4)).get();
    }

}
