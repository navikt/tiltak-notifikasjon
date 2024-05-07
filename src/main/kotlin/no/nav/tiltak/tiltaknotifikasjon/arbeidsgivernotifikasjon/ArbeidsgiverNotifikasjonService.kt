package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import kotlinx.coroutines.runBlocking
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.*
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.SaksStatus
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.Sendevindu
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.inputs.*
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.MineNotifikasjonerResultat
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.Notifikasjon
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class ArbeidsgiverNotifikasjonService {
    private val log = LoggerFactory.getLogger(javaClass)

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
        val response = client.execute(mineNotifikasjonerQuery)
        return response
    }


}