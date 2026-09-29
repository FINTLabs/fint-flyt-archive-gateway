package no.novari.flyt.archive.gateway.template

object TestTemplateServices {
    const val FINT_BASE_URL = "https://api.felleskomponent.no"

    val referenceTemplateFactory = ReferenceTemplateFactory(FINT_BASE_URL)

    fun journalpostTemplateService() =
        JournalpostTemplateService(
            KorrespondansepartTemplateService(
                AdresseTemplateService(),
                KontaktinformasjonTemplateService(),
                skjermingTemplateService(),
                referenceTemplateFactory,
            ),
            DokumentbeskrivelseTemplateService(
                DokumentobjektTemplateService(referenceTemplateFactory),
                skjermingTemplateService(),
                referenceTemplateFactory,
            ),
            skjermingTemplateService(),
            referenceTemplateFactory,
        )

    fun archiveTemplateService() =
        ArchiveTemplateService(
            SearchParametersTemplateService(),
            SakTemplateService(
                KlasseringTemplateService(skjermingTemplateService(), referenceTemplateFactory),
                skjermingTemplateService(),
                journalpostTemplateService(),
                PartTemplateService(
                    AdresseTemplateService(),
                    KontaktinformasjonTemplateService(),
                    referenceTemplateFactory,
                ),
                referenceTemplateFactory,
            ),
            journalpostTemplateService(),
        )

    private fun skjermingTemplateService() = SkjermingTemplateService(referenceTemplateFactory)
}
