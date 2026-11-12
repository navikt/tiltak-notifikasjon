package no.nav.tiltak.tiltaknotifikasjon.utils

import no.nav.tiltak.tiltaknotifikasjon.avtale.RefusjonKontaktperson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.readValue
import java.time.LocalDate

class JacksonMapperTest {

    @Test
    fun `property som starter med ø skal serialiseres og deserialiseres`() {
        val kontaktperson = RefusjonKontaktperson(refusjonKontaktpersonFornavn = "Kari", ønskerVarslingOmRefusjon = true)

        val json = jacksonMapper().writeValueAsString(kontaktperson)

        assertThat(json).contains("\"ønskerVarslingOmRefusjon\":true")
        assertThat(jacksonMapper().readValue<RefusjonKontaktperson>(json)).isEqualTo(kontaktperson)
    }

    @Test
    fun `datoer skal serialiseres som ISO-8601 og ikke som timestamp`() {
        assertThat(jacksonMapper().writeValueAsString(LocalDate.of(2026, 11, 12))).isEqualTo("\"2026-11-12\"")
    }

    @Test
    fun `enum leses uavhengig av casing og ukjente verdier blir null`() {
        val mapper = jacksonMapperSomIkkeBryrSegOmEnumCase()
        assertThat(mapper.readValue<Medlem>("""{"type":"oppgave"}""").type).isEqualTo(Type.OPPGAVE)
        assertThat(mapper.readValue<Medlem>("""{"type":"ukjent"}""").type).isNull()
    }

    private enum class Type { OPPGAVE }

    private data class Medlem(val type: Type?)
}
