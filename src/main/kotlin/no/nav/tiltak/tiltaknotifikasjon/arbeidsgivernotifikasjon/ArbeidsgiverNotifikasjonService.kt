package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import kotlinx.coroutines.runBlocking
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.NyBeskjed
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.NyOppgave
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.NySak
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.OppgaveUtfoert
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.SaksStatus
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.inputs.*
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class ArbeidsgiverNotifikasjonService {
    val client = GraphQLWebClient("https://notifikasjon-fake-produsent-api.ekstern.dev.nav.no")

    fun nySak(): NySak {
        val variabler = NySak.Variables(
            grupperingsid = "123",
            merkelapp = "Lonnstilskudd",
            virksomhetsnummer = "123456789",
            mottakere = listOf(MottakerInput(AltinnMottakerInput(serviceCode = "123", serviceEdition = "1"))),
            tittel = "Lønnstilskudd venter på din godkjenning",
            lenke = "https://arbeidsgiver.nav.no/tiltaksgjennomforing/avtale/123",
            initiellStatus = SaksStatus.MOTTATT,
            tidspunkt = Instant.now().toString(),
        )
        val nySak = NySak(variabler)
        return nySak
    }

    fun nyOppgave(): NyOppgave {
        val oppgaveVariables = NyOppgave.Variables(
            NyOppgaveInput(
                mottaker = MottakerInput(AltinnMottakerInput("1234", "1")),
                mottakere = listOf(),
                notifikasjon = NotifikasjonInput(
                    merkelapp = "Lonnstilskudd",
                    tekst = "Ny avtale til godkjenning!",
                    lenke = "nav.no"
                ),
                metadata = MetadataInput(
                    virksomhetsnummer = "123456789",
                    eksternId = "randomUUID",
                    opprettetTidspunkt = Instant.now().toString(),
                    grupperingsid = "123",
                    hardDelete = null
                ),
                eksterneVarsler = listOf(),
            )
        )
        val oppgave = NyOppgave(oppgaveVariables)
        return oppgave
    }

    fun oppgaveUtført(): OppgaveUtfoert {
        val oppgaveUtfoert = OppgaveUtfoert(OppgaveUtfoert.Variables(id = "123"))
        return oppgaveUtfoert
    }

    fun nyBeskjed(): NyBeskjed {
        val beskjedVariables = NyBeskjed.Variables(
            NyBeskjedInput(
                mottaker = MottakerInput(AltinnMottakerInput("1234", "1")),
                mottakere = listOf(),
                notifikasjon = NotifikasjonInput(
                    merkelapp = "Lonnstilskudd",
                    tekst = "Ny avtale til godkjenning!",
                    lenke = "nav.no"
                ),
                metadata = MetadataInput(
                    virksomhetsnummer = "123456789",
                    eksternId = "randomUUID",
                    opprettetTidspunkt = Instant.now().toString(),
                    grupperingsid = "123",
                    hardDelete = null
                ),
                eksterneVarsler = listOf(),
            )
        )
        val beskjed = NyBeskjed(beskjedVariables)
        return beskjed

    }
}


fun main() {
    runBlocking {
        val arbeidsgiverNotifikasjonService = ArbeidsgiverNotifikasjonService()
        val nySak = arbeidsgiverNotifikasjonService.nySak()
        val nyOppgave = arbeidsgiverNotifikasjonService.nyOppgave()
        println("Ny sak:")
        val sakResultat = arbeidsgiverNotifikasjonService.client.execute(nySak)
        println(sakResultat)
        println("Ny oppgave:")
        val oppgaveResulat = arbeidsgiverNotifikasjonService.client.execute(nyOppgave)
        println(oppgaveResulat)
    }
}