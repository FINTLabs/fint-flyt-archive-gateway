# FINT Flyt Archive Gateway

Spring Boot (WebFlux + Kafka) gateway that listens for mapped archive instances, dispatches cases and journal posts against FINT Arkiv adapters, exposes supporting archive metadata over HTTP, and republishes cacheable resources to downstream services.

## Highlights

- **Reactive dispatch pipeline** — `DispatchService` orchestrates validation and routing for new, by-id, and search-or-new archive instances, composing non-blocking case creation with subsequent record uploads.
- **Archive metadata APIs** — Internal controllers expose lightweight endpoints for case lookups and dozens of kodeverk collections backed by the shared FINT caches.
- **Instance-flow Kafka integration** — Dedicated consumer and producers handle `instance-mapped`, `instance-dispatched`, and dispatch-error events using the Flyt instance-flow templates.
- **Resource publishing scheduler** — `FintResourcePublishingConfiguration` periodically pulls updated archive resources, caches them, and pushes snapshots onto entity topics.
- **Operational guardrails** — OAuth2 resource server, configurable timeouts per downstream client, Prometheus/Actuator endpoints, and deterministic retry/backoff policies.

## Architecture Overview

| Component | Responsibility |
|-----------|----------------|
| `DispatchService` | Routes incoming `ArchiveInstance` payloads, coordinates case dispatch/search, and invokes `RecordsProcessingService` for journal posts and files. |
| `InstanceMappedEventConsumerConfiguration` | Builds the instance-flow listener container, wires the dispatch service, and emits success/decline/error events via the producer services. |
| `FintResourcePublishingConfiguration` | Schedules cache resets, polls FINT Archive resources through configured pipelines, writes to caches, and publishes to Kafka entity topics. |
| `CaseController`/`CaseRequestService` | Serves `/internal/api/arkiv/saker/{year}/{number}/tittel`, issuing Kafka request/reply lookups against the archive adapter. |
| `CodelistController` | Reads from the shared `FintCache` instances and renders multiple kodeverk collections and klassifikasjonssystem metadata as reference JSON. |
| `FintArchiveWebClientConfiguration` & `WebClientConfiguration` | Provide tuned WebClient beans (timeouts, connection pools) for archive adapters, Flyt file service, and resource pulls. |

## HTTP API

Base path: `/internal/api/arkiv`

| Method | Path | Description | Request body | Response |
|--------|------|-------------|--------------|----------|
| `GET` | `/saker/{caseYear}/{caseNumber}/tittel` | Fetches the case title for the given Noark `mappeId`. | – | `200 OK` with `{ "value": "<title>" }`; `404` if unknown. |
| `GET` | `/kodeverk/{type}` | Returns cached kodeverk entries (administrativEnhet, arkivdel, dokumentstatus, journalposttype, etc.). | – | `200 OK` array of `ResourceReference { "id": "<selfLink>", "displayName": "[code] Name #systemId" }`. |
| `GET` | `/kodeverk/klassifikasjonssystem` | Lists all klassifikasjonssystemer with their IDs and titles. | – | `200 OK` array. |
| `GET` | `/kodeverk/klasse?klassifikasjonssystemLink=<link>` | Lists classes that belong to a klassifikasjonssystem link. | – | `200 OK` array; `404` if the system is missing. |

Kodeverk endpoints share the same response contract; each list is distinct (administrativEnhet, arkivdel, arkivressurs, partrolle, korrespondanseparttype, tilknyttetregistreringsom, sakstatus, skjermingshjemmel, tilgangsrestriksjon, dokumentstatus, dokumenttype, journalstatus, journalposttype, saksmappetype, variantformat, format, tilgangsgruppe).

## Kafka Integration

- `InstanceMappedEventConsumerConfiguration.instanceMappedEventConsumer` listens to the `instance-mapped` event topic (partitioned by org/app defaults) and hands each record to `DispatchService`. Failed records are skipped with no retries via the shared `ErrorHandlerFactory`.
- `InstanceDispatchedEventProducerService` provisions the `instance-dispatched` topic (retention derived from `KafkaTopicProperties`) and emits completion events after accepted dispatches. Keys should uniquely represent the archive instance for better ordering.
- `InstanceDispatchingErrorProducerService` creates the `instance-dispatching-error` topic and publishes structured error collections for DECLINED or FAILED dispatches.
- `CaseRequestService` issues Kafka request/reply calls to `arkiv.noark.sak` to resolve case titles, provisioning a reply topic scoped to the service’s application ID.
- `FintResourcePublishingConfiguration` ensures entity topics exist for each pipeline and uses `ParameterizedTemplate` to push updated resources, caching them locally for HTTP lookups.

## Scheduled Tasks

The service relies on Spring Scheduling via `FintResourcePublishingConfiguration`:

- Cron reset: once per day at a randomized time between `reset.from-time-of-day` and `reset.to-time-of-day`, cached last-updated timestamps are cleared so subsequent pulls fetch full deltas.
- Fixed-delay pull: every `pull.fixed-delay` (default 15 minutes, after a 5-second initial delay) each `ResourcePipeline` polls FINT Archive for updates, populates caches, and republishes to Kafka.

