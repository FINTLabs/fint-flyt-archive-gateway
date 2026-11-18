# Architecture

C4 level 1 to 3 for fint-flyt-archive-gateway. Blue is this service, turquoise is external.

## C4 level 1: System context

Archive-gateway is the last step in the FLYT flow. It sends a mapped instance to the archive as a
sak with journalposter and documents, and serves archive data to the frontend and the gateways.

```mermaid
C4Context
    title System context for fint-flyt-archive-gateway

    System_Ext(frontend, "fint-flyt-frontend", "Users build configurations against the archive.")
    System_Ext(gateways, "acos- and egrunnerverv-gateway", "Look up a sak after archiving.")
    System(archive, "archive-gateway", "Sends instances to the archive and serves archive data.")
    System_Ext(fint, "FINT Arkiv", "The archive of the county, through the FINT API.")
    System_Ext(flyt, "Other FLYT services", "mapping-service, history-service, file-service.")
    System_Ext(slack, "Slack", "Alerts.")

    Rel(frontend, archive, "Kodeverk, sak title", "HTTPS")
    Rel(gateways, archive, "Sak", "HTTP")
    Rel(archive, fint, "Sak, journalpost", "HTTPS")
    BiRel(archive, flyt, "Events, files", "Kafka, HTTP")
    Rel(archive, slack, "Alerts")

    UpdateRelStyle(frontend, archive, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(gateways, archive, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(archive, fint, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateRelStyle(archive, flyt, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(archive, slack, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateElementStyle(archive, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(frontend, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(gateways, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(fint, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(flyt, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(slack, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

- **Other FLYT services:** mapping-service publishes `instance-mapped`. Archive-gateway publishes
  `instance-dispatched` or an `instance-error`, which history-service stores. Documents are fetched
  from file-service.
- **acos-gateway and egrunnerverv-gateway** look up the sak of an archived instance over
  `/api/intern-klient/arkiv/saker`, with a token for `fint-flyt-authorization-oauth2-client`, the
  shared client for internal calls. This replaced the `arkiv-noark-sak` request/reply topics
  (FFS-2391, removed in FFS-2400).
- **FINT Arkiv** is called with the FINT client of the org. Tokens come from NAM (not shown).
- **Slack** gets an alert when a dispatch call to the archive fails.

## C4 level 2: Containers

One Spring Boot application with no database. Archive resources are cached in memory.

```mermaid
C4Container
    title Containers

    System_Ext(frontend, "fint-flyt-frontend", "Browser app.")
    System_Ext(gateways, "acos- and egrunnerverv-gateway", "Internal client API.")
    Container(app, "archive-gateway", "Kotlin, Spring Boot 3, Java 25", "Dispatch to the archive, kodeverk cache, sak lookups.")
    System_Ext(fint, "FINT Arkiv", "FINT API.")
    ContainerQueue_Ext(kafka, "Kafka", "Topics", "instance-mapped, instance-dispatched, instance-error, archive resources")
    Container_Ext(file, "file-service", "Spring Boot", "Documents of an instance.")

    Rel(frontend, app, "API", "HTTPS, JWT")
    Rel(gateways, app, "Sak", "HTTP")
    Rel(app, fint, "Reads, writes", "HTTPS")
    BiRel(app, kafka, "Events", "flyt-kafka")
    Rel(app, file, "Files", "HTTP")

    UpdateRelStyle(frontend, app, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(gateways, app, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(app, fint, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateRelStyle(app, kafka, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(app, file, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateElementStyle(app, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(frontend, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(gateways, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(fint, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(kafka, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(file, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

- The ingress only routes `/api/intern/arkiv`. The internal client API, Swagger UI, OpenAPI and
  Actuator are only reachable inside the cluster.
- The archive resources (kodeverk, arkivressurser and more) are also published on entity topics.

## C4 level 3: Components

### Dispatch

```mermaid
C4Component
    title Dispatch

    ContainerQueue_Ext(kafka, "Kafka", "Topics", "instance-mapped, instance-dispatched, instance-error")
    Component(consumer, "Kafka consumer", "flyt-kafka", "One instance-mapped record at a time.")
    Component(producers, "Kafka producers", "flyt-kafka", "instance-dispatched, or an instance-error when declined or failed.")
    Component(dispatch, "DispatchService", "Service", "New sak, existing sak or search-or-new, then the journalposter.")
    Component(caseDispatch, "CaseDispatchService", "Service", "Creates or finds the sak.")
    Component(records, "Records dispatch", "Service", "Journalposter and their documents.")
    Component(fintClient, "FintArchiveDispatchClient", "RestClient", "Posts to FINT and polls the created location.")
    Component(fileClient, "FlytFileClient", "RestClient", "Fetches documents.")
    System_Ext(fint, "FINT Arkiv", "FINT API.")
    Container_Ext(file, "file-service", "Spring Boot", "Documents of an instance.")

    Rel(kafka, consumer, "Consumes")
    Rel(consumer, producers, "Result")
    Rel(consumer, dispatch, "process()")
    Rel(producers, kafka, "Sends")
    Rel(dispatch, caseDispatch, "Sak")
    Rel(dispatch, records, "Journalposter")
    Rel(caseDispatch, fintClient, "Posts")
    Rel(records, fintClient, "Posts")
    Rel(records, fileClient, "Fetches")
    Rel(fintClient, fint, "POST, GET", "HTTPS")
    Rel(fileClient, file, "GET", "HTTP")

    UpdateRelStyle(kafka, consumer, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateRelStyle(consumer, producers, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(consumer, dispatch, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(producers, kafka, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(dispatch, caseDispatch, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(dispatch, records, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(caseDispatch, fintClient, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(records, fintClient, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(records, fileClient, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(fintClient, fint, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(fileClient, file, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateElementStyle(consumer, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(producers, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(dispatch, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(caseDispatch, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(records, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(fintClient, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(fileClient, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(kafka, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(fint, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(file, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

- `InstanceMappedEventConsumerConfiguration` hands each record to `DispatchService` and publishes
  the result with `InstanceDispatchedEventProducerService` or `InstanceErrorEventProducerService`.
  Failed records are skipped without retry.
- Records dispatch is `RecordsProcessingService`, `RecordsDispatchService`, `RecordDispatchService`
  and `FilesDispatchService`.
- `FintArchiveDispatchClient` posts to FINT, which answers `202 Accepted` with a location. The
  client polls the location until the sak or journalpost is created, or until the timeout.

### Archive data

```mermaid
C4Component
    title Archive data

    System_Ext(frontend, "fint-flyt-frontend", "Browser app.")
    System_Ext(gateways, "acos- and egrunnerverv-gateway", "Internal client API.")
    Component(codelist, "CodelistController", "Spring MVC", "Kodeverk under /api/intern/arkiv.")
    Component(caseApi, "Sak API", "Spring MVC", "CaseController and InternalClientCaseController.")
    Component(caches, "Resource caches", "FintCache", "One cache per resource type.")
    Component(caseService, "CaseService", "Service", "Sak by mappeId or archiveInstanceId.")
    Component(publishing, "Resource publishing", "Spring @Scheduled", "Pulls changed resources, fills the caches, publishes them.")
    Component(resourceClient, "FintArchiveResourceClient", "RestClient", "Reads from FINT.")
    ContainerQueue_Ext(kafka, "Kafka", "Topics", "Archive resource entity topics")
    System_Ext(fint, "FINT Arkiv", "FINT API.")

    Rel(frontend, codelist, "GET", "HTTPS")
    Rel(gateways, caseApi, "GET", "HTTP")
    Rel(frontend, caseApi, "Sak title", "HTTPS")
    Rel(codelist, caches, "Reads")
    Rel(caseApi, caseService, "Uses")
    Rel(publishing, caches, "Fills")
    Rel(publishing, resourceClient, "Pulls")
    Rel(caseService, resourceClient, "Reads")
    Rel(publishing, kafka, "Publishes")
    Rel(resourceClient, fint, "GET", "HTTPS")

    UpdateRelStyle(frontend, codelist, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(gateways, caseApi, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(frontend, caseApi, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="20")
    UpdateRelStyle(codelist, caches, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(caseApi, caseService, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(publishing, caches, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(publishing, resourceClient, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="-35", $offsetY="-30")
    UpdateRelStyle(caseService, resourceClient, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(publishing, kafka, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateRelStyle(resourceClient, fint, $textColor="#3B82F6", $lineColor="#3B82F6", $offsetX="10")
    UpdateElementStyle(codelist, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(caseApi, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(caches, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(caseService, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(publishing, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(resourceClient, $bgColor="#93C5FD", $fontColor="#1F2937", $borderColor="#3B82F6")
    UpdateElementStyle(frontend, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(gateways, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(kafka, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateElementStyle(fint, $bgColor="#A5F3FC", $fontColor="#1F2937", $borderColor="#06B6D4")
    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

- `FintResourcePublishingConfiguration` pulls changed resources on a fixed delay and clears the
  "last updated" timestamps once a day, so the next pull fetches everything again.
- `CaseController` returns the title of a sak to the frontend. `InternalClientCaseController`
  returns the whole sak to the gateways, or a sak with only the journalposter in an
  `archiveInstanceId` such as `2024/123-[1,2]`. See [Internal client API](../README.md#internal-client-api).
- `ArchiveTemplateController` returns the mapping template the frontend builds archive
  configurations from. It is static and does not call FINT.
