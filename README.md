# FINT Flyt Archive Gateway

Dispatches mapped instances to the FINT archive, and serves archive kodeverk, templates and sak lookups to the
frontend and other FLYT services. C4 diagrams are in [docs/architecture.md](docs/architecture.md).

- **Dispatch:** a Kafka listener reads `instance-mapped` one record at a time. `DispatchService` creates or finds the
  sak, then posts journalposter and files from file-service. The result goes to `instance-dispatched`, or to
  `instance-error` when the archive declines or the dispatch fails. A failing record is logged and skipped.
- **Resources:** `FintResourcePublishingConfiguration` pulls changed archive resources every 15 minutes, caches them
  (`FlytCache`) and publishes most of them to entity topics, which acos-gateway reads. The last-updated timestamps are
  reset once a day at a random time between 03:00 and 04:00, so the next pull fetches everything.

## HTTP API

Base path: `/api/intern/arkiv`, for FLYT users.

| Method | Path                                                | Description                                                                                            | Response                                                                                                                                                                                                           |
|--------|-----------------------------------------------------|--------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GET`  | `/saker/{caseYear}/{caseNumber}/tittel`             | Fetches the case title for the given Noark `mappeId`.                                                  | `200 OK` with `{ "value": "<title>" }`; `404` if unknown.                                                                                                                                                          |
| `GET`  | `/kodeverk/{type}`                                  | Returns cached kodeverk entries (administrativEnhet, arkivdel, dokumentstatus, journalposttype, etc.). | `200 OK` array of `ResourceReference { "id": "<selfLink>", "displayName": "[code] Name #systemId" }`; arkivressurs also exposes `functionalId`, `name`, and `technicalId`, using empty strings for missing values. |
| `GET`  | `/kodeverk/klassifikasjonssystem`                   | Lists all klassifikasjonssystemer with their IDs and titles.                                           | `200 OK` array.                                                                                                                                                                                                    |
| `GET`  | `/kodeverk/klasse?klassifikasjonssystemLink=<link>` | Lists classes that belong to a klassifikasjonssystem link.                                             | `200 OK` array; `404` if the system is missing.                                                                                                                                                                    |
| `GET`  | `/template`                                         | The mapping template the frontend builds the archive configuration from.                               | `200 OK`                                                                                                                                                                                                           |

### Internal client API

Base path: `/api/intern-klient/arkiv`, for other FLYT services (acos-gateway and egrunnerverv-gateway). Only tokens
from `fint-flyt-authorization-oauth2-client` are accepted.

| Method | Path                             | Description                                                                                       | Response                                                                                        |
|--------|----------------------------------|---------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `GET`  | `/saker/{caseYear}/{caseNumber}` | Fetches the sak for the given Noark `mappeId`.                                                    | `200 OK` with `SakResource`; `404` if unknown; `5xx` if FINT fails.                             |
| `GET`  | `/saker?archiveInstanceId=<id>`  | Fetches the sak with only the journalposter listed in the archive instance id (`2024/123-[1,2]`). | `200 OK` with `SakResource`; `400` if the id is invalid; `404` if unknown; `5xx` if FINT fails. |

### Swagger

Swagger UI (`/swagger-ui.html`) and OpenAPI (`/v3/api-docs`, `/v3/api-docs.yaml`) are open, but outside the ingress
route, so they are only reached with a port-forward:

```shell
kubectl -n fintlabs-no port-forward service/fint-flyt-archive-gateway 8080:8080
```

Then open `http://localhost:8080/beta/fintlabs-no/swagger-ui.html`.

## Security

[flyt-security-starter](https://github.com/FINTLabs/fint-flyt-security-starter) secures the API:

- `/api/intern/**` takes FLYT user tokens with `aud` = `novari.flyt.security.user.audience`, from the namespace's
  organization, `novari.flyt.security.tenant.additional-org-ids` (`frid-iks.no` in afk, bfk and ofk) or the operators
  `vigo.no` and `novari.no`.
- `/api/intern-klient/**` takes client tokens whose `sub` is `fint.flyt.authorization.sso.client-id`.

Outbound calls use NAM: the password grant for the FINT archive (`fint.core.oauth2.*`), and client credentials for
file-service (`fint.sso.*`). Spring Security 7 has no password grant, so `PasswordGrantAuthorizedClientProvider`
implements it.

## Configuration

Profiles included by default: `fint-client`, `fint-oauth2-idp`, `flyt-file-client`, `flyt-kafka`, `flyt-logging`,
`flyt-security`.

| Property                                                               | Description                                                                |
|------------------------------------------------------------------------|----------------------------------------------------------------------------|
| `fint.application-id`                                                  | Kafka consumer group and application id (`fint-flyt-archive-gateway`).     |
| `fint.org-id`, `novari.kafka.topic.org-id`                             | The namespace's organization and topic prefix, set per overlay.            |
| `novari.flyt.archive.gateway.client.fint-archive.base-url`             | The FINT API, set per overlay (the archive simulator in fintlabs-no beta). |
| `novari.flyt.archive.gateway.kafka.topic.instance-processing-events-*` | Retention of `instance-dispatched` and `instance-error`.                   |
| `novari.flyt.archive.gateway.dispatch.fint-client.*`                   | Polling of the archive's created location after a post.                    |
| `novari.flyt.archive.gateway.resource.fint-client.*`                   | Retries of sak searches.                                                   |
| `novari.flyt.archive.gateway.resource.publishing.*`                    | Pull interval and the daily reset window.                                  |
| `novari.flyt.archive.gateway.http-client.*`                            | Connect and read timeouts for the FINT archive and file-service.           |
| `novari.flyt.security.*`                                               | See [Security](#security).                                                 |

## Running locally

Start Kafka on `localhost:9092` with `docker compose up -d` (`--profile tools` adds Kafdrop on
http://localhost:19000). Set `fint.core.oauth2.*`, `fint.sso.*` and `fint.flyt.authorization.sso.client-id`, then:

```shell
SPRING_PROFILES_ACTIVE=local-staging ./gradlew bootRun
```

The app listens on port 8301 and talks to the FINT beta API.

## Deployment

`kustomize/overlays/<org>/<env>/` are generated from `kustomize/templates/overlay.yaml.tpl` by
`script/render-overlay.sh`. Run the script after changing the template or the script, and commit the overlays.

## Development

```shell
./gradlew check                       # tests and ktlint
./gradlew dependencies --write-locks  # after changing dependencies
```

The build uses [dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html). Versions
outside the Spring Boot BOM are in `gradle/libs.versions.toml`. Dependabot updates the lockfiles in its own pull
requests; after a manual dependency change, run `--write-locks` and commit the lockfiles, or the build fails.
