package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import io.getunleash.Unleash
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgiverRefusjonKontaktpersonRepository
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.RefusjonKontaktpersonEntitet
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
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
        groupId = "tiltak-notifikasjon-refusjon-kontaktperson-5",
        properties = ["auto.offset.reset=earliest"], // Hmm, trodde ikke dette var nødvendig.. tydeligvis.
    )
    fun konsumer(melding: ConsumerRecord<String, String>) {
        try {
            val avtaleHendelseMelding: AvtaleHendelseMelding = mapper.readValue(melding.value())
            if (!skalBehandles(avtaleHendelseMelding)) return

            val refusjonKontaktperson = RefusjonKontaktpersonEntitet(
                avtaleId = avtaleHendelseMelding.avtaleId,
                bedriftNr = avtaleHendelseMelding.bedriftNr,
                refusjonKontaktpersonTlf = avtaleHendelseMelding.refusjonKontaktperson?.refusjonKontaktpersonTlf,
                arbeidsgiverOnskerOgsaVarsling = avtaleHendelseMelding.refusjonKontaktperson?.ønskerVarslingOmRefusjon,
                arbeidsgiverTlf = avtaleHendelseMelding.arbeidsgiverTlf!!,
                tiltakstype = avtaleHendelseMelding.tiltakstype,
                avtaleInnholdVersjon = avtaleHendelseMelding.versjon,
                avtaleHendelseType = avtaleHendelseMelding.hendelseType,
                avtaleHendelseSistEndret = avtaleHendelseMelding.sistEndret,
                topicOffset = melding.offset(),
                innlestTidspunkt = Instant.now(),
                deltakerEtternavn = avtaleHendelseMelding.deltakerEtternavn,
                deltakerFornavn = avtaleHendelseMelding.deltakerFornavn,
            )
            refusjonKontaktpersonRepository.save(refusjonKontaktperson)
            log.info("Lagret refusjon kontaktperson via backfill, offset: ${melding.offset()}")
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

    private fun skalBehandles(avtaleHendelseMelding: AvtaleHendelseMelding): Boolean {
        if (avtaleHendelseMelding.tiltakstype == Tiltakstype.ARBEIDSTRENING) return false // arbeidstrening har ikke økonomi
        if (avtaleHendelseMelding.avtaleInngått == null) return false // refusjonsvarslinger har ingen nytte på ting som ikke er inngått
        if (avtaleHendelseMelding.arbeidsgiverTlf == null) return false // skal ikke kunne skje på inngått avtale
        // Kjører backfill frem til toggle skrus på, da tar AvtaleHendelseConsumer over
        return !unleash.isEnabled("refusjon-kontaktperson-backfill-ferdig")
    }
}
