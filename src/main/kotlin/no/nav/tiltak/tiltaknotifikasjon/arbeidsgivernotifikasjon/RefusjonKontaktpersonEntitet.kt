package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
import java.time.Instant
import java.util.UUID

data class RefusjonKontaktpersonEntitet(
    val avtaleId: UUID,
    val bedriftNr: String,
    val refusjonKontaktpersonTlf: String?,
    val arbeidsgiverOnskerOgsaVarsling: Boolean?,
    val arbeidsgiverTlf: String,
    val tiltakstype: Tiltakstype,
    val avtaleInnholdVersjon: Int,
    val avtaleHendelseType: HendelseType,
    val avtaleHendelseSistEndret: Instant,
    val topicOffset: Long,
    val innlestTidspunkt: Instant,
)
