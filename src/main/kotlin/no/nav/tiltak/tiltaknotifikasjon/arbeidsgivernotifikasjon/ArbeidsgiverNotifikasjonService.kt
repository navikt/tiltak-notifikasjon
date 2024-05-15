package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.MineNotifikasjoner
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient


@Profile("dev-gcp")
@Component
class ArbeidsgiverNotifikasjonService(arbeidsgivernotifikasjonProperties: ArbeidsgivernotifikasjonProperties, @Qualifier("azureWebClientBuilder") azureWebClientBuilder: WebClient.Builder) {
    private val log = LoggerFactory.getLogger(javaClass)

    val notifikasjonGraphQlClient = GraphQLWebClient(arbeidsgivernotifikasjonProperties.url, builder = azureWebClientBuilder)

    suspend fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding) {
        val client = GraphQLWebClient("https://notifikasjon-fake-produsent-api.ekstern.dev.nav.no")
        when (avtaleHendelse.hendelseType) {
            HendelseType.OPPRETTET -> {
                val nySak = nySak(avtaleHendelse)
                val response = client.execute(nySak)
            }

            HendelseType.GODKJENT_AV_ARBEIDSGIVER -> {
                val mineNotifikasjonerQuery =
                    mineNotifikasjoner(avtaleHendelse.tiltakstype.beskrivelse, avtaleHendelse.avtaleId.toString())
                val response = client.execute(mineNotifikasjonerQuery)
            }

            else -> {}
        }
        //  HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER
    }

     suspend fun hentMineSaker(avtaleId: String, merkelapp: String): GraphQLClientResponse<MineNotifikasjoner.Result> {
        val mineNotifikasjonerQuery =
            mineNotifikasjoner(merkelapp, avtaleId)
        log.info("laget request for mine saker på avtaleId $avtaleId og merkelapp $merkelapp")
        val response = notifikasjonGraphQlClient.execute(mineNotifikasjonerQuery)
        return response
    }

}