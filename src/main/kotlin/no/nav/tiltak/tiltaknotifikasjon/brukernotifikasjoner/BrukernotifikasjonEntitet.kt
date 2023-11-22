package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tms.varsel.action.Varseltype

data class BrukernotifikasjonEntitet(
    val avtaleMeldingJson: String,
    val minSideJson: String,
    val id: String,
    val type: Varseltype,
    val status: BrukernotifikasjonStatus,
    val feilmelding: String? = null
)

enum class BrukernotifikasjonStatus {
    MOTTATT, BEHANDLET, SENDT_TIL_MIN_SIDE, FEILET
}
