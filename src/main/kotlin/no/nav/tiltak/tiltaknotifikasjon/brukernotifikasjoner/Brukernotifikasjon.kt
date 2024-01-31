package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import java.time.Instant

data class Brukernotifikasjon(
    val id: String,
    var varselId: String? = null,
    val avtaleMeldingJson: String,
    var minSideJson: String? = null,
    var type: BrukernotifikasjonType? = null,
    var status: BrukernotifikasjonStatus,
    val deltakerFnr: String? = null,
    val avtaleId: String? = null,
    val avtaleNr: Int? = null,
    val avtaleHendelseType: HendelseType? = null,
    var feilmelding: String? = null,
    var sendt: Instant? = null,
    var opprettet: Instant = Instant.now(),
    val varslingsformål: Varslingsformål? = null
)

enum class BrukernotifikasjonStatus {
    MOTTATT, BEHANDLET, FEILET_VED_SENDING, FEILET_VED_PARSING, INAKTIVERT
}

enum class BrukernotifikasjonType {
    Beskjed, Oppgave, Innboks, Inaktivering
}

enum class Varslingsformål(val tekst: String) {
    GODKJENNING_AV_AVTALE ("Du har en avtale om tiltak som venter på din godkjenning"),
    GODKJENNING_AV_TAUSHETSERKLÆRING_MENTOR("Du har en taushetserklæring som venter på din godkjenning"),
    AVTALE_FORLENGET("Din avtale om tiltak er forlenget"),
    AVTALE_FORKORTET("Din avtale om tiltak er forkortet"),
    AVTALE_ANNULLERT("Din avtale om tiltak er annullert"),
    AVTALE_INNGÅTT("Din avtale om tiltak er inngått"),
}