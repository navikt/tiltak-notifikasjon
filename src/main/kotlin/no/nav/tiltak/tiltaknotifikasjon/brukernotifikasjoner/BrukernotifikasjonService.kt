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

    fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding, brukernotifikasjon: Brukernotifikasjon) {
        when (avtaleHendelse.hendelseType) {
            HendelseType.ENDRET -> {
                // Oppgave om å godkjenne
                log.info("Endret melding, skal muligens til min side")
                if (avtaleHendelse.avtaleStatus == AvtaleStatus.MANGLER_GODKJENNING && avtaleHendelse.godkjentAvDeltaker == null) {
                    val harSendtOppgaveForEndretHendelse = sjekkOmSendtTilMinSidePåAvtaleHendlese(avtaleHendelse, HendelseType.ENDRET)
                    if (!harSendtOppgaveForEndretHendelse) {
                        val oppgaveIdOgJson = lagOppgave(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString())
                        val brukernotifikasjon = oppdaterBrukernotifikasjon(brukernotifikasjon, oppgaveIdOgJson, BrukernotifikasjonType.Oppgave, avtaleHendelse)
                        brukernotifikasjonRepository.save(brukernotifikasjon)
                        minSideProdusent.sendMeldingTilMinSide(brukernotifikasjon)
                        // lagre oppgave i db.
                    }
                }
            }

            HendelseType.DELTAKERS_GODKJENNING_OPPHEVET_AV_VEILEDER,
            HendelseType.DELTAKERS_GODKJENNING_OPPHEVET_AV_ARBEIDSGIVER -> {
                // Oppgave om å godkjenne (på nytt)
                val oppgaveJson = lagOppgave(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString())
                val brukernotifikasjon = oppdaterBrukernotifikasjon(brukernotifikasjon, oppgaveJson, BrukernotifikasjonType.Oppgave, avtaleHendelse)
                brukernotifikasjonRepository.save(brukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(brukernotifikasjon)
                // lagre beskjed i db.
            }

            HendelseType.GODKJENT_AV_DELTAKER,
            HendelseType.GODKJENT_PAA_VEGNE_AV,
            HendelseType.GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER-> {
                // Skal inaktivere oppgave om behov for godkjenning
                //Finn ID på beskjed om godkjenning
                val oppgaverPåAvtaleId = brukernotifikasjonRepository.findAllByAvtaleIdAndType(
                    avtaleHendelse.avtaleId.toString(),
                    BrukernotifikasjonType.Oppgave
                )
                oppgaverPåAvtaleId.filter { it.status !== BrukernotifikasjonStatus.INAKTIVERT && it.varselId !== null }.forEach {
                    it.status = BrukernotifikasjonStatus.INAKTIVERT
                    brukernotifikasjonRepository.save(it)
                    val inaktiveringMeldingIdOgJson = lagInaktiveringAvOppgave(it.varselId!!)

                    val oppdatertBrukernotifikasjon = oppdaterBrukernotifikasjon(brukernotifikasjon, inaktiveringMeldingIdOgJson, BrukernotifikasjonType.Inaktivering, avtaleHendelse)
                    brukernotifikasjonRepository.save(oppdatertBrukernotifikasjon)
                    minSideProdusent.sendMeldingTilMinSide(oppdatertBrukernotifikasjon)
                }
            }

            else -> {}
        }
    }



    fun sjekkOmSendtTilMinSidePåAvtaleHendlese(avtaleHendelse: AvtaleHendelseMelding, hendelseType: HendelseType): Boolean {
        brukernotifikasjonRepository.findAllbyAvtaleId(avtaleHendelse.avtaleId.toString()).filter { it.avtaleHendelseType === hendelseType }.forEach {
            if (it.sendt !== null || it.status == BrukernotifikasjonStatus.MOTTATT) {
                log.info("Fant allerede en brukernotifikasjon på hendelse $hendelseType for avtaleId: ${avtaleHendelse.avtaleId}")
                return true
            }
        }
        return false
    }


    fun oppdaterBrukernotifikasjon(brukernotifikasjon: Brukernotifikasjon, oppgaveIdOgJson: Pair<String, String>, type: BrukernotifikasjonType, avtaleHendelse: AvtaleHendelseMelding): Brukernotifikasjon =
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
        )

    fun ferdigstillBrukernotifikasjon(brukernotifikasjon: Brukernotifikasjon, oppgaveIdOgJson: Pair<String, String>): Brukernotifikasjon {
        brukernotifikasjon.status = BrukernotifikasjonStatus.BEHANDLET
        brukernotifikasjon.varselId = oppgaveIdOgJson.first
        brukernotifikasjon.minSideJson = oppgaveIdOgJson.second
        return brukernotifikasjon
    }

}