package no.novari.flyt.archive.gateway.template

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jacksonObjectMapper

class MappingTemplateSerializationTest {
    private val objectMapper = jacksonObjectMapper()

    private val archiveTemplateService = TestTemplateServices.archiveTemplateService()

    @Test
    fun `the created template is serializable`() {
        val template = archiveTemplateService.createTemplate()

        assertThatCode { objectMapper.writeValueAsString(template) }.doesNotThrowAnyException()
    }

    @Test
    fun `serialized API template includes nested avskrivning with date and inline choices`() {
        val json = objectMapper.readTree(objectMapper.writeValueAsString(archiveTemplateService.createTemplate()))
        val journalpost =
            json["rootObjectTemplate"]["objectCollectionTemplates"].single {
                it["elementConfig"]["key"].asString() == "journalpost"
            }["template"]["elementTemplate"]
        val avskrivning =
            journalpost["objectTemplates"].single {
                it["elementConfig"]["key"].asString() == "avskrivning"
            }["template"]
        val avskrivningsdato = avskrivning["valueTemplates"].single()
        assertThat(avskrivningsdato["elementConfig"]["key"].asString()).isEqualTo("avskrivningsdato")
        assertThat(avskrivningsdato["template"]["type"].asString()).isEqualTo("DYNAMIC_STRING")
        assertThat(journalpost["selectableValueTemplates"].values().map { it["elementConfig"]["key"].asString() })
            .doesNotContain("avskrivningsmate")
        val avskrivningsmate =
            avskrivning["selectableValueTemplates"].single {
                it["elementConfig"]["key"].asString() == "avskrivningsmate"
            }["template"]

        assertThat(avskrivningsmate["type"].asString()).isEqualTo("DYNAMIC_STRING_OR_SEARCH_SELECT")
        assertThat(avskrivningsmate["selectables"].values().map { it["value"].asString() })
            .containsExactly("BU", "BE", "TLF", "TE", "TO")
        assertThat(avskrivningsmate.has("selectablesSources")).isFalse()
        assertThat(avskrivningsmate.has("referenceTemplate")).isFalse()
    }
}
