package no.nav.tiltak.tiltaknotifikasjon.kafka

import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.Arbeidsgivernotifikasjon
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.mottakerTlf
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.sendtSms
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.Brukernotifikasjon
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import java.time.Instant
import java.util.*

data class TiltakNotifikasjonKvitteringDto(
    val id: String = ulid(),
    val opprettetTidspunkt: Instant = Instant.now(),
    val notifikasjonstype: NotifikasjonsType,
    val payload: String,
    var feilmelding: String? = null,
    var sendtTidspunkt: Instant? = null,
    val avtaleHendelseType: HendelseType,
    val mottaker: String,
    val mottakerTlf: String? = null,
    val sendtSms: Boolean,
    val avtaleId: UUID,
    val notifikasjonId: String
) {
    fun toJson(): String = jacksonMapper().writeValueAsString(this)
}

enum class NotifikasjonsType {
    BRUKERNOTIFIKASJON, ARBEIDSGIVERNOTIFIKASJON
}

fun kvitteringFra(arbeidsgivernotifikasjon: Arbeidsgivernotifikasjon): TiltakNotifikasjonKvitteringDto {
    return TiltakNotifikasjonKvitteringDto(
        notifikasjonstype = NotifikasjonsType.ARBEIDSGIVERNOTIFIKASJON,
        payload = arbeidsgivernotifikasjon.arbeidsgivernotifikasjonJson!!,
        avtaleHendelseType = arbeidsgivernotifikasjon.avtaleHendelseType!!,
        mottaker = arbeidsgivernotifikasjon.bedriftNr!!,
        avtaleId = UUID.fromString(arbeidsgivernotifikasjon.avtaleId),
        notifikasjonId = arbeidsgivernotifikasjon.id,
        sendtSms = arbeidsgivernotifikasjon.sendtSms(),
        mottakerTlf = arbeidsgivernotifikasjon.mottakerTlf()
    )
}

fun kvitteringFra(brukernotifikasjon: Brukernotifikasjon): TiltakNotifikasjonKvitteringDto {
    return TiltakNotifikasjonKvitteringDto(
        notifikasjonstype = NotifikasjonsType.BRUKERNOTIFIKASJON,
        payload = brukernotifikasjon.minSideJson!!,
        avtaleHendelseType = brukernotifikasjon.avtaleHendelseType!!,
        mottaker = brukernotifikasjon.deltakerFnr!!,
        avtaleId = UUID.fromString(brukernotifikasjon.avtaleId),
        notifikasjonId = brukernotifikasjon.id,
        sendtSms = true, // Sender alltid SMS til personbruker,
        mottakerTlf = "UKJENT" // Dette er det ikke vi som spesifiserer
    )
}
