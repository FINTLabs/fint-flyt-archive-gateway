package no.novari.flyt.archive.gateway.template

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

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
                it["elementConfig"]["key"].asText() == "journalpost"
            }["template"]["elementTemplate"]
        val avskrivning =
            journalpost["objectTemplates"].single {
                it["elementConfig"]["key"].asText() == "avskrivning"
            }["template"]
        val avskrivningsdato = avskrivning["valueTemplates"].single()
        assertThat(avskrivningsdato["elementConfig"]["key"].asText()).isEqualTo("avskrivningsdato")
        assertThat(avskrivningsdato["template"]["type"].asText()).isEqualTo("DYNAMIC_STRING")
        assertThat(journalpost["selectableValueTemplates"].map { it["elementConfig"]["key"].asText() })
            .doesNotContain("avskrivningsmate")
        val avskrivningsmate =
            avskrivning["selectableValueTemplates"].single {
                it["elementConfig"]["key"].asText() == "avskrivningsmate"
            }["template"]

        assertThat(avskrivningsmate["type"].asText()).isEqualTo("DYNAMIC_STRING_OR_SEARCH_SELECT")
        assertThat(avskrivningsmate["selectables"].map { it["value"].asText() })
            .containsExactly("BU", "BE", "TLF", "TE", "TO")
        assertThat(avskrivningsmate.has("selectablesSources")).isFalse()
        assertThat(avskrivningsmate.has("referenceTemplate")).isFalse()
    }
}
