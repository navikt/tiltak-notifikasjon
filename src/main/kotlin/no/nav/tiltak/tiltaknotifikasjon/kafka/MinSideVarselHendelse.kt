package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.BrukernotifikasjonRepository
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class MinSideVarselHendelse(val brukernotifikasjonRepository: BrukernotifikasjonRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [Topics.BRUKERNOTIFIKASJON_HENDELSE])
    fun nyBrukernotifikasjonHendelse(brukernotifikasjonHendelse: String) {
        try {
            val melding: MinSideVarselHendelseKafkaMelding = jacksonMapper().readValue(brukernotifikasjonHendelse)
            if (melding.appnavn != "tiltak-notifikasjon") {
                return
            }

            val brukernotifikasjon = brukernotifikasjonRepository.findById(melding.varselId)
            if (brukernotifikasjon !== null) {
                //
                log.info("Mottok hendelse for brukernotifikasjon med id ${melding.varselId}")
            } else {
                log.error("Mottok hendelse for brukernoitifikasjon med id ${melding.varselId} som ikke finnes i DB")
            }

        } catch (e: Exception) {
            log.error("Error parsing MinSideVarselHendelseKafkaMelding", e)
        }

    }
}
