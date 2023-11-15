package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "dockercompose")
class BrukernotifikasjonService(val minSideProdusent: MinSideProdusent) {
    private val log = LoggerFactory.getLogger(javaClass)
    fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding) {
        when (avtaleHendelse.hendelseType) {
            HendelseType.GODKJENT_AV_VEILEDER -> {
                log.info("Behandler avtalehendelsemelding - godkjent av veileder. Skal lage og sende beskjed for avtaleId: ${avtaleHendelse.avtaleId}")
                val beskjed = lagBeskjed(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString())
                minSideProdusent.sendBeskjedTilMinSide(beskjed)
            }
            else -> {}

        }
    }

}