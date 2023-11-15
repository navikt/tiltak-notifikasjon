package no.nav.tiltak.tiltaknotifikasjon.kafka

import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.Brukernotifikasjon
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "dockercompose")
class MinSideProdusent(
    val minSideOppgaveKafkaTemplate: KafkaTemplate<String, String>
) {
    var log: Logger = LoggerFactory.getLogger(javaClass)

    fun sendOppgaveTilMinSide(brukernotifikasjon: Brukernotifikasjon) {
        minSideOppgaveKafkaTemplate.send(Topics.BRUKERNOTIFIKASJON_OPPGAVE, brukernotifikasjon.id, brukernotifikasjon.json)
            .whenComplete {it, ex ->
                if (ex != null) {
                    log.error(
                        "Melding med id {} kunne ikke sendes til Kafka topic {}",
                        brukernotifikasjon.id,
                        Topics.BRUKERNOTIFIKASJON_OPPGAVE,
                        ex
                    )
                } else {
                    log.info("Melding med id {} sendt til Kafka topic {}", it.producerRecord.key(), Topics.BRUKERNOTIFIKASJON_OPPGAVE)
                }
            }
    }
    fun sendBeskjedTilMinSide(brukernotifikasjon: Brukernotifikasjon) {
        minSideOppgaveKafkaTemplate.send(Topics.BRUKERNOTIFIKASJON_BESKJED, brukernotifikasjon.id, brukernotifikasjon.json)
            .whenComplete {it, ex ->
                if (ex != null) {
                    log.error(
                        "Melding med id {} kunne ikke sendes til Kafka topic {}",
                        brukernotifikasjon.id,
                        Topics.BRUKERNOTIFIKASJON_OPPGAVE,
                        ex
                    )
                } else {
                    log.info("Melding med id {} sendt til Kafka topic {}", it.producerRecord.key(), Topics.BRUKERNOTIFIKASJON_OPPGAVE)
                }
            }
    }
}