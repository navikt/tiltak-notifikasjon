package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleStatus
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BrukernotifikasjonService(val minSideProdusent: MinSideProdusent, val brukernotifikasjonRepository: BrukernotifikasjonRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding, avtaleHendelseJson: String) {
        when (avtaleHendelse.hendelseType) {
            HendelseType.ENDRET -> {
                // Oppgave om å godkjenne
                log.info("Endret melding, skal muligens til min side")
                if (avtaleHendelse.avtaleStatus == AvtaleStatus.MANGLER_GODKJENNING && avtaleHendelse.godkjentAvDeltaker == null) {
                    val harSendtOppgaveForEndretHendelse = sjekkOmSendtTilMinSidePåAvtaleHendlese(avtaleHendelse, HendelseType.ENDRET)

                    if (!harSendtOppgaveForEndretHendelse) {
                        val oppgaveJson = lagOppgave(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString())
                        val brukernotifikasjon = lagBrukernotifikasjon(oppgaveJson, avtaleHendelseJson, avtaleHendelse, BrukernotifikasjonType.Oppgave)
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
                val brukernotifikasjon = lagBrukernotifikasjon(oppgaveJson, avtaleHendelseJson, avtaleHendelse, BrukernotifikasjonType.Oppgave)
                brukernotifikasjonRepository.save(brukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(brukernotifikasjon)
                // lagre beskjed i db.
            }

            HendelseType.GODKJENT_AV_DELTAKER,
            HendelseType.GODKJENT_PAA_VEGNE_AV,
            HendelseType.GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER-> {
                // Skal inaktivere oppgave om behov for godkjenning
                //Finn ID på beskjed om godkjenning
                val oppgaverPåAvtaleId = brukernotifikasjonRepository.findAllByAvtaleIdAndType(avtaleHendelse.avtaleId.toString(), BrukernotifikasjonType.Oppgave)
                oppgaverPåAvtaleId.forEach {
                    val inaktiveringMeldingJson = lagInaktiveringAvOppgave(it.varselId)
                    val brukernotifikasjon = lagBrukernotifikasjon(inaktiveringMeldingJson, avtaleHendelseJson, avtaleHendelse, BrukernotifikasjonType.Inaktivering)
                    brukernotifikasjonRepository.save(brukernotifikasjon)
                    minSideProdusent.sendMeldingTilMinSide(brukernotifikasjon)
                }
            }
            else -> {}
        }
    }



    fun sjekkOmSendtTilMinSidePåAvtaleHendlese(avtaleHendelse: AvtaleHendelseMelding, hendelseType: HendelseType): Boolean {
        brukernotifikasjonRepository.findAllbyAvtaleId(avtaleHendelse.avtaleId.toString()).filter { it.avtaleHendelseType === hendelseType }.forEach {
            if (it.status === BrukernotifikasjonStatus.SENDT_TIL_MIN_SIDE || it.status == BrukernotifikasjonStatus.MOTTATT) {
                log.info("Fant allerede en brukernotifikasjon på hendelse $hendelseType for avtaleId: ${avtaleHendelse.avtaleId}")
                return true
            }
        }
        return false
    }

    fun lagBrukernotifikasjon(oppgaveJson: Pair<String, String>, avtaleHendelseJson: String, avtaleHendelse: AvtaleHendelseMelding, type: BrukernotifikasjonType): Brukernotifikasjon {
        val brukernotifikasjon = Brukernotifikasjon(
            id = ulid(),
            varselId = oppgaveJson.first,
            avtaleMeldingJson = avtaleHendelseJson,
            minSideJson = oppgaveJson.second,
            type = type,
            status = BrukernotifikasjonStatus.MOTTATT,
            deltakerFnr = avtaleHendelse.deltakerFnr,
            avtaleId = avtaleHendelse.avtaleId.toString(),
            avtaleNr = avtaleHendelse.avtaleNr,
            avtaleHendelseType = avtaleHendelse.hendelseType
        )
        return brukernotifikasjon
    }

}