package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.APP_NAVN
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.BrukernotifikasjonRepository
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapperSomIkkeBryrSegOmEnumCase
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "prod-gcp", "dockercompose")
class MinSideVarselHendelseConsumer(
    val brukernotifikasjonRepository: BrukernotifikasjonRepository,
    private val tiltakNotifikasjonKvitteringProdusent: TiltakNotifikasjonKvitteringProdusent
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [Topics.BRUKERNOTIFIKASJON_HENDELSE])
    fun nyBrukernotifikasjonHendelse(brukernotifikasjonHendelse: String) {
        try {
            val melding: MinSideVarselHendelseKafkaMelding = jacksonMapperSomIkkeBryrSegOmEnumCase().readValue(brukernotifikasjonHendelse)
            if (melding.appnavn != APP_NAVN) {
                return
            }

            val brukernotifikasjon = brukernotifikasjonRepository.findNotifikasjonByVarselId(melding.varselId)
            if (brukernotifikasjon == null) {
                log.error("Mottok hendelse for brukernoitifikasjon med id ${melding.varselId} som ikke finnes i DB. Hendelse: $melding")
                return
            }
            log.info("Mottok hendelse for brukernotifikasjon som vi fant igjen i DB. Hendelse: $melding")
            if (melding.eventName == EventName.EKSTERNSTATUSOPPDATERT) {
                brukernotifikasjon.smsStatus = melding.status
                brukernotifikasjonRepository.save(brukernotifikasjon)
                if (melding.status == EksternStatusOppdatertStatus.SENDT) {
                    tiltakNotifikasjonKvitteringProdusent.sendNotifikasjonKvittering(brukernotifikasjon)
                }
            }
            if (melding.feilmelding !== null) {
                brukernotifikasjon.smsFeilmelding = melding.feilmelding
                brukernotifikasjonRepository.save(brukernotifikasjon)
            }

        } catch (e: Exception) {
            log.error("Error parsing MinSideVarselHendelseKafkaMelding", e)
        }
    }
}
