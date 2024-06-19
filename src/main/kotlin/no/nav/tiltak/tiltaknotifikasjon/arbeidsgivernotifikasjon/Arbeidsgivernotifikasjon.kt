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
)

enum class ArbeidsgivernotifikasjonStatus {
    BEHANDLET, FEILET_VED_BEHANDLING, FEILET_VED_SENDING, FEILET_VED_OPPRETTELSE_HOS_FAGER, SLETTET
}

enum class ArbeidsgivernotifikasjonType {
    Beskjed, Oppgave, Sak, FerdigstillOppgave, SoftDeleteNotifikasjon, SoftDeleteSak
}

enum class Varslingsformål {
    GODKJENNING_AV_AVTALE,
    AVTALE_FORLENGET,
    AVTALE_FORKORTET,
    AVTALE_ANNULLERT,
    AVTALE_INNGÅTT,
    INGEN_VARSLING
}
