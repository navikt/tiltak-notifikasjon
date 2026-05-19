package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import io.getunleash.Unleash
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgiverRefusjonKontaktpersonRepository
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.RefusjonKontaktpersonEntitet
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

@Component
@Profile("prod-gcp", "dev-gcp", "dockercompose")
class RefusjonKontaktpersonConsumer(val refusjonKontaktpersonRepository: ArbeidsgiverRefusjonKontaktpersonRepository, val unleash: Unleash) {
    private val mapper = jacksonMapper()
    private val log = LoggerFactory.getLogger(javaClass)
    private val antallLagret = AtomicLong(0)

    @KafkaListener(topics = [Topics.AVTALE_HENDELSE_COMPACT],
        groupId = "tiltak-notifikasjon-refusjon-kontaktperson-1",
        properties = ["auto.offset.reset=earliest"], // Hmm, trodde ikke dette var nødvendig.. tydeligvis.
    )
    fun konsumer(melding: ConsumerRecord<String, String>) {
        try {
            if (!skalBehandles()) return
            val avtaleHendelseMelding: AvtaleHendelseMelding = mapper.readValue(melding.value())
            if (avtaleHendelseMelding.refusjonKontaktperson?.refusjonKontaktpersonTlf == null) return


            val refusjonKontaktperson = RefusjonKontaktpersonEntitet(
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
            val count = antallLagret.incrementAndGet()
            if (count % 100 == 0L) {
                log.info("Backfill refusjon kontaktperson: $count meldinger lagret, siste offset ${melding.offset()}")
            }
        } catch (e: Exception) {
            log.error(
                "Feil ved konsumering av refusjon kontaktperson. Skipper melding fra topic ${melding.topic()}, partition ${melding.partition()}, offset ${melding.offset()}",
                e,
            )
        }
    }

    private fun skalBehandles(): Boolean {
        // Kjører backfill frem til toggle skrus på, da tar AvtaleHendelseConsumer over
        return !unleash.isEnabled("refusjon-kontaktperson-backfill-ferdig")
    }
}
