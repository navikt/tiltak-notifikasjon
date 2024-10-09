package no.nav.tiltak.tiltaknotifikasjon.kafka

import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.Brukernotifikasjon
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.BrukernotifikasjonRepository
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.BrukernotifikasjonStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class MinSideProdusent(val minSideOppgaveKafkaTemplate: KafkaTemplate<String, String>, val brukernotifikasjonRepository: BrukernotifikasjonRepository, private val tiltakNotifikasjonKvitteringProdusent: TiltakNotifikasjonKvitteringProdusent) {
    var log: Logger = LoggerFactory.getLogger(javaClass)

    val topic = Topics.BRUKERNOTIFIKASJON_BRUKERVARSEL
    fun sendMeldingTilMinSide(brukernotifikasjon: Brukernotifikasjon) {
        minSideOppgaveKafkaTemplate.send(topic, brukernotifikasjon.id, brukernotifikasjon.minSideJson)
            .whenComplete { it, ex ->
                if (ex != null) {
                    brukernotifikasjon.status = BrukernotifikasjonStatus.FEILET_VED_SENDING
                    brukernotifikasjon.feilmelding = ex.message
                    brukernotifikasjonRepository.save(brukernotifikasjon)
                    log.error("Melding med id ${brukernotifikasjon.id} kunne ikke sendes til Kafka topic $topic", ex)
                } else {
                    brukernotifikasjon.sendt = Instant.now()
                    brukernotifikasjonRepository.save(brukernotifikasjon)
                    val kvittering = kvitteringFra(brukernotifikasjon)
                    tiltakNotifikasjonKvitteringProdusent.sendNotifikasjonKvittering(kvittering)
                    log.info("Melding med id ${it.producerRecord.key()} sendt til Kafka topic $topic")
                }
            }
    }
}
