package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import java.time.Instant
import java.time.LocalDateTime

data class Arbeidsgivernotifikasjon(
    val id: String,
    val avtaleMeldingJson: String,
    var arbeidsgivernotifikasjonJson: String? = null,
    var type: ArbeidsgivernotifikasjonType? = null,
    var status: ArbeidsgivernotifikasjonStatus,
    val bedriftNr: String? = null,
    val avtaleHendelseType: HendelseType? = null,
    var feilmelding: String? = null,
    var sendtTidspunkt: Instant? = null,
    val opprettetTidspunkt: Instant = Instant.now(),
    val varslingsformål: Varslingsformål? = null,
    val avtaleId: String? = null,
    val avtaleNr: Int? = null,
    /** id'en som notifikasjonen har hos fager. Den blir generert av de ved opprettelse og returnert */
    var responseId: String? = null,
    /** Tidspunktet notifikasjonen er skedulert til å slettes. api-et forventer LocalDateTime i Europe/Oslo tidssone */
    var hardDeleteSkedulertTidspunkt: LocalDateTime? = null
)

enum class ArbeidsgivernotifikasjonStatus {
    BEHANDLET, FEILET_VED_BEHANDLING, FEILET_VED_SENDING, FEILET_VED_OPPRETTELSE_HOS_FAGER, SLETTET, OPPGAVE_FERDIGSTILT, SAK_ANNULLERT, SAK_MOTTATT, SAK_FERDIG
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
