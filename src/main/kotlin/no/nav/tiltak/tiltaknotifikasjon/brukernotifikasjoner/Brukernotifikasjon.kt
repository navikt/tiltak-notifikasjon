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

enum class Varslingsformål {
    GODKJENNING_AV_AVTALE,
    GODKJENNING_AV_TAUSHETSERKLÆRING_MENTOR,
    AVTALE_FORLENGET,
    AVTALE_FORKORTET,
    AVTALE_ANNULLERT,
    AVTALE_INNGÅTT,
}