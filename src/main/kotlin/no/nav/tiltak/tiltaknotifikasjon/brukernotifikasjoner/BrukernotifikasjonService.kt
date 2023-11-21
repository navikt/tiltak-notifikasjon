package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleStatus
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "dockercompose")
class BrukernotifikasjonService(val minSideProdusent: MinSideProdusent) {
    private val log = LoggerFactory.getLogger(javaClass)
    fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding) {
        when (avtaleHendelse.hendelseType) {
            HendelseType.AVTALE_INNGÅTT -> {
                // Sjekk om det finnes en beskjed fra før på inngått
                // hvis ikke - opprett enitet og beskjed og send til min side. entiet status = mottatt
                log.info("Behandler avtalehendelsemelding - Avtale inngått. Skal lage og sende beskjed for avtaleId: ${avtaleHendelse.avtaleId}")
                val beskjed = lagBeskjed(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString())
                minSideProdusent.sendMeldingTilMinSide(beskjed)
                // oppdater entitet med status sendt_min_side
            }
            HendelseType.ENDRET -> {
                if (avtaleHendelse.avtaleStatus == AvtaleStatus.MANGLER_GODKJENNING && !avtaleHendelse.godkjentAvDeltaker) {
                    val harSendtMeldingOmGodkjenning = false // Slå opp i DB
                    if (!harSendtMeldingOmGodkjenning) {
                        val oppgave = lagOppgave(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString())
                        minSideProdusent.sendMeldingTilMinSide(oppgave)
                        // lagre oppgave i db.
                    }
                }
            }
            HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER -> {
                // Skal kun ha oppgave om deltaker aldri hadde godkjent
                val oppgave = lagOppgave(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString())
                minSideProdusent.sendMeldingTilMinSide(oppgave)
                // lagre beskjed i db.
            }
            else -> {}

        }
    }

}