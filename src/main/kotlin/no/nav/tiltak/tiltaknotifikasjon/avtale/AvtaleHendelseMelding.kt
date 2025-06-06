package no.nav.tiltak.tiltaknotifikasjon.avtale

import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.NotifikasjonTekst
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import java.math.BigDecimal
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
    val stillingprosent: BigDecimal?,
    val avtaleInngått: LocalDateTime?,
    val utførtAv: String,
    val utførtAvRolle: AvtaleHendelseUtførtAvRolle,
    val deltakerFnr: String,
    val deltakerFornavn: String?,
    val deltakerEtternavn: String?,
    val arbeidsgiverTlf: String?,
    val avtaleId: UUID,
    val avtaleNr: Int,
    val sistEndret: Instant,
    val veilederNavIdent: String?,
    val annullertGrunn: String?,
    val antallDagerPerUke: BigDecimal?,
    val godkjentAvDeltaker: LocalDateTime?,
    val feilregistrert: Boolean,
    val opphav: AvtaleOpphav?
)

enum class AvtaleOpphav {
    ARBEIDSGIVER,
    ARENA,
    VEILEDER
}

/** grupperingsId for saker, beskjeder og oppgaver. Bruker avtaleId, alle notifikasjoner med denne grupperingsIden vil knyttes sammen */
fun AvtaleHendelseMelding.grupperingsId(): String = avtaleId.toString()

/** ID sammensatt av avtaleNr og en random ulid (avtaleNr_ulid) Skal være unik for hver notifikasjon hos fager.
 * VI BRUKER IKKE DENNE. Vi slår opp på oppgaver via ID'en Fager returnerer */
fun AvtaleHendelseMelding.eksternId(): String = "${avtaleNr}_${ulid()}"
/** Lag tekst til notifikasjon for min side arbeidsgiver. Baserer seg på HendelsesType. Skiller på sak og oppgave. */
fun AvtaleHendelseMelding.lagArbeidsgivernotifikasjonTekst(erSak: Boolean): String =
    when (this.hendelseType) {
        HendelseType.OPPRETTET -> if (erSak) NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET_SAK.tekst(this)
            else NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET.tekst(this)
        HendelseType.AVTALE_INNGÅTT -> if (erSak) NotifikasjonTekst.TILTAK_AVTALE_INNGATT_SAK.tekst(this) else NotifikasjonTekst.TILTAK_AVTALE_INNGATT.tekst(this)
        HendelseType.STILLINGSBESKRIVELSE_ENDRET -> NotifikasjonTekst.TILTAK_STILLINGSBESKRIVELSE_ENDRET.tekst(this)
        HendelseType.MÅL_ENDRET -> NotifikasjonTekst.TILTAK_MÅL_ENDRET.tekst(this)
        HendelseType.INKLUDERINGSTILSKUDD_ENDRET -> NotifikasjonTekst.TILTAK_INKLUDERINGSTILSKUDD_ENDRET.tekst(this)
        HendelseType.OM_MENTOR_ENDRET -> NotifikasjonTekst.TILTAK_OM_MENTOR_ENDRET.tekst(this)
        HendelseType.OPPFØLGING_OG_TILRETTELEGGING_ENDRET -> NotifikasjonTekst.TILTAK_OPPFØLGING_OG_TILRETTELEGGING_ENDRET.tekst(this)
        HendelseType.AVTALE_FORKORTET -> NotifikasjonTekst.TILTAK_AVTALE_FORKORTET.tekst(this)
        HendelseType.AVTALE_FORLENGET -> NotifikasjonTekst.TILTAK_AVTALE_FORLENGET.tekst(this)
        HendelseType.TILSKUDDSBEREGNING_ENDRET -> NotifikasjonTekst.TILTAK_TILSKUDDSBEREGNING_ENDRET.tekst(this)
        HendelseType.ARBEIDSGIVERS_GODKJENNING_OPPHEVET_AV_VEILEDER -> NotifikasjonTekst.TILTAK_GODKJENNINGER_OPPHEVET_AV_VEILEDER.tekst(this)
        HendelseType.KONTAKTINFORMASJON_ENDRET -> NotifikasjonTekst.TILTAK_KONTAKTINFORMASJON_ENDRET.tekst(this)
        HendelseType.ANNULLERT -> NotifikasjonTekst.TILTAK_AVTALE_ANNULLERT.tekst(this)
        else -> {
            throw IllegalArgumentException("HendelseType $hendelseType har ikke definert notifikasjonstekst")
        }
    }
fun AvtaleHendelseMelding.erArbeidsgiversTlfGyldigNorskMobilnr(): Boolean = arbeidsgiverTlf?.matches(Regex("^(\\+47|0047)?(4|9)\\d{7}$")) ?: false
