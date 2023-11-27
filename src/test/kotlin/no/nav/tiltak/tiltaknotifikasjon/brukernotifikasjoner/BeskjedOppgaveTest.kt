package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.jsonGodkjentAvVeileder
import no.nav.tiltak.tiltaknotifikasjon.jsonManglerGodkjenningEndretAvtaleMelding
import no.nav.tms.varsel.action.Varseltype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BeskjedOppgaveTest {
    private val mapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)


//    @Test
//    fun `Avtalemelding Godkjent av veileder - skal lage beskjed i brukernotifikajson`() {
//        val melding: AvtaleHendelseMelding = mapper.readValue(jsonGodkjentAvVeileder)
//        val beskjedTilBrukerNotifikasjon = lagBeskjed(melding.deltakerFnr, melding.avtaleId.toString())
//
//        assertThat(beskjedTilBrukerNotifikasjon).isNotNull()
//        assertThat(beskjedTilBrukerNotifikasjon.type).isEqualTo(Varseltype.Beskjed)
//        println(beskjedTilBrukerNotifikasjon.minSideJson)
//        assertThat(beskjedTilBrukerNotifikasjon.minSideJson).isNotEmpty()
//    }

//    @Test
//    fun `Avtalemelding med status MANGLER_GODKJENNING - skal lage oppgave i brukernotifikajson`() {
//
//        val melding: AvtaleHendelseMelding = mapper.readValue(jsonManglerGodkjenningEndretAvtaleMelding)
//        val oppgaveTilBrukerNotifikasjon = lagOppgave(melding.deltakerFnr, melding.avtaleId.toString())
//        assertThat(oppgaveTilBrukerNotifikasjon.type).isEqualTo(Varseltype.Oppgave)
//        println(oppgaveTilBrukerNotifikasjon.minSideJson)
//        assertThat(oppgaveTilBrukerNotifikasjon.minSideJson).isNotEmpty()
//
//    }
}