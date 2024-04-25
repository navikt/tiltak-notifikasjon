package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import kotlinx.coroutines.runBlocking
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.*
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.SaksStatus
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.Sendevindu
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.inputs.*
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class ArbeidsgiverNotifikasjonService {
    private val log = LoggerFactory.getLogger(javaClass)

    fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding) {
        when (avtaleHendelse.hendelseType) {
            HendelseType.AVTALE_INNGÅTT -> {

            }
          //  HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER
            else -> {}
        }
    }

}