package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tms.varsel.action.Varseltype

data class Brukernotifikasjon(
    val minSideJson: String,
    val id: String,
    val type: Varseltype,
)