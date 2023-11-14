package no.nav.tiltak.tiltaknotifikasjon.kafka

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class AvtaleHendelseConsumer {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [Topics.AVTALE_HENDELSE_COMPACT])
    fun nyAvtaleHendelse(avtaleHendelse: String) {
        log.info("Mottok avtalehendelse: $avtaleHendelse")
    }

}