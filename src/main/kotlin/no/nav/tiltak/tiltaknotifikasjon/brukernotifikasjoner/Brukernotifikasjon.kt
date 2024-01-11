package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import java.time.Instant
import java.time.LocalDateTime

data class Brukernotifikasjon(
    val id: String,
    val varselId: String,
    val avtaleMeldingJson: String?,
    val minSideJson: String?,
    val type: BrukernotifikasjonType,
    var status: BrukernotifikasjonStatus,
    val deltakerFnr: String,
    val avtaleId: String,
    val avtaleNr: Int,
    val avtaleHendelseType: HendelseType,
    var feilmelding: String? = null,
    var sendt: Instant? = null,
    var opprettet: Instant? = null
)

enum class BrukernotifikasjonStatus {
    MOTTATT, BEHANDLET, FEILET, INAKTIVERT
}

enum class BrukernotifikasjonType {
    Beskjed, Oppgave, Innboks, Inaktivering
}