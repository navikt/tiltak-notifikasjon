package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleStatus
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import no.nav.tms.varsel.action.Varseltype
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.math.log

@Component
class BrukernotifikasjonService(val minSideProdusent: MinSideProdusent, val brukernotifikasjonRepository: BrukernotifikasjonRepository) {
    private val log = LoggerFactory.getLogger(javaClass)
    fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding, avtaleHendelseJson: String) {
        when (avtaleHendelse.hendelseType) {
            HendelseType.AVTALE_INNGÅTT -> {
                // Sjekk om det finnes en beskjed fra før på inngått
                // hvis ikke - opprett enitet og beskjed og send til min side. entiet status = mottatt
                val harSendtMeldingTilMinside = sjekkOmSendtTilMinSidePåAvtaleHendlese(avtaleHendelse, HendelseType.AVTALE_INNGÅTT)
                if (harSendtMeldingTilMinside) {
                    log.info("Fant allerede en brukernotifikasjon på avtale inngått for avtaleId: ${avtaleHendelse.avtaleId}")
                    return
                }

                log.info("Behandler avtalehendelsemelding - Avtale inngått. Skal lage og sende beskjed for avtaleId: ${avtaleHendelse.avtaleId}")
                val id = ulid()
                val beskjedJson = lagBeskjed(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString())
                val brukernotifikasjon = Brukernotifikasjon(
                    id = ulid(),
                    varselId = beskjedJson.first,
                    avtaleMeldingJson = avtaleHendelseJson,
                    minSideJson = beskjedJson.second,
                    type = Varseltype.Beskjed,
                    status = BrukernotifikasjonStatus.MOTTATT,
                    deltakerFnr = avtaleHendelse.deltakerFnr,
                    avtaleId = avtaleHendelse.avtaleId.toString(),
                    avtaleNr = avtaleHendelse.avtaleNr,
                    avtaleHendelseType = avtaleHendelse.hendelseType
                )
                minSideProdusent.sendMeldingTilMinSide(brukernotifikasjon)
                // oppdater entitet med status sendt_min_side

            }

            HendelseType.ENDRET -> {
                log.info("Endret melding, skal muligens til min side")
                if (avtaleHendelse.avtaleStatus == AvtaleStatus.MANGLER_GODKJENNING && !avtaleHendelse.godkjentAvDeltaker) {
                    val harSendtMeldingOmGodkjenning = sjekkOmSendtTilMinSidePåAvtaleHendlese(avtaleHendelse, HendelseType.ENDRET)

                    if (!harSendtMeldingOmGodkjenning) {
                        val oppgaveJson = lagOppgave(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString())
                        val brukernotifikasjon = Brukernotifikasjon(
                            id = ulid(),
                            varselId = oppgaveJson.first,
                            avtaleMeldingJson = avtaleHendelseJson,
                            minSideJson = oppgaveJson.second,
                            type = Varseltype.Oppgave,
                            status = BrukernotifikasjonStatus.MOTTATT,
                            deltakerFnr = avtaleHendelse.deltakerFnr,
                            avtaleId = avtaleHendelse.avtaleId.toString(),
                            avtaleNr = avtaleHendelse.avtaleNr,
                            avtaleHendelseType = avtaleHendelse.hendelseType
                        )
                        brukernotifikasjonRepository.save(brukernotifikasjon)
                        minSideProdusent.sendMeldingTilMinSide(brukernotifikasjon)
                        // lagre oppgave i db.
                    }
                }
            }

            HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER -> {
                // Skal kun ha oppgave om deltaker aldri hadde godkjent
                val oppgaveJson = lagOppgave(fnr = avtaleHendelse.deltakerFnr, avtaleId = avtaleHendelse.avtaleId.toString())
                val brukernotifikasjon = Brukernotifikasjon(
                    id = ulid(),
                    varselId = oppgaveJson.first,
                    avtaleMeldingJson = avtaleHendelseJson,
                    minSideJson = oppgaveJson.second,
                    type = Varseltype.Oppgave,
                    status = BrukernotifikasjonStatus.MOTTATT,
                    deltakerFnr = avtaleHendelse.deltakerFnr,
                    avtaleId = avtaleHendelse.avtaleId.toString(),
                    avtaleNr = avtaleHendelse.avtaleNr,
                    avtaleHendelseType = avtaleHendelse.hendelseType
                )
                brukernotifikasjonRepository.save(brukernotifikasjon)
                minSideProdusent.sendMeldingTilMinSide(brukernotifikasjon)
                // lagre beskjed i db.
            }

            else -> {}

        }
    }

    fun sjekkOmSendtTilMinSidePåAvtaleHendlese(avtaleHendelse: AvtaleHendelseMelding, hendelseType: HendelseType): Boolean {
        val hehe = brukernotifikasjonRepository.findAllbyAvtaleId(avtaleHendelse.avtaleId.toString())
        brukernotifikasjonRepository.findAllbyAvtaleId(avtaleHendelse.avtaleId.toString()).filter { it.avtaleHendelseType === hendelseType }.forEach {
            if (it.status === BrukernotifikasjonStatus.SENDT_TIL_MIN_SIDE || it.status == BrukernotifikasjonStatus.MOTTATT) {
                log.info("Fant allerede en brukernotifikasjon på hendelse $hendelseType for avtaleId: ${avtaleHendelse.avtaleId}")
                return true
            }
        }
        return false
    }

}