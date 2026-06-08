package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import java.time.Instant
import java.util.*

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


/** ID sammensatt av avtaleNr og en random ulid (avtaleNr_ulid) Skal være unik for hver notifikasjon hos fager.
 * VI BRUKER IKKE DENNE. Vi slår opp på oppgaver via ID'en Fager returnerer */
fun RefusjonKontaktpersonEntitet.eksternId(): String = "${avtaleId}_${ulid()}"

/** grupperingsId for saker, beskjeder og oppgaver knyttet til refusjoner. Alle notifikasjoner med denne grupperingsIden vil knyttes sammen */
fun RefusjonKontaktpersonEntitet.grupperingsId(): String = "${avtaleId}-refusjoner"
