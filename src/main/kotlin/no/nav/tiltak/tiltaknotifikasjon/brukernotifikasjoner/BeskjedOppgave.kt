package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import no.nav.tiltak.tiltaknotifikasjon.utils.Cluster
import no.nav.tms.varsel.action.*
import no.nav.tms.varsel.builder.VarselActionBuilder
import java.time.ZoneId
import java.time.ZonedDateTime


val NAMESPACE = "team-tiltak"
val APP_NAVN = "tiltak-notifikasjon"

fun lagOppgave(fnr: String, avtaleId: String): Brukernotifikasjon {
    val id = ulid()
    val kafkaValueJson = VarselActionBuilder.opprett {
        type = Varseltype.Oppgave
        varselId = id
        sensitivitet = Sensitivitet.High
        ident = fnr
        tekster += Tekst(
            spraakkode = "nb",
            tekst = "Det er noe du må gjøre i en avtale om tiltak",
            default = true
        )
        link = lagLink(avtaleId)
        aktivFremTil = ZonedDateTime.now(ZoneId.of("Z")).plusDays(14)
        eksternVarsling = EksternVarslingBestilling(prefererteKanaler = listOf(EksternKanal.SMS))
        produsent = Produsent(Cluster.current.verdi, NAMESPACE, APP_NAVN)
    }
    return Brukernotifikasjon(kafkaValueJson, id, Varseltype.Oppgave)
}

fun lagBeskjed(fnr: String, avtaleId: String): Brukernotifikasjon {
    val id = ulid()
    val kafkaValueJson = VarselActionBuilder.opprett {
        type = Varseltype.Beskjed
        varselId = id
        sensitivitet = Sensitivitet.High
        ident = fnr
        tekster += Tekst(
            spraakkode = "nb",
            tekst = "Det er skjedd noe nytt i en avtale om tiltak",
            default = true
        )
        link = lagLink(avtaleId)
        aktivFremTil = ZonedDateTime.now(ZoneId.of("Z")).plusDays(14)
        eksternVarsling = EksternVarslingBestilling(prefererteKanaler = listOf(EksternKanal.SMS))
        produsent = Produsent(Cluster.current.verdi, NAMESPACE, APP_NAVN)
    }
    return Brukernotifikasjon(kafkaValueJson, id, Varseltype.Beskjed)
}

private fun lagLink(avtaleId: String): String {
    return when(Cluster.current) {
        Cluster.PROD_GCP -> "https://arbeidsgiver.nav.no/tiltaksgjennomforing/avtale/${avtaleId}"
        Cluster.DEV_GCP -> "https://tiltaksgjennomforing.ekstern.dev.nav.no/tiltaksgjennomforing/avtale/${avtaleId}"
        else -> "http://localhost:8080/tiltaksgjennomforing/avtale/${avtaleId}"
    }
}


