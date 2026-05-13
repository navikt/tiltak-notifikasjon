package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import de.huxhorn.sulky.ulid.ULID
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import java.time.Instant
import java.util.UUID

data class RefusjonKontaktperson(
    val id: String,
    val refusjonKontaktpersonTlf: String,
    val arbeidsgiverOnskerOgsaVarsling: Boolean?,
    val avtaleId: UUID,
    val avtaleInnholdVersjon: Int,
    val avtaleHendelseType: HendelseType,
    val avtaleHendelseSistEndret: Instant,
    val topicOffset: Long,
    val innlestTidspunkt: Instant,
)
