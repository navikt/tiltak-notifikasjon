package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import java.time.Instant

data class Arbeidsgivernotifikasjon(
    val id: String,
    var varselId: String? = null,
    val avtaleMeldingJson: String,
    var notifikasjonJson: String? = null,
    var type: ArbeidsgivernotifikasjonType? = null,
    var status: ArbeidsgivernotifikasjonStatus,
    val bedriftNr: String? = null,
    val avtaleHendelseType: HendelseType? = null,
    var feilmelding: String? = null,
    var sendt: Instant? = null,
    var opprettet: Instant = Instant.now(),
    val varslingsformål: Varslingsformål? = null,
    val avtaleId: String? = null,
    val avtaleNr: Int? = null,
    var responseId: String? = null,
    // TODO: slettetTdispunkt el. og hardDeleteSkedulertTidspunkt
)

enum class ArbeidsgivernotifikasjonStatus {
    BEHANDLET, FEILET_VED_BEHANDLING, FEILET_VED_SENDING, FEILET_VED_OPPRETTELSE_HOS_FAGER, SLETTET, OPPGAVE_FERDIGSTILT, SAK_ANNULLERT, SAK_MOTTATT
}

enum class ArbeidsgivernotifikasjonType {
    Beskjed, Oppgave, Sak, FerdigstillOppgave, SoftDeleteNotifikasjon, SoftDeleteSak, NySakStatus
}

enum class Varslingsformål {
    GODKJENNING_AV_AVTALE,
    AVTALE_FORLENGET,
    AVTALE_FORKORTET,
    AVTALE_ANNULLERT,
    AVTALE_INNGÅTT,
    INGEN_VARSLING,
    MÅL_ENDRET,
    INKLUDERINGSTILSKUDD_ENDRET,
    OM_MENTOR_ENDRET,
    STILLINGSBESKRIVELSE_ENDRET,
    OPPFØLGING_OG_TILRETTELEGGING_ENDRET,
    TILSKUDDSBEREGNING_ENDRET,
    KONTAKTINFORMASJON_ENDRET,
}
