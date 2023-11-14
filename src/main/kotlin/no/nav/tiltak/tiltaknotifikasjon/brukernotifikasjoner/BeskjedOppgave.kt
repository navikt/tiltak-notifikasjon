package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.ulid
import no.nav.tiltak.tiltaknotifikasjon.utils.Cluster
import no.nav.tms.varsel.action.*
import no.nav.tms.varsel.builder.VarselActionBuilder
import java.time.ZoneId
import java.time.ZonedDateTime

class BeskjedOppgave {
    val NAMESPACE = "team-tiltak"
    val APP_NAVN= "tiltak-notifikasjon"

    public fun lagOppgave(fnr: String, avtaleId: String): String {
        val kafkaValueJson = VarselActionBuilder.opprett {
            type = Varseltype.Oppgave
            varselId = ulid()
            sensitivitet = Sensitivitet.High
            ident = fnr
            tekster += Tekst(
                spraakkode = "nb",
                tekst = "Det er noe du må gjøre i en avtale tiltak",
                default = true
            )
            link = "https://www.arbeidsgiver.nav.no/tiltaksgjennomforing/avtale/${avtaleId}"
            aktivFremTil = ZonedDateTime.now(ZoneId.of("Z")).plusDays(14)
            eksternVarsling = EksternVarslingBestilling(prefererteKanaler = listOf(EksternKanal.SMS))
            produsent = Produsent(Cluster.current.name.lowercase(), NAMESPACE, APP_NAVN)
        }
        return kafkaValueJson
    }
    public fun lagBeskjed(fnr: String, avtaleId: String): String {
        val kafkaValueJson = VarselActionBuilder.opprett {
            type = Varseltype.Beskjed
            varselId = ulid()
            sensitivitet = Sensitivitet.High
            ident = fnr
            tekster += Tekst(
                spraakkode = "nb",
                tekst = "Det er skjedd noe nytt i en avtale om tiltak",
                default = true
            )
            link = "https://www.arbeidsgiver.nav.no/tiltaksgjennomforing/avtale/${avtaleId}"
            aktivFremTil = ZonedDateTime.now(ZoneId.of("Z")).plusDays(14)
            eksternVarsling = EksternVarslingBestilling(prefererteKanaler = listOf(EksternKanal.SMS))
            produsent = Produsent(Cluster.current.name.lowercase(), NAMESPACE, APP_NAVN)
        }
        return kafkaValueJson
    }

}