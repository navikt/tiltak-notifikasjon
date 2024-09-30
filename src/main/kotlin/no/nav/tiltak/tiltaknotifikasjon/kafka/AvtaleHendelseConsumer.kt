package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import io.getunleash.Unleash
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgiverNotifikasjonService
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.Arbeidsgivernotifikasjon
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgivernotifikasjonRepository
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgivernotifikasjonStatus
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
import java.time.Instant

@Component
@Profile("prod-gcp", "dev-gcp", "dockercompose")
class AvtaleHendelseConsumer(
    val brukernotifikasjonService: BrukernotifikasjonService,
    val arbeidsgiverNotifikasjonService: ArbeidsgiverNotifikasjonService,
    val brukernotifikasjonRepository: BrukernotifikasjonRepository,
    val arbeidsgivernotifikasjonRepository: ArbeidsgivernotifikasjonRepository,
    val unleash: Unleash
) {
    private val mapper = jacksonMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [Topics.AVTALE_HENDELSE_COMPACT])
    fun nyAvtaleHendelse(avtaleHendelse: String) {
        behandleBrukernotifikasjon(avtaleHendelse)
        behandleArbeidsgivernotifikasjon(avtaleHendelse)
    }

    fun behandleBrukernotifikasjon(avtaleHendelse: String) {
        val togglePå = sjekkToggle("sms-min-side-deltaker")
        if (!togglePå) return

        try {
            val melding: AvtaleHendelseMelding = mapper.readValue(avtaleHendelse)
            brukernotifikasjonService.behandleAvtaleHendelseMelding(melding)

        } catch (e: Exception) {
            val brukernotifikasjon = Brukernotifikasjon(
                id = ulid(),
                avtaleMeldingJson = avtaleHendelse,
                status = BrukernotifikasjonStatus.FEILET_VED_BEHANDLING,
                opprettet = Instant.now(),
                feilmelding = e.message
            )
            brukernotifikasjonRepository.save(brukernotifikasjon)
            log.error("Error parsing AvtaleHendelseMelding: ${brukernotifikasjon.id}", e)
        }
    }
    fun behandleArbeidsgivernotifikasjon(avtaleHendelse: String) {
        val togglePå = sjekkToggle("arbeidsgivernotifikasjon-med-sak-og-sms")
        if (!togglePå) return

        try {
            val melding: AvtaleHendelseMelding = mapper.readValue(avtaleHendelse)
            arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(melding)
        } catch (e: Exception) {
            val arbeidsgivernotifikasjon = Arbeidsgivernotifikasjon(
                id = ulid(),
                avtaleMeldingJson = avtaleHendelse,
                status = ArbeidsgivernotifikasjonStatus.FEILET_VED_BEHANDLING,
                opprettetTidspunkt = Instant.now(),
                feilmelding = e.message
            )
            arbeidsgivernotifikasjonRepository.save(arbeidsgivernotifikasjon)
            log.error("Error parsing AvtaleHendelseMelding for arbeidsgivernotifikasjon: ${arbeidsgivernotifikasjon.id}", e)
        }
    }

    private fun sjekkToggle(toggle: String): Boolean {
        val erSkruddPå = unleash.isEnabled(toggle)
        if (!erSkruddPå) {
            log.info("Feature toggle $toggle er skrudd av. Prosesserer ikke melding")
            return false
        } else {
            log.info("Feature toggle $toggle er skrudd på. Prosesserer melding")
            return true
        }
    }

}