No other cron-style jobs exist; the dispatch path is fully event-driven.

## Configuration

Spring profiles included by default: `fint-client`, `fint-oauth2-idp`, `flyt-file-client`, `flyt-kafka`, `flyt-logging`, `flyt-resource-server`. Key properties:

| Property | Description |
|----------|-------------|
| `fint.application-id` | Identifier reused for Kafka consumer groups and topic prefixes (default `fint-flyt-archive-gateway`). |
| `fint.org-id` | Current organization context; overrides live per overlay (local-staging defaults to `fintlabs.no`). |
| `novari.kafka.topic.instance-processing-events-retention-time` | Retention (hours) applied to instance-processing topics when provisioning. |
| `novari.flyt.archive.gateway.dispatch.fint-client.*` | Timeout and polling settings for posting cases, records, and files to the archive adapter. |
| `novari.flyt.archive.gateway.resource.publishing.*` | Window, offset, and pull cadence for resource caching and topic publishing. |
| `novari.flyt.archive.gateway.resource.fint-client.*` | Timeouts and retry strategy for resource lookups and case searches. |
| `novari.flyt.resource-server.security.api.internal.*` | Enables internal API protection plus `authorized-org-id-role-pairs-json` for org/role allow-lists. |
| `spring.kafka.bootstrap-servers` | Kafka bootstrap list (e.g., `localhost:9092` in `application-local-staging.yaml`). |
| `server.port` | HTTP port (8301 for local-staging). |
| `spring.security.oauth2.client.*` | Credentials for the fint archive adapter and Flyt file-service OAuth2 clients. |

## Running Locally

Prerequisites: Java 21+, Docker (for Kafka, if needed), and the Gradle wrapper.

1. Start Kafka/Schema Registry (e.g., `docker compose up kafka`). Ensure it listens on `localhost:9092` to match `application-local-staging.yaml`.
2. Export the local profile:
   ```shell
   export SPRING_PROFILES_ACTIVE=local-staging
   ```
3. Provide required secrets via environment variables or a `.env` file (`fint.core.oauth2.*`, `fint.sso.*`, etc.).
4. Build and run:
   ```shell
   ./gradlew clean build
   ./gradlew bootRun
   ```
5. Exercise the APIs:
   ```shell
   curl -H "Authorization: Bearer <token>" \
        "http://localhost:8301/internal/api/arkiv/saker/2024/123/tittel"
   curl -H "Authorization: Bearer <token>" \
        "http://localhost:8301/internal/api/arkiv/kodeverk/dokumenttype"
   ```

Running unit tests only:
```shell
./gradlew test
```

## Deployment

Manifests live under `kustomize/`:

- `kustomize/base/` — shared FLAIS Application manifest plus configmaps/secrets referenced across environments.
- `kustomize/templates/overlay.yaml.tpl` — source templates for per-org overlays.
- `kustomize/overlays/<org>/<env>/` — generated overlays that pin org IDs, ingress rules, Kafka parameters, and secret references.
- `script/render-overlay.sh` — helper to regenerate every `kustomize/overlays/**/kustomization.yaml` after changing templates.

After editing templates or base manifests run:
```shell
./script/render-overlay.sh
```
and commit both template and regenerated overlay files.

## Security

- OAuth2 resource server validates JWTs issued by `https://idp.felleskomponent.no` and enforces internal API org/role mappings via the Flyt resource server starter.
- Outbound calls use two OAuth2 clients: password-grant credentials for the FINT archive adapter and client-credentials for the Flyt file service.
- Kafka consumers/producers leverage org/application-scoped prefixes ensuring each tenant’s topics stay isolated.
- Internal APIs live under `/internal/api/**` and require authorization according to the configured org-role pairs.

## Observability & Operations

- Actuator health/info/prometheus endpoints are enabled (see `application-flyt-logging.yaml`); scrape `/actuator/prometheus` for metrics.
- Logging integrates with the shared Flyt logging profile and emits structured entries for dispatch outcomes and resource pulls.
- Kafka listener and resource publishing logs indicate topic provisioning, cache sizes, and error conditions for easier incident triage.

## Development Tips

- `RecordsProcessingService` streams journalpost records/files sequentially; keep payload sizes manageable and verify the file-service timeouts before tightening Kafka consumer settings.
- Resource caches are populated by the scheduler. If you call kodeverk endpoints immediately after boot, trigger the pull (or wait for `pull.initial-delay`) to avoid empty lists.
- Use the TODO markers in `InstanceDispatchedEventProducerService` and `InstanceDispatchingErrorProducerService` as a guide—supplying deterministic keys simplifies debugging and replay.

## Contributing

1. Create a topic branch off `main` (e.g., `feature/<ticket>`).
2. Implement changes plus unit/integration tests; run `./gradlew test` (and `./gradlew bootRun` locally if needed).
3. If kustomize manifests change, re-run `./script/render-overlay.sh` and commit the outputs.
4. Open a pull request with a summary of behavior changes and testing evidence.

———

FINT Flyt Archive Gateway is maintained by the FINT Flyt team. Reach out via the internal Slack channel or open an issue if you need enhancements or run into problems.
