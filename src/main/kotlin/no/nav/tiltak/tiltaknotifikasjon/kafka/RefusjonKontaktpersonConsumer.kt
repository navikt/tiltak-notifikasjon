package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import io.getunleash.Unleash
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgiverRefusjonKontaktpersonRepository
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.RefusjonKontaktperson
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.Instant

@Component
@Profile("prod-gcp", "dev-gcp", "dockercompose")
class RefusjonKontaktpersonConsumer(val refusjonKontaktpersonRepository: ArbeidsgiverRefusjonKontaktpersonRepository, val unleash: Unleash) {
    private val mapper = jacksonMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [Topics.AVTALE_HENDELSE_COMPACT],
        groupId = "tiltak-notifikasjon-refusjon-kontaktperson-1",
        properties = ["auto.offset.reset=earliest"], // Hmm, trodde ikke dette var nødvendig.. tydeligvis.
    )
    fun konsumer(melding: ConsumerRecord<String, String>) {
        try {
            val avtaleHendelseMelding: AvtaleHendelseMelding = mapper.readValue(melding.value())

            if (!skalBehandles(avtaleHendelseMelding)) return
            if (avtaleHendelseMelding.refusjonKontaktperson?.refusjonKontaktpersonTlf == null) return


            val refusjonKontaktperson = RefusjonKontaktperson(
                avtaleId = avtaleHendelseMelding.avtaleId,
                refusjonKontaktpersonTlf = avtaleHendelseMelding.refusjonKontaktperson.refusjonKontaktpersonTlf,
                arbeidsgiverOnskerOgsaVarsling = avtaleHendelseMelding.refusjonKontaktperson.ønskerVarslingOmRefusjon,
                avtaleInnholdVersjon = avtaleHendelseMelding.versjon,
                avtaleHendelseType = avtaleHendelseMelding.hendelseType,
                avtaleHendelseSistEndret = avtaleHendelseMelding.sistEndret,
                topicOffset = melding.offset(),
                innlestTidspunkt = Instant.now(),
            )
            refusjonKontaktpersonRepository.save(refusjonKontaktperson)
            log.info("Lagret refusjon kontaktperson for avtale ${avtaleHendelseMelding.avtaleId}, offset ${melding.offset()}")
        } catch (e: Exception) {
            log.error("Feil ved konsumering av refusjon kontaktperson, offset ${melding.offset()}", e)
            throw e
        }
    }

    private fun skalBehandles(melding: AvtaleHendelseMelding): Boolean {
        return unleash.isEnabled("tiltak-notifikasjon-refusjon-kontaktperson")
    }
}
