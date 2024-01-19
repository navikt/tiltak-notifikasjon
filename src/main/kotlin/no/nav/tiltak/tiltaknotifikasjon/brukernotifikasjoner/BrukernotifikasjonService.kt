package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleStatus
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class BrukernotifikasjonService(val minSideProdusent: MinSideProdusent, val brukernotifikasjonRepository: BrukernotifikasjonRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    // OPPGAVER
    fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding, brukernotifikasjon: Brukernotifikasjon) {
        when (avtaleHendelse.hendelseType) {
            HendelseType.GODKJENT_AV_ARBEIDSGIVER -> {
                // Oppgave om å godkjenne
                log.info("Godkjent av arbeidsgiver, skal muligens varsle deltaker om godkjenning via min side")
                if (avtaleHendelse.godkjentAvDeltaker == null) {
                    val harSendtOppgaveForGodkjenning = sjekkOmDetFinnesAktiveOppgaverPåAvtaleMedFormål(avtaleHendelse, Varslingsformål.GODKJENNING_AV_AVTALE)
                    if (!harSendtOppgaveForGodkjenning) {
                        val oppgaveIdOgJson = lagOppgave(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString(), Varslingsformål.GODKJENNING_AV_AVTALE)
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
            HendelseType.GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER-> {
                // Skal inaktivere oppgave om behov for godkjenning
                //Finn ID på beskjed om godkjenning
                val oppgaverPåAvtaleId = brukernotifikasjonRepository.findAllByAvtaleIdAndType(avtaleHendelse.avtaleId.toString(), BrukernotifikasjonType.Oppgave)
                oppgaverPåAvtaleId.filter { it.status != BrukernotifikasjonStatus.INAKTIVERT && it.varselId != null && it.varslingsformål == Varslingsformål.GODKJENNING_AV_AVTALE }.forEach {
                    it.status = BrukernotifikasjonStatus.INAKTIVERT
                    brukernotifikasjonRepository.save(it)

                    val inaktiveringMeldingIdOgJson = lagInaktiveringAvOppgave(it.varselId!!)
                    val oppdatertBrukernotifikasjon = oppdaterBrukernotifikasjon(brukernotifikasjon, inaktiveringMeldingIdOgJson, BrukernotifikasjonType.Inaktivering, avtaleHendelse, null)
                    brukernotifikasjonRepository.save(oppdatertBrukernotifikasjon)
                    minSideProdusent.sendMeldingTilMinSide(oppdatertBrukernotifikasjon)
                }
            }

            // BESKJEDER
            HendelseType.AVTALE_FORLENGET -> {
                // Beskjed om at avtalen er blitt forlenget
                log.info("Avtale forlenget, skal varsle deltaker om forlengelse via min side")
                val beskjedIdOgJson = lagBeskjed(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString(), Varslingsformål.AVTALE_FORLENGET)
                val oppdatertBrukernotifikasjon = oppdaterBrukernotifikasjon(brukernotifikasjon, beskjedIdOgJson, BrukernotifikasjonType.Beskjed, avtaleHendelse, Varslingsformål.AVTALE_FORLENGET)
                brukernotifikasjonRepository.save(oppdatertBrukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(oppdatertBrukernotifikasjon)
            }
            HendelseType.AVTALE_FORKORTET -> {
                // Beskjed om at avtalen er blitt forkortet
                log.info("Avtale forkortet, skal varsle deltaker om forkortelse via min side")
                val beskjedIdOgJson = lagBeskjed(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString(), Varslingsformål.AVTALE_FORKORTET)
                val oppdatertBrukernotifikasjon = oppdaterBrukernotifikasjon(brukernotifikasjon, beskjedIdOgJson, BrukernotifikasjonType.Beskjed, avtaleHendelse, Varslingsformål.AVTALE_FORKORTET)
                brukernotifikasjonRepository.save(oppdatertBrukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(oppdatertBrukernotifikasjon)
            }

            else -> {}
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
            opprettet = Instant.now(),
            varslingsformål = varslingsformål
        )
}