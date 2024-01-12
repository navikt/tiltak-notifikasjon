package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.Brukernotifikasjon
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.BrukernotifikasjonRepository
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.BrukernotifikasjonService
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.BrukernotifikasjonStatus
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "dockercompose")
class AvtaleHendelseConsumer(
    val brukernotifikasjonService: BrukernotifikasjonService,
    val brukernotifikasjonRepository: BrukernotifikasjonRepository
) {
    private val mapper = jacksonMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [Topics.AVTALE_HENDELSE_COMPACT])
    fun nyAvtaleHendelse(avtaleHendelse: String) {
        log.info("Mottok avtalehendelse: $avtaleHendelse")
        val brukernotifikasjon = Brukernotifikasjon(
            id = ulid(),
            avtaleMeldingJson = avtaleHendelse,
            status = BrukernotifikasjonStatus.MOTTATT,
        )
        brukernotifikasjonRepository.save(brukernotifikasjon)

        try {
            val melding: AvtaleHendelseMelding = mapper.readValue(avtaleHendelse)
            brukernotifikasjonService.behandleAvtaleHendelseMelding(melding, brukernotifikasjon)
        } catch (e: Exception) {
            brukernotifikasjon.status = BrukernotifikasjonStatus.FEILET_VED_PARSING
            brukernotifikasjon.feilmelding = e.message
            brukernotifikasjonRepository.save(brukernotifikasjon)
            log.error("Error parsing AvtaleHendelseMelding: ${brukernotifikasjon.id}", e)
        }

    }

}