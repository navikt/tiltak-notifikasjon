package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import de.huxhorn.sulky.ulid.ULID
import no.nav.tms.varsel.action.Varseltype

data class BrukernotifikasjonEntitet(
    val id: String,
    val avtaleMeldingJson: String?,
    val minSideJson: String?,
    val type: Varseltype,
    val status: BrukernotifikasjonStatus,
    val feilmelding: String? = null
)

enum class BrukernotifikasjonStatus {
    MOTTATT, BEHANDLET, SENDT_TIL_MIN_SIDE, FEILET
}
