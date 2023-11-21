package no.nav.tiltak.tiltaknotifikasjon.avtale

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class AvtaleHendelseMelding(
    val hendelseType: HendelseType,
    val tiltakstype: Tiltakstype,
    val avtaleStatus: AvtaleStatus,
    val startDato: LocalDate?,
    val sluttDato: LocalDate?,
    val bedriftNavn: String?,
    val bedriftNr: String,
    val stillingstittel: String?,
    val stillingprosent: Int?,
    val avtaleInngått: LocalDateTime?,
    val utførtAv: String,
    val utførtAvRolle: AvtaleHendelseUtførtAvRolle,
    val deltakerFnr: String,
    val avtaleId: UUID,
    val avtaleNr: Int,
    val sistEndret: Instant,
    val veilederNavIdent: String?,
    val annullertGrunn: String?,
    val antallDagerPerUke: Int?,
    val godkjentAvDeltaker: Boolean,
)