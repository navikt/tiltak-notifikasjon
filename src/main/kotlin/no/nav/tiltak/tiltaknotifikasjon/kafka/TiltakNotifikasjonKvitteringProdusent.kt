package no.nav.tiltak.tiltaknotifikasjon.kafka

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TiltakNotifikasjonKvitteringProdusent(
    val notifikasjonKvitteringKafkaTemplate: KafkaTemplate<String, String>,
    val tiltakNotifikasjonKvitteringRepository: TiltakNotifikasjonKvitteringRepository
) {
    var log: Logger = LoggerFactory.getLogger(javaClass)

    val topic = Topics.NOTIFIKASJON_KVITTERING
    fun sendNotifikasjonKvittering(tiltakNotifikasjonKvitteringDto: TiltakNotifikasjonKvitteringDto) {

        notifikasjonKvitteringKafkaTemplate.send(topic, tiltakNotifikasjonKvitteringDto.id, tiltakNotifikasjonKvitteringDto.toJson())
            .whenComplete { it, ex ->
                if (ex != null) {
                    tiltakNotifikasjonKvitteringDto.feilmelding = ex.message
                    tiltakNotifikasjonKvitteringRepository.save(tiltakNotifikasjonKvitteringDto)
                    log.error("Melding med id ${tiltakNotifikasjonKvitteringDto.id} kunne ikke sendes til Kafka topic $topic", ex)
                } else {
                    tiltakNotifikasjonKvitteringDto.sendtTidspunkt = Instant.now()
                    tiltakNotifikasjonKvitteringRepository.save(tiltakNotifikasjonKvitteringDto)
                    log.info("Melding med id ${it.producerRecord.key()} sendt til Kafka topic $topic")
                }
            }
    }
}
