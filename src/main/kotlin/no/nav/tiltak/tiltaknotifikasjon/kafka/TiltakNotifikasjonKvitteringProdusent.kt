package no.nav.tiltak.tiltaknotifikasjon.kafka

import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.Arbeidsgivernotifikasjon
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.Brukernotifikasjon
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

    fun sendNotifikasjonKvittering(arbeidsgivernotifikasjon: Arbeidsgivernotifikasjon) {
        val tiltakNotifikasjonKvitteringDto = kvitteringFra(arbeidsgivernotifikasjon)
        sendNotifikasjonKvittering(tiltakNotifikasjonKvitteringDto)
    }
    fun sendNotifikasjonKvittering(brukernotifikasjon: Brukernotifikasjon) {
        val tiltakNotifikasjonKvitteringDto = kvitteringFra(brukernotifikasjon)
        sendNotifikasjonKvittering(tiltakNotifikasjonKvitteringDto)
    }


    val topic = Topics.NOTIFIKASJON_KVITTERING
    private fun sendNotifikasjonKvittering(tiltakNotifikasjonKvitteringDto: TiltakNotifikasjonKvitteringDto) {

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
