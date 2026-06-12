package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import java.time.Instant
import java.time.LocalDateTime

data class ArbeidsgiverRefusjonNotifikasjon(
    val id: String,
    val refusjonId: String,
    var arbeidsgivernotifikasjonJson: String,
    var type: ArbeidsgivernotifikasjonType,
    var status: ArbeidsgivernotifikasjonStatus,
    val bedriftNr: String,
    var feilmelding: String? = null,
    var sendtTidspunkt: Instant? = null,
    val opprettetTidspunkt: Instant = Instant.now(),
    val varslingsformål: Varslingsformål,
    val avtaleId: String,
    /** id'en som notifikasjonen har hos fager. Den blir generert av de ved opprettelse og returnert */
    var responseId: String? = null,
    /** Tidspunktet notifikasjonen er skedulert til å slettes. Api-et forventer LocalDateTime i Europe/Oslo tidssone */
    var hardDeleteSkedulertTidspunkt: LocalDateTime? = null

)
