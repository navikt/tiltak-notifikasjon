package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tms.varsel.action.Varseltype

data class Brukernotifikasjon(
    val id: String,
    val varselId: String,
    val avtaleMeldingJson: String?,
    val minSideJson: String?,
    val type: Varseltype,
    var status: BrukernotifikasjonStatus,
    val deltakerFnr: String,
    val avtaleId: String,
    val avtaleNr: Int,
    val avtaleHendelseType: HendelseType,
    var feilmelding: String? = null
)

enum class BrukernotifikasjonStatus {
    MOTTATT, BEHANDLET, SENDT_TIL_MIN_SIDE, FEILET, SENDER_TIL_MIN_SIDE
}