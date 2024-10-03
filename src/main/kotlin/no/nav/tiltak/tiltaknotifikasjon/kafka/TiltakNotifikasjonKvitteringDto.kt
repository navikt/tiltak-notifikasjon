package no.nav.tiltak.tiltaknotifikasjon.kafka

import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.Arbeidsgivernotifikasjon
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.sendtSms
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.Brukernotifikasjon
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.sendtSms
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import java.time.Instant
import java.util.*

//TODO: Gå over flyway-script
data class TiltakNotifikasjonKvitteringDto(
    val notifikasjonstype: NotifikasjonsType,
    val payload: String,
    var feilmelding: String? = null,
    var sendtTidspunkt: Instant? = null,
    val avtaleHendelseType: HendelseType,
    val mottaker: String,
    val sendtSms: Boolean,
    val avtaleId: UUID,
    var json: String? = null
//  val tiltakstype: Tiltakstype // Trenger vel ikke dette
//  val notifikasjonTekst: String, // Trenger vel ikke dette
) {
    val id = ulid()
    val opprettetTidspunkt: Instant = Instant.now()
}

enum class NotifikasjonsType {
    BRUKERNOTIFIKASJON, ARBEIDSGIVERNOTIFIKASJON
}

fun TiltakNotifikasjonKvitteringDto.toJson(): String {
    return jacksonMapper().writeValueAsString(this)
}


fun kvitterinFraArbeidsgivernotifikasjon(arbeidsgivernotifikasjon: Arbeidsgivernotifikasjon): TiltakNotifikasjonKvitteringDto {
    return TiltakNotifikasjonKvitteringDto(
        notifikasjonstype = NotifikasjonsType.ARBEIDSGIVERNOTIFIKASJON,
        payload = arbeidsgivernotifikasjon.arbeidsgivernotifikasjonJson!!,
        avtaleHendelseType = arbeidsgivernotifikasjon.avtaleHendelseType!!,
        mottaker = arbeidsgivernotifikasjon.bedriftNr!!,
        avtaleId = UUID.fromString(arbeidsgivernotifikasjon.avtaleId),
        sendtSms = arbeidsgivernotifikasjon.sendtSms()
    )
}

fun kvitteringFraBrukernoitfikasjon(brukernotifikasjon: Brukernotifikasjon): TiltakNotifikasjonKvitteringDto {
    return TiltakNotifikasjonKvitteringDto(
        notifikasjonstype = NotifikasjonsType.BRUKERNOTIFIKASJON,
        payload = brukernotifikasjon.minSideJson!!,
        avtaleHendelseType = brukernotifikasjon.avtaleHendelseType!!,
        mottaker = brukernotifikasjon.deltakerFnr!!,
        avtaleId = UUID.fromString(brukernotifikasjon.avtaleId),
        sendtSms = brukernotifikasjon.sendtSms()
    )
}