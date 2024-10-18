package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import no.nav.tiltak.tiltaknotifikasjon.utils.Cluster
import no.nav.tiltak.tiltaknotifikasjon.utils.norskDatoFormat
import no.nav.tms.varsel.action.*
import no.nav.tms.varsel.builder.VarselActionBuilder
import org.slf4j.LoggerFactory
import java.time.ZoneId
import java.time.ZonedDateTime


const val NAMESPACE = "team-tiltak"
const val APP_NAVN = "tiltak-notifikasjon"

val log =  LoggerFactory.getLogger("BeskjedOppgave")

fun lagOppgave(avtaleHendelseMelding: AvtaleHendelseMelding, varslingsformål: Varslingsformål): Pair<String, String> {
    val id = ulid()
    val kafkaValueJson = VarselActionBuilder.opprett {
        type = Varseltype.Oppgave
        varselId = id
        sensitivitet = Sensitivitet.High
        ident = avtaleHendelseMelding.deltakerFnr
        tekster += Tekst(
            spraakkode = "nb",
            tekst = lagTekst(varslingsformål, avtaleHendelseMelding),
            default = true
        )
        link = lagLink(avtaleHendelseMelding.avtaleId.toString())
        aktivFremTil = null // Aktiv frem til oppgaven er utført eller 1 år
        eksternVarsling = EksternVarslingBestilling(prefererteKanaler = listOf(EksternKanal.SMS))
        produsent = Produsent(Cluster.current.verdi, NAMESPACE, APP_NAVN)
    }
    return Pair(id, kafkaValueJson)
}

fun lagBeskjed(avtaleHendelseMelding: AvtaleHendelseMelding, varslingsformål: Varslingsformål): Pair<String, String> {
    val id = ulid()
    val kafkaValueJson = VarselActionBuilder.opprett {
        type = Varseltype.Beskjed
        varselId = id
        sensitivitet = Sensitivitet.High
        ident = avtaleHendelseMelding.deltakerFnr
        tekster += Tekst(
            spraakkode = "nb",
            tekst = lagTekst(varslingsformål, avtaleHendelseMelding),
            default = true
        )
        link = lagLink(avtaleHendelseMelding.avtaleId.toString())
        aktivFremTil = null // Aktiv frem til beskjeden er lest eller 1 år
        eksternVarsling = EksternVarslingBestilling(prefererteKanaler = listOf(EksternKanal.SMS))
        produsent = Produsent(Cluster.current.verdi, NAMESPACE, APP_NAVN)
    }
    return Pair(id, kafkaValueJson)
}

fun lagInaktiveringAvOppgave(id: String): Pair<String, String> {
    val inaktivering = VarselActionBuilder.inaktiver {
        varselId = id
        produsent = Produsent(Cluster.current.verdi, NAMESPACE, APP_NAVN)
    }
    return Pair(id, inaktivering)
}

private fun lagLink(avtaleId: String): String {
    return when (Cluster.current) {
        Cluster.PROD_GCP -> "https://arbeidsgiver.nav.no/tiltaksgjennomforing/avtale/${avtaleId}?part=DELTAKER"
        Cluster.DEV_GCP -> "https://tiltaksgjennomforing.ekstern.dev.nav.no/tiltaksgjennomforing/avtale/${avtaleId}?part=DELTAKER"
        Cluster.LOKAL -> {
            log.warn("Bruker localhost link for avtale: ${avtaleId}")
            "http://localhost:8080/tiltaksgjennomforing/avtale/${avtaleId}"
        }
    }
}

private fun lagTekst(varslingsformål: Varslingsformål, avtaleHendelseMelding: AvtaleHendelseMelding): String {
    return when (varslingsformål) {
        Varslingsformål.GODKJENNING_AV_AVTALE -> "Du må godkjenne en avtale om ${avtaleHendelseMelding.tiltakstype.beskrivelse} hos ${avtaleHendelseMelding.bedriftNavn} med oppstartsdato ${norskDatoFormat(avtaleHendelseMelding.startDato!!)}"
        Varslingsformål.GODKJENNING_AV_TAUSHETSERKLÆRING_MENTOR -> "Du har en taushetserklæring som venter på din godkjenning"
        Varslingsformål.AVTALE_FORLENGET -> "Sluttdatoen for ${avtaleHendelseMelding.tiltakstype.beskrivelse} hos ${avtaleHendelseMelding.bedriftNavn} er forlenget til ${norskDatoFormat(avtaleHendelseMelding.sluttDato!!)}"
        Varslingsformål.AVTALE_FORKORTET -> "Sluttdatoen for ${avtaleHendelseMelding.tiltakstype.beskrivelse} hos ${avtaleHendelseMelding.bedriftNavn} er forkortet til ${norskDatoFormat(avtaleHendelseMelding.sluttDato!!)}"
        Varslingsformål.AVTALE_ANNULLERT -> "Avtalen om ${avtaleHendelseMelding.tiltakstype.beskrivelse} hos ${avtaleHendelseMelding.bedriftNavn} ble avlyst"
        Varslingsformål.AVTALE_INNGÅTT -> "Din avtale om ${avtaleHendelseMelding.tiltakstype.beskrivelse} er inngått"
    }
}
