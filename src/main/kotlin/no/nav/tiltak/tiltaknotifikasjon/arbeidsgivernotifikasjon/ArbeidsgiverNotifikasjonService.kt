package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.*
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.OppgaveTilstand
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.Beskjed
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.MineNotifikasjonerResultat
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.NotifikasjonConnection
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.Oppgave
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nybeskjed.NyBeskjedVellykket
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nyoppgave.NyOppgaveVellykket
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nysak.NySakVellykket
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nystatussak.NyStatusSakVellykket
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.softdeletenotifikasjon.SoftDeleteNotifikasjonVellykket
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.softdeletesakbygrupperingsid.SakFinnesIkke
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.softdeletesakbygrupperingsid.SoftDeleteSakVellykket
import no.nav.tiltak.tiltaknotifikasjon.avtale.*
import no.nav.tiltak.tiltaknotifikasjon.kafka.TiltakNotifikasjonKvitteringProdusent
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import no.nav.tiltak.tiltaknotifikasjon.utils.erOpphavArenaOgErKlarforvisning
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant
import java.time.LocalDateTime

@Component
class ArbeidsgiverNotifikasjonService(
    arbeidsgivernotifikasjonProperties: ArbeidsgivernotifikasjonProperties,
    private val altinnProperties: AltinnProperties,
    @Qualifier("azureWebClientBuilder") azureWebClientBuilder: WebClient.Builder,
    val arbeidsgivernotifikasjonRepository: ArbeidsgivernotifikasjonRepository,
    private val tiltakNotifikasjonKvitteringProdusent: TiltakNotifikasjonKvitteringProdusent,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    val notifikasjonGraphQlClient = GraphQLWebClient(arbeidsgivernotifikasjonProperties.url, builder = azureWebClientBuilder)

    fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding) {
        if (finnesDuplikatMelding(avtaleHendelse)) return

        runBlocking {
            when (avtaleHendelse.hendelseType) {

                HendelseType.OPPRETTET -> {
                    log.info("AG: Avtale opprettet: lager sak og oppgave. avtaleId: ${avtaleHendelse.avtaleId}")
                    // Sak
                    val nySak = nySak(avtaleHendelse, altinnProperties)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Sak, Varslingsformål.GODKJENNING_AV_AVTALE, nySak)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                    opprettNySak(nySak, notifikasjon)
                    //Oppgave (på saken - via grupperingsId)
                    val nyOppgave = nyOppgave(avtaleHendelse, altinnProperties)
                    val notifikasjonOppgave = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Oppgave, Varslingsformål.GODKJENNING_AV_AVTALE, nyOppgave)
                    arbeidsgivernotifikasjonRepository.save(notifikasjonOppgave)
                    opprettNyOppgave(nyOppgave, notifikasjonOppgave)
                }

                HendelseType.ENDRET -> {
                    if (erOpphavArenaOgErKlarforvisning(avtaleHendelse, Avtalerolle.ARBEIDSGIVER)) {
                        // Vi skal gjøre det samme som ved opprettelse her, men kun 1 gang, og kun hvis den er klar for visning til eksterne
                        log.info("AG: Avtale endret fra Arena: sjekker om sak finnes, hvis ikke lager sak og oppgave. avtaleId: ${avtaleHendelse.avtaleId}")
                        val eksisterendeSak = arbeidsgivernotifikasjonRepository.findSakByAvtaleId(avtaleHendelse.avtaleId.toString())
                        if (eksisterendeSak == null) {
                            // Sak
                            val nySak = nySak(avtaleHendelse, altinnProperties)
                            val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Sak, Varslingsformål.GODKJENNING_AV_AVTALE, nySak)
                            arbeidsgivernotifikasjonRepository.save(notifikasjon)
                            opprettNySak(nySak, notifikasjon)
                            //Oppgave (på saken - via grupperingsId)
                            val nyOppgave = nyOppgave(avtaleHendelse, altinnProperties)
                            val notifikasjonOppgave = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Oppgave, Varslingsformål.GODKJENNING_AV_AVTALE, nyOppgave)
                            arbeidsgivernotifikasjonRepository.save(notifikasjonOppgave)
                            opprettNyOppgave(nyOppgave, notifikasjonOppgave)
                        } else {
                            log.info("AG: Avtale med opphav ARENA endret: sak finnes allerede, gjør ingenting. avtaleId: ${avtaleHendelse.avtaleId}")
                        }
                    }
                }

                HendelseType.GODKJENT_AV_ARBEIDSGIVER,
                HendelseType.GODKJENT_PAA_VEGNE_AV_ARBEIDSGIVER,
                HendelseType.GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER -> {
                    log.info("AG: Avtale godkjent: lukker oppgaver. avtaleId: ${avtaleHendelse.avtaleId}")
                    // Lukk alle oppgaver
                    val mineNotifikasjonerQuery = mineNotifikasjoner(tiltakstype = avtaleHendelse.tiltakstype, grupperingsid = avtaleHendelse.grupperingsId())
                    val response = notifikasjonGraphQlClient.execute(mineNotifikasjonerQuery)
                    val notifikasjoner = response.data?.mineNotifikasjoner
                    lukkÅpneOppgaverPåAvtale(notifikasjoner, avtaleHendelse)
                }

                HendelseType.ARBEIDSGIVERS_GODKJENNING_OPPHEVET_AV_VEILEDER -> {
                    log.info("AG: Avtale godkjenning opphevet: lager ny oppgave. avtaleId: ${avtaleHendelse.avtaleId}")
                    //Oppgave (på saken - via grupperingsId)
                    val nyOppgave = nyOppgave(avtaleHendelse, altinnProperties)
                    val notifikasjonOppgave = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Oppgave, Varslingsformål.GODKJENNING_AV_AVTALE, nyOppgave)
                    arbeidsgivernotifikasjonRepository.save(notifikasjonOppgave)
                    opprettNyOppgave(nyOppgave, notifikasjonOppgave)
                }

                HendelseType.ANNULLERT -> {
                    log.info("AG: Avtale annullert: sletter saker, oppgaver og beskjeder. avtaleId: ${avtaleHendelse.avtaleId}")
                    // Fager har "cascade" på softDeleteSak, den tar da med seg oppgaver og beskjeder også.
                    // men det kan finnes oppgaver/beskjeder på avtalen uten at det er en sak der (fra gammelt oppsett) må uansett slette de.
                    if (avtaleHendelse.feilregistrert) {
                        // Ved annullering av avtale med årsak feilregistrert, skjules avtalen for alle. Dermed fjerner vi notifikasjoner også.
                        log.info("AG: Avtale annullert med årsak feilregistrering. Forsøker å slette sak. avtaleId: ${avtaleHendelse.avtaleId}")
                        val softDeleteSakQuery = nySoftDeleteSakQuery(avtaleHendelse.tiltakstype.arbeidsgiverNotifikasjonMerkelapp, avtaleHendelse.grupperingsId())
                        val notifikasjonSakSletting = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.SoftDeleteSak, Varslingsformål.AVTALE_ANNULLERT, softDeleteSakQuery)
                        val gikkSoftDeleteSakBra = softDeleteSak(softDeleteSakQuery, avtaleHendelse.avtaleId.toString(), notifikasjonSakSletting)
                        if (!gikkSoftDeleteSakBra) {
                            log.warn("AG: Soft delete av sak gikk ikke/fant ikke sak, må slette notifikasjoner manuelt. avtaleId: ${avtaleHendelse.avtaleId}")
                            val mineNotifikasjonerQuery = mineNotifikasjoner(
                                tiltakstype = avtaleHendelse.tiltakstype,
                                grupperingsid = avtaleHendelse.grupperingsId()
                            )
                            val response = notifikasjonGraphQlClient.execute(mineNotifikasjonerQuery)
                            val notifikasjoner = response.data?.mineNotifikasjoner
                            softDeleteOppgaverOgBeskjeder(notifikasjoner, avtaleHendelse)
                        }
                    } else {
                        // Ikke feilregistrert.
                        // Hvis vi har sak: sletter kun oppgaver. kaller nyStatusSak med hardDelete = now().pus12weeks. Lagrer de som slettet i basen.
                        // Hvis vi ikke har sak: sletter både oppgaver og beskjeder.
                        val saken = arbeidsgivernotifikasjonRepository.findSakByAvtaleId(avtaleHendelse.avtaleId.toString())
                        if (saken != null) {
                            log.info("AG: Annullert avtale har sak. sletter oppgaver og setter hardDelete på sak. avtaleId: ${avtaleHendelse.avtaleId}")
                            // slett oppgaver og sett hardDelete til 12 uker på sak.
                            val mineNotifikasjonerQuery = mineNotifikasjoner(
                                tiltakstype = avtaleHendelse.tiltakstype,
                                grupperingsid = avtaleHendelse.grupperingsId()
                            )
                            val response = notifikasjonGraphQlClient.execute(mineNotifikasjonerQuery)
                            val notifikasjoner = response.data?.mineNotifikasjoner
                            if (notifikasjoner is NotifikasjonConnection) {
                                val oppgaver = notifikasjoner.edges.map { it.node }.filterIsInstance<Oppgave>()
                                // Slett alle oppgaver
                                oppgaver.forEach { oppgave -> softDeleteNotifikasjon(oppgave.metadata.id, avtaleHendelse) }
                            }
                            // Annuller Sak
                            val nySakStatusAnnullertQuery = nySakStatusAnnullertQuery(saken.responseId!!)
                            val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.NySakStatus, Varslingsformål.AVTALE_ANNULLERT, nySakStatusAnnullertQuery)
                            nySakStatus(nySakStatusAnnullertQuery, notifikasjon, saken, ArbeidsgivernotifikasjonStatus.SAK_ANNULLERT)
                        } else {
                            // Slett alle oppgaver og beskjeder
                            log.info("AG: Annullert avtale har ingen sak. sletter oppgaver og beskjeder. avtaleId: ${avtaleHendelse.avtaleId}")
                            val mineNotifikasjonerQuery = mineNotifikasjoner(
                                tiltakstype = avtaleHendelse.tiltakstype,
                                grupperingsid = avtaleHendelse.grupperingsId()
                            )
                            val response = notifikasjonGraphQlClient.execute(mineNotifikasjonerQuery)
                            val notifikasjoner = response.data?.mineNotifikasjoner
                            softDeleteOppgaverOgBeskjeder(notifikasjoner, avtaleHendelse)
                        }

                        if (avtaleHendelse.opphav != AvtaleOpphav.ARENA || erOpphavArenaOgErKlarforvisning(avtaleHendelse, Avtalerolle.ARBEIDSGIVER)) {
                            // Vi sender ikke beskjed om annullering på opphav Arena avtaler. OBS: Kun de som ikke er inngått vil være utilgjengelige for arbeidsgiver.
                            // Send beskjed om annullering (ikke feilregistrert)
                            log.info("AG: Avtale annullert. lager beskjed om annullering. avtaleId: ${avtaleHendelse.avtaleId}")
                            val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                            val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.AVTALE_ANNULLERT, nyBeskjed)
                            arbeidsgivernotifikasjonRepository.save(notifikasjon)
                            opprettNyBeskjed(nyBeskjed, notifikasjon)
                        }

                    }
                }

                HendelseType.STATUSENDRING -> {
                    // Statusendringer som oppstår som følge av at av dager går.
                    log.info("AG: Statusendring: sjekker om avtale er endret til avsluttet. avtaleId: ${avtaleHendelse.avtaleId}")
                    settSakTilFerdigHvisAvtalestatusAvsluttet(avtaleHendelse)
                }

                // BESKJEDER
                HendelseType.AVTALE_INNGÅTT -> {
                    log.info("AG: Avtale inngått: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.AVTALE_INNGÅTT, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                    opprettNyBeskjed(nyBeskjed, notifikasjon)
                }
                HendelseType.AVTALE_FORLENGET -> {
                    log.info("AG: Avtale forlenget: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjonBeskjed = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.AVTALE_FORLENGET, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjonBeskjed)
                    opprettNyBeskjed(nyBeskjed, notifikasjonBeskjed)
                    // Endre status på sak tilbake til mottatt hvis den var avsluttet
                    val saken = arbeidsgivernotifikasjonRepository.findSakByAvtaleId(avtaleHendelse.avtaleId.toString())
                    if (saken?.status == ArbeidsgivernotifikasjonStatus.SAK_FERDIG && avtaleHendelse.avtaleStatus == AvtaleStatus.GJENNOMFØRES) {
                        log.info("AG: Avtale er forlenget. Sak/Avtale var avsluttet. Setter sak til mottatt igjen (gjennomføres). avtaleId: ${avtaleHendelse.avtaleId}")
                        val nySakStatusMottattQuery = nySakStatusMottattQuery(saken.responseId!!, avtaleHendelse)
                        val notifikasjonNySakStatus = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.NySakStatus, Varslingsformål.INGEN_VARSLING, nySakStatusMottattQuery)
                        nySakStatus(nySakStatusMottattQuery, notifikasjonNySakStatus, saken, ArbeidsgivernotifikasjonStatus.SAK_MOTTATT)
                    }
                }
                HendelseType.AVTALE_FORLENGET_AV_ARENA -> {
                    // Endre status på sak tilbake til mottatt hvis den var avsluttet eller annullert
                    val saken = arbeidsgivernotifikasjonRepository.findSakByAvtaleId(avtaleHendelse.avtaleId.toString())
                    if ((saken?.status == ArbeidsgivernotifikasjonStatus.SAK_FERDIG ||
                                saken?.status == ArbeidsgivernotifikasjonStatus.SAK_ANNULLERT)
                        && avtaleHendelse.avtaleStatus == AvtaleStatus.GJENNOMFØRES) {
                        log.info("AG: Avtale er forlenget av arena. Sak/Avtale var avsluttet eller annullert. Setter sak til mottatt igjen (gjennomføres). avtaleId: ${avtaleHendelse.avtaleId}")
                        val nySakStatusMottattQuery = nySakStatusMottattQuery(saken.responseId!!, avtaleHendelse)
                        val notifikasjonNySakStatus = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.NySakStatus, Varslingsformål.INGEN_VARSLING, nySakStatusMottattQuery)
                        nySakStatus(nySakStatusMottattQuery, notifikasjonNySakStatus, saken, ArbeidsgivernotifikasjonStatus.SAK_MOTTATT)
                    }
                }



                HendelseType.AVTALE_FORKORTET_AV_ARENA -> {
                    log.info("AG: Avtale forkortet av Arena: Setter sak til ferdig hvis avsluttet. avtaleId: ${avtaleHendelse.avtaleId}")
                    settSakTilFerdigHvisAvtalestatusAvsluttet(avtaleHendelse)
                }
                HendelseType.AVTALE_FORKORTET -> {
                    log.info("AG: Avtale forkortet: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjonNyBeskjed = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.AVTALE_FORKORTET, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjonNyBeskjed)
                    opprettNyBeskjed(nyBeskjed, notifikasjonNyBeskjed)
                    // Endre status på sak hvis forkortet til før d.d
                    settSakTilFerdigHvisAvtalestatusAvsluttet(avtaleHendelse)
                }
                HendelseType.MÅL_ENDRET -> {
                    log.info("AG: Mål endret: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.MÅL_ENDRET, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                    opprettNyBeskjed(nyBeskjed, notifikasjon)
                }
                HendelseType.INKLUDERINGSTILSKUDD_ENDRET -> {
                    log.info("AG: Inkluderingstilskudd endret: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.INKLUDERINGSTILSKUDD_ENDRET, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                }
                HendelseType.OM_MENTOR_ENDRET -> {
                    log.info("AG: Om mentor endret: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.OM_MENTOR_ENDRET, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                    opprettNyBeskjed(nyBeskjed, notifikasjon)
                }
                HendelseType.STILLINGSBESKRIVELSE_ENDRET -> {
                    log.info("AG: Stillingsbeskrivelse endret: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.STILLINGSBESKRIVELSE_ENDRET, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                    opprettNyBeskjed(nyBeskjed, notifikasjon)
                }
                HendelseType.OPPFØLGING_OG_TILRETTELEGGING_ENDRET -> {
                    log.info("AG: Oppfølging og tilrettelegging endret: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.OPPFØLGING_OG_TILRETTELEGGING_ENDRET, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                    opprettNyBeskjed(nyBeskjed, notifikasjon)
                }
                HendelseType.TILSKUDDSBEREGNING_ENDRET -> {
                    log.info("AG: Tilskuddsberegning endret: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.TILSKUDDSBEREGNING_ENDRET, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                    opprettNyBeskjed(nyBeskjed, notifikasjon)
                }
                HendelseType.KONTAKTINFORMASJON_ENDRET -> {
                    log.info("AG: Kontaktinformasjon endret: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.KONTAKTINFORMASJON_ENDRET, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                    opprettNyBeskjed(nyBeskjed, notifikasjon)
                }

                else -> {}
            }
        }
    }

    private fun settSakTilFerdigHvisAvtalestatusAvsluttet(avtaleHendelse: AvtaleHendelseMelding) {
        if (avtaleHendelse.avtaleStatus == AvtaleStatus.AVSLUTTET) {
            val saken = arbeidsgivernotifikasjonRepository.findSakByAvtaleId(avtaleHendelse.avtaleId.toString())
            if (saken != null) {
                log.info("AG: Avtale er avsluttet. Setter sak til ferdig. avtaleId: ${avtaleHendelse.avtaleId}")
                val nySakStatusFerdigQuery = nySakStatusFerdigQuery(saken.responseId!!)
                val notifikasjonNySakStatus = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.NySakStatus, Varslingsformål.INGEN_VARSLING, nySakStatusFerdigQuery)
                nySakStatus(nySakStatusFerdigQuery, notifikasjonNySakStatus, saken, ArbeidsgivernotifikasjonStatus.SAK_FERDIG)
            }
        }
    }


    fun lukkÅpneOppgaverPåAvtale(notifikasjonerPåAvtale: MineNotifikasjonerResultat?, avtaleHendelse: AvtaleHendelseMelding) {
        runBlocking {
            if (notifikasjonerPåAvtale is NotifikasjonConnection) {
                if (sjekkAtIkkeFlereSider(notifikasjonerPåAvtale, avtaleHendelse)) return@runBlocking

                log.info("AG: Fant ${notifikasjonerPåAvtale.edges.size} notifikasjoner på avtaleId: ${avtaleHendelse.avtaleId} Lukker de som er åpne/ny.")
                notifikasjonerPåAvtale.edges.forEach {
                    val notifikasjon = it.node
                    if (notifikasjon is Oppgave) {
                        // lukk hver oppgave
                        if (notifikasjon.oppgave.tilstand != OppgaveTilstand.NY) return@forEach
                        val oppgaveId = notifikasjon.metadata.id
                        val oppgaveUtfoert = oppgaveUtført(oppgaveId)
                        val notifikasjonFerdigstillOppgave = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.FerdigstillOppgave, Varslingsformål.INGEN_VARSLING, oppgaveUtfoert)
                        arbeidsgivernotifikasjonRepository.save(notifikasjonFerdigstillOppgave)
                        val response = notifikasjonGraphQlClient.execute(oppgaveUtfoert)
                        if (response.errors != null) {
                            log.error("AG: GraphQl-kall for å lukke oppgave feilet: ${response.errors}")
                            notifikasjonFerdigstillOppgave.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
                            notifikasjonFerdigstillOppgave.feilmelding = response.errors.toString()
                        } else {
                            notifikasjonFerdigstillOppgave.sendtTidspunkt = Instant.now()
                            val opprinneligOppgave = arbeidsgivernotifikasjonRepository.findOppgaveByResponseId(oppgaveId) //arbeidsgivernotifikasjonRepository.findAllByResponseId(oppgaveId).firstOrNull { oppgave -> oppgave.type == ArbeidsgivernotifikasjonType.Oppgave }
                            if (opprinneligOppgave != null) {
                                opprinneligOppgave.status = ArbeidsgivernotifikasjonStatus.OPPGAVE_FERDIGSTILT
                                arbeidsgivernotifikasjonRepository.save(opprinneligOppgave)
                            } else {
                                log.warn("AG: Fant ikke oppgave i DB for ferdigstilling. Trolig opprettet tidligere. oppgaveId: $oppgaveId avtaleId: ${avtaleHendelse.avtaleId}")
                            }
                            log.info("AG: Oppgave $oppgaveId lukket vellykket. avtaleId: ${avtaleHendelse.avtaleId}")
                        }
                        arbeidsgivernotifikasjonRepository.save(notifikasjonFerdigstillOppgave)
                    }
                }
            }
        }
    }

    private fun sjekkAtIkkeFlereSider(notifikasjonerPåAvtale: NotifikasjonConnection, avtaleHendelse: AvtaleHendelseMelding): Boolean {
        if (notifikasjonerPåAvtale.pageInfo.hasNextPage) {
            log.error("AG: Det er flere sider med notifikasjoner på avtaleId: ${avtaleHendelse.avtaleId} enn det som er hentet (${notifikasjonerPåAvtale.edges.size}). Limit skal være 1k")
            return true
        }
        return false
    }

    fun softDeleteOppgaverOgBeskjeder(notifikasjonerPåAvtale: MineNotifikasjonerResultat?, avtaleHendelse: AvtaleHendelseMelding) {
        runBlocking {
            if (notifikasjonerPåAvtale is NotifikasjonConnection) {
                if (sjekkAtIkkeFlereSider(notifikasjonerPåAvtale, avtaleHendelse)) return@runBlocking
                log.info("AG: Fant ${notifikasjonerPåAvtale.edges.size} notifikasjoner på avtaleId: ${avtaleHendelse.avtaleId} for softDelete.")
                notifikasjonerPåAvtale.edges.forEach {
                    val notifikasjon = it.node
                    val notifikasjonId: ID = when (notifikasjon) {
                        is Beskjed -> notifikasjon.metadata.id
                        is Oppgave -> notifikasjon.metadata.id
                        else -> {
                            log.error("AG: Fant en notifikasjon som ikke er beskjed eller oppgave. avtaleId: ${avtaleHendelse.avtaleId}")
                            return@forEach
                        }
                    }
                    softDeleteNotifikasjon(notifikasjonId, avtaleHendelse)
                }
            }
        }
    }

    private fun softDeleteNotifikasjon(notifikasjonId: String, avtaleHendelse: AvtaleHendelseMelding) {
        runBlocking {
            val softDeleteNotifikasjonQuery = nySoftDeleteNotifikasjonQuery(notifikasjonId)
            val notifikasjonSoftDelete = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.SoftDeleteNotifikasjon, Varslingsformål.INGEN_VARSLING, softDeleteNotifikasjonQuery)
            arbeidsgivernotifikasjonRepository.save(notifikasjonSoftDelete)
            val response = notifikasjonGraphQlClient.execute(softDeleteNotifikasjonQuery)
            if (response.errors != null) {
                log.error("AG: GraphQl-kall for å softDelete notifikasjon feilet: ${response.errors}")
                notifikasjonSoftDelete.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
                notifikasjonSoftDelete.feilmelding = response.errors.toString()
            } else {
                val resultat = response.data?.softDeleteNotifikasjon
                if (resultat is SoftDeleteNotifikasjonVellykket) {
                    log.info("AG: notifikasjon $notifikasjonId softDeletet vellykket. avtaleId: ${avtaleHendelse.avtaleId}")
                    notifikasjonSoftDelete.sendtTidspunkt = Instant.now()
                    notifikasjonSoftDelete.responseId = resultat.id
                    val arbeidsgivernotifikasjonIDb = arbeidsgivernotifikasjonRepository.findNotifikasjonByResponseId(notifikasjonId) //arbeidsgivernotifikasjonRepository.findAllByResponseId(notifikasjonId)
                    if (arbeidsgivernotifikasjonIDb != null) {
                        arbeidsgivernotifikasjonIDb.status = ArbeidsgivernotifikasjonStatus.SLETTET
                        arbeidsgivernotifikasjonRepository.save(arbeidsgivernotifikasjonIDb)
                    } else {
                        log.warn("AG: Fant ikke notifikasjon i DB for sletting. notifikasjonId: $notifikasjonId avtaleId: ${avtaleHendelse.avtaleId}")
                    }
                } else {
                    // UgyldigMerkelapp | NotifikasjonFinnesIkke | UkjentProdusent
                    log.error("AG: Soft delete av beskjed eller oppgave gikk ikke med resultatet: ${response.data?.softDeleteNotifikasjon}")
                    val softDeleteResultat = response.data?.softDeleteNotifikasjon.toString()
                    notifikasjonSoftDelete.feilmelding = softDeleteResultat
                    notifikasjonSoftDelete.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_OPPRETTELSE_HOS_FAGER
                }

            }
            arbeidsgivernotifikasjonRepository.save(notifikasjonSoftDelete)

        }
    }

    fun softDeleteSak(
        softDeleteSakQuery: SoftDeleteSakByGrupperingsid,
        avtaleId: String,
        notifikasjonSakSletting: Arbeidsgivernotifikasjon
    ): Boolean = runBlocking {
        val response = notifikasjonGraphQlClient.execute(softDeleteSakQuery)
        if (response.errors != null) {
            log.error("AG: GraphQl-kall for å softDelete sak feilet: ${response.errors}")
            notifikasjonSakSletting.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
            notifikasjonSakSletting.feilmelding = response.errors.toString()
            arbeidsgivernotifikasjonRepository.save(notifikasjonSakSletting)
            throw RuntimeException("GraphQl-kall for å softDelete sak feilet: ${response.errors}")
        } else {
            // Kall gikk bra
            val resultat = response.data?.softDeleteSakByGrupperingsid
            if (resultat is SoftDeleteSakVellykket) {
                log.info("AG: Sak slettet vellykket. grupperingsId: ${softDeleteSakQuery.variables.grupperingsid}")
                notifikasjonSakSletting.responseId = resultat.id
                notifikasjonSakSletting.sendtTidspunkt = Instant.now()
                arbeidsgivernotifikasjonRepository.save(notifikasjonSakSletting)
                // Oppdatere opprinnelig sak til slettet i DB:
                val opprinneligSak = arbeidsgivernotifikasjonRepository.findNotifikasjonByResponseId(resultat.id)
                if (opprinneligSak != null) {
                    opprinneligSak.status = ArbeidsgivernotifikasjonStatus.SAK_ANNULLERT
                    arbeidsgivernotifikasjonRepository.save(opprinneligSak)
                } else {
                    log.error("AG: Fant ikke SAK i DB for sletting. grupperingsId: ${softDeleteSakQuery.variables.grupperingsid}")
                }
                // Sett andre notifikasjoner også til slettet. Fager skal ha cascade på soft-delete av sak.
                arbeidsgivernotifikasjonRepository.findAllbyAvtaleId(avtaleId).filter { it.type == ArbeidsgivernotifikasjonType.Beskjed || it.type == ArbeidsgivernotifikasjonType.Oppgave }.forEach {
                    it.status = ArbeidsgivernotifikasjonStatus.SLETTET
                    arbeidsgivernotifikasjonRepository.save(it)
                }
                return@runBlocking true

            } else if (resultat is SakFinnesIkke) {
                log.info("AG: Sak finnes ikke. Må slette notifikasjoner manuelt. grupperingsId: ${softDeleteSakQuery.variables.grupperingsid}")
                return@runBlocking false
            } else {
                log.error("AG: Sak sletting gikk ikke med resultatet: ${response.data?.softDeleteSakByGrupperingsid}")
                return@runBlocking false
            }
        }
    }


    fun opprettNySak(nySak: NySak, notifikasjon: Arbeidsgivernotifikasjon) {
        runBlocking {
            val response = notifikasjonGraphQlClient.execute(nySak)

            if (response.errors != null) {
                log.error("AG: GraphQl-kall for å opprette sak feilet: ${response.errors}")
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
                notifikasjon.feilmelding = response.errors.toString()
                arbeidsgivernotifikasjonRepository.save(notifikasjon)
                return@runBlocking
            }

            val nySakResultat = response.data?.nySak
            if (nySakResultat is NySakVellykket) {
                log.info("AG: Sak opprettet vellykket. avtaleId: ${notifikasjon.avtaleId}")
                notifikasjon.responseId = nySakResultat.id
                notifikasjon.sendtTidspunkt = Instant.now()
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.SAK_MOTTATT
            } else {
                // UgyldigMerkelapp | UgyldigMottaker | DuplikatGrupperingsid | DuplikatGrupperingsidEtterDelete| UkjentProdusent | UkjentRolle
                log.error("AG: opprett sak gikk ikke med resultatet: ${response.data?.nySak}")
                val sakResultat = response.data?.nySak.toString()
                notifikasjon.feilmelding = sakResultat
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_OPPRETTELSE_HOS_FAGER
            }
            arbeidsgivernotifikasjonRepository.save(notifikasjon)

        }
    }

    fun nySakStatus(nySakStatusQuery: NyStatusSak, notifikasjon: Arbeidsgivernotifikasjon, opprinneligSak: Arbeidsgivernotifikasjon, saksStatus: ArbeidsgivernotifikasjonStatus) {
        if (!listOf(ArbeidsgivernotifikasjonStatus.SAK_ANNULLERT, ArbeidsgivernotifikasjonStatus.SAK_FERDIG, ArbeidsgivernotifikasjonStatus.SAK_MOTTATT).contains(saksStatus)) {
            throw IllegalArgumentException("Kan kun sette sak til annullert eller ferdig. Forsøkte å sette sak til status $saksStatus på notifikasjon med id ${notifikasjon.id} for avtaleId: ${notifikasjon.avtaleId}")
        }
        runBlocking {
            val response = notifikasjonGraphQlClient.execute(nySakStatusQuery)

            if (response.errors != null) {
                log.error("AG: GraphQl-kall for å endre sakStatus til ${nySakStatusQuery.variables.nyStatus} feilet: ${response.errors}")
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
                notifikasjon.feilmelding = response.errors.toString()
                arbeidsgivernotifikasjonRepository.save(notifikasjon)
                return@runBlocking
            }

            val nySakStatusResultat = response.data?.nyStatusSak
            if (nySakStatusResultat is NyStatusSakVellykket) {
                log.info("AG: Sak status oppdatert vellykket. NyStatus: ${nySakStatusQuery.variables.nyStatus} overstyrtTekst: ${nySakStatusQuery.variables.overstyrStatustekstMed}")
                notifikasjon.responseId = nySakStatusResultat.id
                notifikasjon.sendtTidspunkt = Instant.now()
                opprinneligSak.status = saksStatus
                if (nySakStatusQuery.variables.hardDelete?.nyTid?.den !== null) {
                    opprinneligSak.hardDeleteSkedulertTidspunkt = LocalDateTime.parse(nySakStatusQuery.variables.hardDelete.nyTid.den)
                } else {
                    opprinneligSak.hardDeleteSkedulertTidspunkt = null
                }

                arbeidsgivernotifikasjonRepository.save(opprinneligSak)
            } else {
                log.error("AG: Sett sak status til ${nySakStatusQuery.variables.nyStatus} gikk ikke med resultatet: ${response.data?.nyStatusSak}")
                val sakResultat = response.data?.nyStatusSak.toString()
                notifikasjon.feilmelding = sakResultat
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_OPPRETTELSE_HOS_FAGER
            }
            arbeidsgivernotifikasjonRepository.save(notifikasjon)
        }
    }

    fun opprettNyOppgave(nyOppgave: NyOppgave, notifikasjon: Arbeidsgivernotifikasjon) {
        runBlocking {
            val response = notifikasjonGraphQlClient.execute(nyOppgave)

            if (response.errors != null) {
                log.error("AG: GraphQl-kall for å opprette oppgave feilet: ${response.errors}")
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
                notifikasjon.feilmelding = response.errors.toString()
                arbeidsgivernotifikasjonRepository.save(notifikasjon)
                return@runBlocking
            }

            val nyOppgaveResultat = response.data?.nyOppgave
            if (nyOppgaveResultat is NyOppgaveVellykket) {
                log.info("AG: Oppgave opprettet vellykket. avtaleId: ${notifikasjon.avtaleId}")
                notifikasjon.responseId = nyOppgaveResultat.id
                notifikasjon.sendtTidspunkt = Instant.now()

                tiltakNotifikasjonKvitteringProdusent.sendNotifikasjonKvittering(notifikasjon)
            } else {
                //  UgyldigMerkelapp | UgyldigMottaker | DuplikatGrupperingsid | DuplikatGrupperingsidEtterDelete| UkjentProdusent | UkjentRolle
                log.error("AG: opprett oppgave gikk ikke med resultatet: ${response.data?.nyOppgave}")
                val oppgaveResultat = response.data?.nyOppgave.toString()
                notifikasjon.feilmelding = oppgaveResultat
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_OPPRETTELSE_HOS_FAGER
            }
            arbeidsgivernotifikasjonRepository.save(notifikasjon)
        }
    }

    fun opprettNyBeskjed(nyBeskjed: NyBeskjed, notifikasjon: Arbeidsgivernotifikasjon) {
        runBlocking {
            val response = notifikasjonGraphQlClient.execute(nyBeskjed)

            if (response.errors != null) {
                log.error("AG: GraphQl-kall for å opprette beskjed feilet: ${response.errors}")
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
                notifikasjon.feilmelding = response.errors.toString()
                arbeidsgivernotifikasjonRepository.save(notifikasjon)
                return@runBlocking
            }

            val nyBeskjedResultat = response.data?.nyBeskjed
            if (nyBeskjedResultat is NyBeskjedVellykket) {
                log.info("AG: Beskjed opprettet vellykket. avtaleId: ${notifikasjon.avtaleId}")
                notifikasjon.responseId = nyBeskjedResultat.id
                notifikasjon.sendtTidspunkt = Instant.now()

                tiltakNotifikasjonKvitteringProdusent.sendNotifikasjonKvittering(notifikasjon)

            } else {
                // NyBeskjedVellykket | UgyldigMerkelapp | UgyldigMottaker | DuplikatGrupperingsid | DuplikatGrupperingsidEtterDelete| UkjentProdusent | UkjentRolle
                log.error("AG: opprett beskjed gikk ikke med resultatet: ${response.data?.nyBeskjed}")
                val beskjedResultat = response.data?.nyBeskjed.toString()
                notifikasjon.feilmelding = beskjedResultat
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_OPPRETTELSE_HOS_FAGER
            }
            arbeidsgivernotifikasjonRepository.save(notifikasjon)
        }
    }


    private fun nyArbeidsgivernotifikasjon(avtaleHendelse: AvtaleHendelseMelding, type: ArbeidsgivernotifikasjonType, varslingsformål: Varslingsformål, notifikasjonObject: Any
    ): Arbeidsgivernotifikasjon {
        return Arbeidsgivernotifikasjon(
            id = ulid(),
            avtaleMeldingJson = jacksonMapper().writeValueAsString(avtaleHendelse),
            arbeidsgivernotifikasjonJson = jacksonMapper().writeValueAsString(notifikasjonObject),
            type = type,
            status = ArbeidsgivernotifikasjonStatus.BEHANDLET,
            bedriftNr = avtaleHendelse.bedriftNr,
            avtaleHendelseType = avtaleHendelse.hendelseType,
            varslingsformål = varslingsformål,
            avtaleId = avtaleHendelse.avtaleId.toString(),
            avtaleNr = avtaleHendelse.avtaleNr,
            opprettetTidspunkt = Instant.now()
        )
    }

    fun finnesDuplikatMelding(avtaleHendelse: AvtaleHendelseMelding): Boolean {
        // Sjekker om det finnes behandlede avtaleHendelser i basen som har likt endret tildspunt som den som kommer inn. IDEMPOTENCE-SJEKK
        arbeidsgivernotifikasjonRepository.findAllbyAvtaleId(avtaleHendelse.avtaleId.toString()).forEach {
            if (it.status != ArbeidsgivernotifikasjonStatus.SLETTET) {
                val melding: AvtaleHendelseMelding = jacksonMapper().readValue(it.avtaleMeldingJson)
                if (melding.sistEndret == avtaleHendelse.sistEndret && melding.hendelseType == avtaleHendelse.hendelseType) {
                    log.warn("AG: Fant en brukernotifikasjon med samme hendelsetype og sistEndret tidspunkt som allerede er behandlet, avtaleId: ${avtaleHendelse.avtaleId}")
                    return true
                }
            }
        }
        return false
    }


    fun hentMineSaker(avtaleId: String, tiltakstype: Tiltakstype): GraphQLClientResponse<MineNotifikasjoner.Result> {
        val mineNotifikasjonerQuery = mineNotifikasjoner(tiltakstype, avtaleId)
        log.info("AG: laget request for mine saker på avtaleId $avtaleId og merkelapp ${tiltakstype.arbeidsgiverNotifikasjonMerkelapp}")

        return runBlocking {
            notifikasjonGraphQlClient.execute(mineNotifikasjonerQuery)
        }
    }

}
