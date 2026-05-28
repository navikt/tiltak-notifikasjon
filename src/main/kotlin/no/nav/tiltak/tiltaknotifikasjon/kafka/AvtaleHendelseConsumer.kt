package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import io.getunleash.Unleash
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgiverNotifikasjonService
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgiverRefusjonKontaktpersonRepository
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.Arbeidsgivernotifikasjon
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgivernotifikasjonRepository
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgivernotifikasjonStatus
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.RefusjonKontaktpersonEntitet
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.Avtalerolle
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.Brukernotifikasjon
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.BrukernotifikasjonRepository
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.BrukernotifikasjonService
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.BrukernotifikasjonStatus
import no.nav.tiltak.tiltaknotifikasjon.persondata.PersondataService
import no.nav.tiltak.tiltaknotifikasjon.utils.erOpphavArenaOgErKlarforvisning
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.slf4j.MDC
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
    val persondataService: PersondataService,
    val unleash: Unleash,
    private val arbeidsgiverRefusjonKontaktpersonRepository: ArbeidsgiverRefusjonKontaktpersonRepository
) {
    private val mapper = jacksonMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [Topics.AVTALE_HENDELSE_COMPACT])
    fun nyAvtaleHendelse(melding: ConsumerRecord<String, String>) {
        try {
            val startTidspunkt = System.currentTimeMillis()
            MDC.put("avtaleId", melding.key())
            MDC.put("kafkaOffset", melding.offset().toString())

            val avtaleHendelseJson = melding.value()
            val avtaleHendelsemelding: AvtaleHendelseMelding = mapper.readValue(avtaleHendelseJson)
            MDC.put("avtaleHendelseType", avtaleHendelsemelding.hendelseType.toString())
            MDC.put("avtaleStatus", avtaleHendelsemelding.avtaleStatus.toString())

            lagreRefusjonKontaktperson(avtaleHendelsemelding, melding.offset())
            behandleBrukernotifikasjon(avtaleHendelsemelding, avtaleHendelseJson)
            behandleArbeidsgivernotifikasjon(avtaleHendelsemelding, avtaleHendelseJson)
            val sluttTidspunkt = System.currentTimeMillis()
            log.atInfo()
                .addKeyValue("behandlingstidMs", sluttTidspunkt - startTidspunkt)
                .log("Behandlet kafkamelding ${melding.offset()} på ${sluttTidspunkt - startTidspunkt} ms");
        } catch (e: Exception) {
            log.error("Feil ved behandling/serialisering av avtalehendelsemelding. " +
                    "Skipper melding fra topic ${melding.topic()}, " +
                    "partition ${melding.partition()}, " +
                    "offset ${melding.offset()}",
                e)
            registrerFeiletMelding(melding.value(), e)
        }
        finally {
            MDC.remove("avtaleId")
            MDC.remove("kafkaOffset")
            MDC.remove("avtaleHendelseType")
            MDC.remove("avtaleStatus")
        }
    }

    fun behandleBrukernotifikasjon(avtaleHendelse: AvtaleHendelseMelding, avtaleHendelseJson: String) {
        try {
            if (!sjekkOmAvtaleFraArenaSkalBehandles(avtaleHendelse)) return
            brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelse)
        } catch (e: Exception) {
            val brukernotifikasjon = Brukernotifikasjon(
                id = ulid(),
                avtaleMeldingJson = avtaleHendelseJson,
                status = BrukernotifikasjonStatus.FEILET_VED_BEHANDLING,
                opprettet = Instant.now(),
                feilmelding = e.message
            )
            brukernotifikasjonRepository.save(brukernotifikasjon)
            log.error("Feil ved behandling AvtaleHendelseMelding for brukernotifikasjon: ${brukernotifikasjon.id}", e)
        }
    }

    fun behandleArbeidsgivernotifikasjon(avtaleHendelse: AvtaleHendelseMelding, avtaleHendelseJson: String) {
        try {
            if (!skalArbeidsgivernotifikasjonBehanldes(avtaleHendelse)) return
            arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelse)
        } catch (e: Exception) {
            val arbeidsgivernotifikasjon = Arbeidsgivernotifikasjon(
                id = ulid(),
                avtaleMeldingJson = avtaleHendelseJson,
                status = ArbeidsgivernotifikasjonStatus.FEILET_VED_BEHANDLING,
                opprettetTidspunkt = Instant.now(),
                feilmelding = e.message
            )
            arbeidsgivernotifikasjonRepository.save(arbeidsgivernotifikasjon)
            log.error("Feil ved behandling av AvtaleHendelseMelding for arbeidsgivernotifikasjon: ${arbeidsgivernotifikasjon.id}", e)
        }
    }

    fun lagreRefusjonKontaktperson(avtaleHendelseMelding: AvtaleHendelseMelding, offset: Long) {
        try {
            if (!unleash.isEnabled("refusjon-kontaktperson-backfill-ferdig")) return // Backfill håndteres av RefusjonKontaktpersonConsumer
            if (avtaleHendelseMelding.tiltakstype == Tiltakstype.ARBEIDSTRENING) return // arbeidstrening har ikke økonomi
            if (avtaleHendelseMelding.avtaleInngått == null) return  // refusjonsvarslinger har ingen nytte på ting som ikke er inngått
            if (avtaleHendelseMelding.arbeidsgiverTlf == null) return // skal ikke kunne skje på inngått avtale

            val refusjonKontaktperson = RefusjonKontaktpersonEntitet(
                avtaleId = avtaleHendelseMelding.avtaleId,
                refusjonKontaktpersonTlf = avtaleHendelseMelding.refusjonKontaktperson?.refusjonKontaktpersonTlf,
                arbeidsgiverOnskerOgsaVarsling = avtaleHendelseMelding.refusjonKontaktperson?.ønskerVarslingOmRefusjon,
                arbeidsgiverTlf = avtaleHendelseMelding.arbeidsgiverTlf!!,
                tiltakstype = avtaleHendelseMelding.tiltakstype,
                avtaleInnholdVersjon = avtaleHendelseMelding.versjon,
                avtaleHendelseType = avtaleHendelseMelding.hendelseType,
                avtaleHendelseSistEndret = avtaleHendelseMelding.sistEndret,
                topicOffset = offset,
                innlestTidspunkt = Instant.now(),
            )
            arbeidsgiverRefusjonKontaktpersonRepository.save(refusjonKontaktperson)
            log.info("Lagret refusjon kontaktperson for avtale ${avtaleHendelseMelding.avtaleId}, offset ${offset}")
        } catch (e: Exception) {
            log.error("Feil ved lagring av refusjon kontaktperson, offset $offset", e)
        }
    }

    private fun sjekkOmAvtaleFraArenaSkalBehandles(avtaleHendelse: AvtaleHendelseMelding): Boolean {
        return erOpphavArenaOgErKlarforvisning(avtaleHendelse, Avtalerolle.DELTAKER)
    }

    private fun skalArbeidsgivernotifikasjonBehanldes(avtaleHendelsemelding: AvtaleHendelseMelding): Boolean {
        // Arena-sjekk - ikke behandle meldinger på migrerte avtaler fra arena før de er tilgjengelige for arbeidsgiver.
        if (!erOpphavArenaOgErKlarforvisning(avtaleHendelsemelding, Avtalerolle.ARBEIDSGIVER)) return false
        // Diskresjonssjekk - Behandle kun kode 6/7 hvis det er statusendring eller annullert
        val erKode6Eller7 = persondataService.hentDiskresjonskode(avtaleHendelsemelding.deltakerFnr).erKode6Eller7()
        if (!erKode6Eller7) return true
        if (avtaleHendelsemelding.hendelseType == HendelseType.STATUSENDRING || avtaleHendelsemelding.hendelseType == HendelseType.ANNULLERT) return true
        return false
    }

    private fun registrerFeiletMelding(avtaleHendelseJson: String, exception: Exception) {
        // TODO: Mer sentralisert letterbox pattern her..
        try {
            val brukernotifikasjon = Brukernotifikasjon(
                id = ulid(),
                avtaleMeldingJson = avtaleHendelseJson,
                status = BrukernotifikasjonStatus.FEILET_VED_BEHANDLING,
                opprettet = Instant.now(),
                feilmelding = exception.message
            )
            brukernotifikasjonRepository.save(brukernotifikasjon)
        } catch (e: Exception) {
            log.error("Feil ved lagring av feilet brukernotifikasjon ved toppnivåfeil", e)
        }

        try {
            val arbeidsgivernotifikasjon = Arbeidsgivernotifikasjon(
                id = ulid(),
                avtaleMeldingJson = avtaleHendelseJson,
                status = ArbeidsgivernotifikasjonStatus.FEILET_VED_BEHANDLING,
                opprettetTidspunkt = Instant.now(),
                feilmelding = exception.message
            )
            arbeidsgivernotifikasjonRepository.save(arbeidsgivernotifikasjon)
        } catch (e: Exception) {
            log.error("Feil ved lagring av feilet arbeidsgivernotifikasjon ved toppnivåfeil", e)
        }
    }

}
