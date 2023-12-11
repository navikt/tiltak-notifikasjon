package no.nav.tiltak.tiltaknotifikasjon.kafka

import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.Brukernotifikasjon
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.BrukernotifikasjonRepository
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.BrukernotifikasjonStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class MinSideProdusent(val minSideOppgaveKafkaTemplate: KafkaTemplate<String, String>, val brukernotifikasjonRepository: BrukernotifikasjonRepository) {
    var log: Logger = LoggerFactory.getLogger(javaClass)

    val topic = Topics.BRUKERNOTIFIKASJON_BRUKERVARSEL
    fun sendMeldingTilMinSide(brukernotifikasjon: Brukernotifikasjon) {
        minSideOppgaveKafkaTemplate.send(topic, brukernotifikasjon.id, brukernotifikasjon.minSideJson)
            .whenComplete { it, ex ->
                if (ex != null) {
                    brukernotifikasjon.status = BrukernotifikasjonStatus.FEILET
                    brukernotifikasjon.feilmelding = ex.message
                    brukernotifikasjonRepository.save(brukernotifikasjon)
                    log.error("Melding med id ${brukernotifikasjon.id} kunne ikke sendes til Kafka topic $topic", ex)
                } else {
                    brukernotifikasjon.sendt = true
                    brukernotifikasjonRepository.save(brukernotifikasjon)
                    log.info("Melding med id ${it.producerRecord.key()} sendt til Kafka topic $topic")
                }
            }
    }
}