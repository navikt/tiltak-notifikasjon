package no.nav.tiltak.tiltaknotifikasjon.kafka

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp, dockercompose")
class MinSideProdusent(
    val minSideOppgaveKafkaTemplate: KafkaTemplate<String, String>
) {
    var log: Logger = LoggerFactory.getLogger(javaClass)

    fun sendOppgaveTilMinSide(oppgave: String, id: String) {
        minSideOppgaveKafkaTemplate.send(Topics.BRUKERNOTIFIKASJON_OPPGAVE, id, oppgave)
            .whenComplete {it, ex ->
                if (ex != null) {
                    log.error(
                        "Melding med id {} kunne ikke sendes til Kafka topic {}",
                        id,
                        Topics.BRUKERNOTIFIKASJON_OPPGAVE,
                        ex
                    )
                } else {
                    log.info("Melding med id {} sendt til Kafka topic {}", it.producerRecord.key(), Topics.BRUKERNOTIFIKASJON_OPPGAVE)
                }
            }
    }
}