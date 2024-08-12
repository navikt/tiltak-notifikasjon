package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleStatus
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class BrukernotifikasjonService(val minSideProdusent: MinSideProdusent, val brukernotifikasjonRepository: BrukernotifikasjonRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding) {
        if (finnesDuplikatMelding(avtaleHendelse)) return

        when (avtaleHendelse.hendelseType) {
            // OPPGAVER
            HendelseType.GODKJENT_AV_ARBEIDSGIVER -> {
                // Oppgave om å godkjenne
                log.info("Godkjent av arbeidsgiver, skal muligens varsle deltaker om godkjenning via min side")
                if (avtaleHendelse.godkjentAvDeltaker == null) {
                    val harSendtOppgaveForGodkjenning = sjekkOmDetFinnesAktiveOppgaverPåAvtaleMedFormål(avtaleHendelse, Varslingsformål.GODKJENNING_AV_AVTALE)
                    if (!harSendtOppgaveForGodkjenning) {
                        val oppgaveIdOgJson = lagOppgave(avtaleHendelse, Varslingsformål.GODKJENNING_AV_AVTALE)
                        val brukernotifikasjon = nyBrukernotifikasjon(avtaleHendelse, BrukernotifikasjonType.Oppgave, Varslingsformål.GODKJENNING_AV_AVTALE, oppgaveIdOgJson)
                        brukernotifikasjonRepository.save(brukernotifikasjon)
                        minSideProdusent.sendMeldingTilMinSide(brukernotifikasjon)
                    }
                }
            }

            // INAKTIVERING AV OPPGAVE
            HendelseType.GODKJENT_AV_DELTAKER,
            HendelseType.GODKJENT_PAA_VEGNE_AV,
            HendelseType.GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER -> {
                // Skal inaktivere oppgave om behov for godkjenning
                //Finn ID på beskjed om godkjenning
                inaktiverEksisterendeOppgaverOmGodkjenning(avtaleHendelse)
            }

            // BESKJEDER
            HendelseType.AVTALE_FORLENGET -> {
                // Beskjed om at avtalen er blitt forlenget
                log.info("Avtale forlenget, skal varsle deltaker om forlengelse via min side")
                val beskjedIdOgJson = lagBeskjed(avtaleHendelse, Varslingsformål.AVTALE_FORLENGET)
                val brukernotifikasjon = nyBrukernotifikasjon(avtaleHendelse, BrukernotifikasjonType.Beskjed, Varslingsformål.AVTALE_FORLENGET, beskjedIdOgJson)
                brukernotifikasjonRepository.save(brukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(brukernotifikasjon)
            }
            HendelseType.AVTALE_FORKORTET -> {
                // Beskjed om at avtalen er blitt forkortet
                log.info("Avtale forkortet, skal varsle deltaker om forkortelse via min side")
                val beskjedIdOgJson = lagBeskjed(avtaleHendelse, Varslingsformål.AVTALE_FORKORTET)
                val brukernotifikasjon = nyBrukernotifikasjon(avtaleHendelse, BrukernotifikasjonType.Beskjed, Varslingsformål.AVTALE_FORKORTET, beskjedIdOgJson)
                brukernotifikasjonRepository.save(brukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(brukernotifikasjon)
            }
            HendelseType.AVTALE_INNGÅTT -> {
                // Beskjed om at avtalen er blitt inngått
                log.info("Avtale inngått, skal varsle deltaker om inngåelse via min side")
                val beskjedIdOgJson = lagBeskjed(avtaleHendelse, Varslingsformål.AVTALE_INNGÅTT)
                val brukernotifikasjon = nyBrukernotifikasjon(avtaleHendelse, BrukernotifikasjonType.Beskjed, Varslingsformål.AVTALE_INNGÅTT, beskjedIdOgJson)
                brukernotifikasjonRepository.save(brukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(brukernotifikasjon)
            }
            HendelseType.ANNULLERT -> {
                log.info("Avtale annullert, skal varsle deltaker om annullering via min side og inaktivere oppgave")
                // Inaktivering av evt. oppgave om godkjenning
                inaktiverEksisterendeOppgaverOmGodkjenning(avtaleHendelse)
                if (avtaleHendelse.feilregistrert) {
                    log.info("Avtale er feilregistrert, skal ikke varsle deltaker om annullering via min side")
                    return
                }
                // Beskjed om at avtalen er blitt annullert
                val beskjedIdOgJson = lagBeskjed(avtaleHendelse, Varslingsformål.AVTALE_ANNULLERT)
                val brukernotifikasjon = nyBrukernotifikasjon(avtaleHendelse, BrukernotifikasjonType.Beskjed, Varslingsformål.AVTALE_ANNULLERT, beskjedIdOgJson)
                brukernotifikasjonRepository.save(brukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(brukernotifikasjon)
            }

            else -> {}
        }
    }



    fun inaktiverEksisterendeOppgaverOmGodkjenning (avtaleHendelse: AvtaleHendelseMelding) {
        // Sette oppgave om godkjenning av oppgave til inaktivert i DB
        val oppgaverPåAvtaleMedFormålGodkjenning = brukernotifikasjonRepository.findAllByAvtaleIdAndType(avtaleHendelse.avtaleId.toString(), BrukernotifikasjonType.Oppgave)
            .filter {it.status != BrukernotifikasjonStatus.INAKTIVERT && it.varselId != null && it.varslingsformål == Varslingsformål.GODKJENNING_AV_AVTALE }
        if (oppgaverPåAvtaleMedFormålGodkjenning.size > 1) {
            log.error("Fant flere aktive oppgaver om godkjenning av avtale på avtaleId: ${avtaleHendelse.avtaleId}")
            throw RuntimeException("Fant flere aktive oppgaver om godkjenning av avtale på avtaleId: ${avtaleHendelse.avtaleId}")
        }
        val oppgaveSomSkalInaktiveres = oppgaverPåAvtaleMedFormålGodkjenning.firstOrNull()
        if (oppgaveSomSkalInaktiveres == null) {
            // Dette kan skje dersom f.eks. en avtale annulleres uten at arbeidsgiver har godkjent, da har vi ikke sendt noen oppgaver om godkjenning
            // Eller ved eldre avtaler som ikke er sendt ut oppgaver om godkjenning
            log.info("Fant ingen aktive oppgaver om godkjenning av avtale på avtaleId: ${avtaleHendelse.avtaleId}")
            return
        }
        oppgaveSomSkalInaktiveres.status = BrukernotifikasjonStatus.INAKTIVERT
        brukernotifikasjonRepository.save(oppgaveSomSkalInaktiveres)

        // Sende inaktiveringsmelding på avtalen på oppgave om godkjenning til min-side
        val inaktiveringMeldingIdOgJson = lagInaktiveringAvOppgave(oppgaveSomSkalInaktiveres.varselId!!)
        val inaktiveringsBrukernotifikasjon = nyBrukernotifikasjon(avtaleHendelse, BrukernotifikasjonType.Inaktivering, null, inaktiveringMeldingIdOgJson)

        brukernotifikasjonRepository.save(inaktiveringsBrukernotifikasjon)
        minSideProdusent.sendMeldingTilMinSide(inaktiveringsBrukernotifikasjon)

        log.info("Inaktiverer oppgave ${oppgaveSomSkalInaktiveres.id} på avtaleId: ${avtaleHendelse.avtaleId} med formål: ${oppgaveSomSkalInaktiveres.varslingsformål}")
    }

    fun sjekkOmDetFinnesAktiveOppgaverPåAvtaleMedFormål(avtaleHendelse: AvtaleHendelseMelding, varslingsformål: Varslingsformål): Boolean {
        brukernotifikasjonRepository.findAllByAvtaleIdAndType(avtaleHendelse.avtaleId.toString(), BrukernotifikasjonType.Oppgave).filter { it.varslingsformål == varslingsformål }.forEach {
            if (it.status != BrukernotifikasjonStatus.INAKTIVERT) {
                // Finnes en oppgave på avtalen som ikke er inaktivert
                log.info("Fant allerede en aktiv brukernotifikasjon oppgave ${it.id} på avtaleId: ${avtaleHendelse.avtaleId} med formål: $varslingsformål")
                return true
            }
        }
        return false
    }
    fun finnesDuplikatMelding(avtaleHendelse: AvtaleHendelseMelding): Boolean {
        // Sjekker om det finnes behandlede avtaleHendelser i basen som har likt endret tildspunt som den som kommer inn. Bør ikke skje.
        brukernotifikasjonRepository.findAllbyAvtaleId(avtaleHendelse.avtaleId.toString()).forEach {
            if (it.status != BrukernotifikasjonStatus.INAKTIVERT) {
                val melding: AvtaleHendelseMelding = jacksonMapper().readValue(it.avtaleMeldingJson)
                if (melding.sistEndret == avtaleHendelse.sistEndret && melding.hendelseType == avtaleHendelse.hendelseType) {
                    log.warn("Fant en brukernotifikasjon med samme hendelsetype og sistEndret tidspunkt som allerede er behandlet, avtaleId: ${avtaleHendelse.avtaleId}")
                    return true
                }
            }
        }
        return false
    }

    fun nyBrukernotifikasjon(avtaleHendelse: AvtaleHendelseMelding, type: BrukernotifikasjonType, varslingsformål: Varslingsformål?, oppgaveIdOgJson: Pair<String, String>): Brukernotifikasjon =
        Brukernotifikasjon(
            id = ulid(),
            avtaleMeldingJson = jacksonMapper().writeValueAsString(avtaleHendelse),
            status = BrukernotifikasjonStatus.BEHANDLET,
            opprettet = Instant.now(),
            type = type,
            deltakerFnr = avtaleHendelse.deltakerFnr,
            avtaleId = avtaleHendelse.avtaleId.toString(),
            avtaleNr = avtaleHendelse.avtaleNr,
            avtaleHendelseType = avtaleHendelse.hendelseType,
            varslingsformål = varslingsformål,
            varselId = oppgaveIdOgJson.first,
            minSideJson = oppgaveIdOgJson.second
        )
}

private fun Brukernotifikasjon.medNyId(): Brukernotifikasjon = this.copy(id = ulid())
