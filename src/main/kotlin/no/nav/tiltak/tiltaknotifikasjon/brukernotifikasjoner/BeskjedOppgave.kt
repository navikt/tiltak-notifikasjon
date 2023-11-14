package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.ulid
import no.nav.tms.varsel.action.*
import no.nav.tms.varsel.builder.VarselActionBuilder
import java.time.ZoneId
import java.time.ZonedDateTime

class BeskjedOppgave {
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
        }
        return kafkaValueJson
    }

}