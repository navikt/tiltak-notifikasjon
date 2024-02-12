package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class BrukernotifikasjonService(val minSideProdusent: MinSideProdusent, val brukernotifikasjonRepository: BrukernotifikasjonRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding, brukernotifikasjon: Brukernotifikasjon) {
        when (avtaleHendelse.hendelseType) {
            // OPPGAVER
            HendelseType.GODKJENT_AV_ARBEIDSGIVER -> {
                // Oppgave om å godkjenne
                log.info("Godkjent av arbeidsgiver, skal muligens varsle deltaker om godkjenning via min side")
                if (avtaleHendelse.godkjentAvDeltaker == null) {
                    val harSendtOppgaveForGodkjenning = sjekkOmDetFinnesAktiveOppgaverPåAvtaleMedFormål(avtaleHendelse, Varslingsformål.GODKJENNING_AV_AVTALE)
                    if (!harSendtOppgaveForGodkjenning) {
                        val oppgaveIdOgJson = lagOppgave(avtaleHendelse, Varslingsformål.GODKJENNING_AV_AVTALE)
                        val oppdatertBrukernotifikasjon = oppdaterBrukernotifikasjon(brukernotifikasjon, oppgaveIdOgJson, BrukernotifikasjonType.Oppgave, avtaleHendelse, Varslingsformål.GODKJENNING_AV_AVTALE)
                        brukernotifikasjonRepository.save(oppdatertBrukernotifikasjon)
                        minSideProdusent.sendMeldingTilMinSide(oppdatertBrukernotifikasjon)
                        // lagre oppgave i db.
                    }
                }
            }

            // INAKTIVERING AV OPPGAVE
            HendelseType.GODKJENT_AV_DELTAKER,
            HendelseType.GODKJENT_PAA_VEGNE_AV,
            HendelseType.GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER -> {
                // Skal inaktivere oppgave om behov for godkjenning
                //Finn ID på beskjed om godkjenning
                inaktiverBrukernotifikasjon(avtaleHendelse, brukernotifikasjon)
            }

            // BESKJEDER
            HendelseType.AVTALE_FORLENGET -> {
                // Beskjed om at avtalen er blitt forlenget
                log.info("Avtale forlenget, skal varsle deltaker om forlengelse via min side")
                val beskjedIdOgJson = lagBeskjed(avtaleHendelse, Varslingsformål.AVTALE_FORLENGET)
                val oppdatertBrukernotifikasjon = oppdaterBrukernotifikasjon(brukernotifikasjon, beskjedIdOgJson, BrukernotifikasjonType.Beskjed, avtaleHendelse, Varslingsformål.AVTALE_FORLENGET)
                brukernotifikasjonRepository.save(oppdatertBrukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(oppdatertBrukernotifikasjon)
            }
            HendelseType.AVTALE_FORKORTET -> {
                // Beskjed om at avtalen er blitt forkortet
                log.info("Avtale forkortet, skal varsle deltaker om forkortelse via min side")
                val beskjedIdOgJson = lagBeskjed(avtaleHendelse, Varslingsformål.AVTALE_FORKORTET)
                val oppdatertBrukernotifikasjon = oppdaterBrukernotifikasjon(brukernotifikasjon, beskjedIdOgJson, BrukernotifikasjonType.Beskjed, avtaleHendelse, Varslingsformål.AVTALE_FORKORTET)
                brukernotifikasjonRepository.save(oppdatertBrukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(oppdatertBrukernotifikasjon)
            }
            HendelseType.AVTALE_INNGÅTT -> {
                // Beskjed om at avtalen er blitt inngått
                log.info("Avtale inngått, skal varsle deltaker om inngåelse via min side")
                val beskjedIdOgJson = lagBeskjed(avtaleHendelse, Varslingsformål.AVTALE_INNGÅTT)
                val oppdatertBrukernotifikasjon = oppdaterBrukernotifikasjon(brukernotifikasjon, beskjedIdOgJson, BrukernotifikasjonType.Beskjed, avtaleHendelse, Varslingsformål.AVTALE_INNGÅTT)
                brukernotifikasjonRepository.save(oppdatertBrukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(oppdatertBrukernotifikasjon)
            }
            HendelseType.ANNULLERT -> {
                // Beskjed om at avtalen er blitt annullert
                log.info("Avtale annullert, skal varsle deltaker om annullering via min side og inaktivere oppgave")
                val beskjedIdOgJson = lagBeskjed(avtaleHendelse, Varslingsformål.AVTALE_ANNULLERT)
                val oppdatertBrukernotifikasjon = oppdaterBrukernotifikasjon(brukernotifikasjon, beskjedIdOgJson, BrukernotifikasjonType.Beskjed, avtaleHendelse, Varslingsformål.AVTALE_ANNULLERT)
                brukernotifikasjonRepository.save(oppdatertBrukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(oppdatertBrukernotifikasjon)
                // Inaktivering av evt. oppgave om godkjenning
                inaktiverBrukernotifikasjon(avtaleHendelse, brukernotifikasjon)
            }

            else -> {}
        }
    }

    fun inaktiverBrukernotifikasjon(avtaleHendelse: AvtaleHendelseMelding, brukernotifikasjon: Brukernotifikasjon) {
        val oppgaverPåAvtaleId = brukernotifikasjonRepository.findAllByAvtaleIdAndType(avtaleHendelse.avtaleId.toString(), BrukernotifikasjonType.Oppgave)
        oppgaverPåAvtaleId.filter {it.status != BrukernotifikasjonStatus.INAKTIVERT && it.varselId != null && it.varslingsformål == Varslingsformål.GODKJENNING_AV_AVTALE }.forEach {
            it.status = BrukernotifikasjonStatus.INAKTIVERT
            brukernotifikasjonRepository.save(it)

            val inaktiveringMeldingIdOgJson = lagInaktiveringAvOppgave(it.varselId!!)
            val oppdatertBrukernotifikasjon = oppdaterBrukernotifikasjon(brukernotifikasjon, inaktiveringMeldingIdOgJson, BrukernotifikasjonType.Inaktivering, avtaleHendelse, null)
            brukernotifikasjonRepository.save(oppdatertBrukernotifikasjon)
            minSideProdusent.sendMeldingTilMinSide(oppdatertBrukernotifikasjon)
            log.info("Inaktiverer oppgave ${it.id} på avtaleId: ${avtaleHendelse.avtaleId} med formål: ${it.varslingsformål}")
        }
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

    fun oppdaterBrukernotifikasjon(brukernotifikasjon: Brukernotifikasjon, oppgaveIdOgJson: Pair<String, String>, type: BrukernotifikasjonType, avtaleHendelse: AvtaleHendelseMelding, varslingsformål: Varslingsformål?): Brukernotifikasjon =
        brukernotifikasjon.copy(
            varselId = oppgaveIdOgJson.first,
            minSideJson = oppgaveIdOgJson.second,
            type = type,
            status = BrukernotifikasjonStatus.BEHANDLET,
            deltakerFnr = avtaleHendelse.deltakerFnr,
            avtaleId = avtaleHendelse.avtaleId.toString(),
            avtaleNr = avtaleHendelse.avtaleNr,
            avtaleHendelseType = avtaleHendelse.hendelseType,
            varslingsformål = varslingsformål
        )
}