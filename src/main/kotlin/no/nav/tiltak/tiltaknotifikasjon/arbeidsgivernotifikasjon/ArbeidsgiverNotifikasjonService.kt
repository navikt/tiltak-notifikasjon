package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import kotlinx.coroutines.runBlocking
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.NySak
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.SaksStatus
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.inputs.AltinnMottakerInput
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.inputs.MottakerInput
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class ArbeidsgiverNotifikasjonService {

    suspend fun sendNotifikasjon() {
        // Send notifikasjon til arbeidsgiver
        val client = GraphQLWebClient("https://notifikasjon-fake-produsent-api.ekstern.dev.nav.no")
        val variabler = NySak.Variables(
            grupperingsid = "123",
            merkelapp = "Lonnstilskudd",
            virksomhetsnummer = "123456789",
            mottakere = listOf(MottakerInput(AltinnMottakerInput(serviceCode = "123", serviceEdition = "1"))),
            tittel = "Lønnstilskudd venter på din godkjenning",
            lenke = "https://arbeidsgiver.nav.no/tiltaksgjennomforing/avtale/123",
            initiellStatus = SaksStatus.MOTTATT,
            tidspunkt = Instant.now().toString()
        )
        val nySak = NySak(variabler)
        client.execute(nySak)

    }
}

fun main() {
    runBlocking {
        val arbeidsgiverNotifikasjonService = ArbeidsgiverNotifikasjonService()
        arbeidsgiverNotifikasjonService.sendNotifikasjon()
    }
}