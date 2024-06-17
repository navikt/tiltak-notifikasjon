package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import kotlinx.coroutines.runBlocking
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.*
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.OppgaveTilstand
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.MineNotifikasjonerResultat
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.NotifikasjonConnection
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.Oppgave
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nybeskjed.NyBeskjedVellykket
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nyoppgave.NyOppgaveVellykket
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nysak.NySakVellykket
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.softdeletesakbygrupperingsid.SakFinnesIkke
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.softdeletesakbygrupperingsid.SoftDeleteSakVellykket
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant


@Profile("dev-gcp")
@Component
class ArbeidsgiverNotifikasjonService(
    arbeidsgivernotifikasjonProperties: ArbeidsgivernotifikasjonProperties,
    private val altinnProperties: AltinnProperties,
    @Qualifier("azureWebClientBuilder") azureWebClientBuilder: WebClient.Builder,
    val arbeidsgivernotifikasjonRepository: ArbeidsgivernotifikasjonRepository,
    properties: AltinnProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)
    val notifikasjonGraphQlClient = GraphQLWebClient(arbeidsgivernotifikasjonProperties.url, builder = azureWebClientBuilder)

    fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding) {
        runBlocking {
            when (avtaleHendelse.hendelseType) {

                HendelseType.OPPRETTET -> {
                    log.info("Avtale opprettet: lager sak og oppgave. avtaleId: ${avtaleHendelse.avtaleId}")
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

                HendelseType.GODKJENT_AV_ARBEIDSGIVER,
                HendelseType.GODKJENT_PAA_VEGNE_AV_ARBEIDSGIVER,
                HendelseType.GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER -> {
                    log.info("Avtale godkjent: lukker oppgaver. avtaleId: ${avtaleHendelse.avtaleId}")
                    // Lukk alle oppgaver
                    val mineNotifikasjonerQuery = mineNotifikasjoner(avtaleHendelse.tiltakstype.beskrivelse, avtaleHendelse.avtaleId.toString()) // TODO: tilse at grupperingsId blir laget likt overalt og explisitt.
                    val response = notifikasjonGraphQlClient.execute(mineNotifikasjonerQuery)
                    val notifikasjoner = response.data?.mineNotifikasjoner
                    lukkÅpneOppgaverPåAvtale(notifikasjoner, avtaleHendelse)
                    // TODO: Sette sak til FERDIG. Sjekke om man trengr å lukke oppgaver når sak settes til ferdig
                }

                HendelseType.ARBEIDSGIVERS_GODKJENNING_OPPHEVET_AV_VEILEDER -> {
                    log.info("Avtale godkjenning opphevet: lager ny oppgave. avtaleId: ${avtaleHendelse.avtaleId}")
                    //Oppgave (på saken - via grupperingsId)
                    val nyOppgave = nyOppgave(avtaleHendelse, altinnProperties)
                    val notifikasjonJsonOppgave = jacksonMapper().writeValueAsString(nyOppgave)
                    val notifikasjonOppgave = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Oppgave, Varslingsformål.GODKJENNING_AV_AVTALE, notifikasjonJsonOppgave)
                    arbeidsgivernotifikasjonRepository.save(notifikasjonOppgave)
                    opprettNyOppgave(nyOppgave, notifikasjonOppgave)
                }

                HendelseType.ANNULLERT -> {
                    log.info("Avtale annullert: sletter saker, oppgaver og beskjeder. avtaleId: ${avtaleHendelse.avtaleId}")
                    // soft delete saker, oppgaver og beskjeder
                    // Soft delete sak: gjøres med grupperingsId (avtaleId)
                    // Soft delete oppgaver og beskjeder (hentes med mineNotifikasjoner og deletes med id)
                    // TODO: Fager skal sjekke opp om de kan cascade på softdelete også (tar med seg alt når vi sletter sak)
                    // TODO2: Fager skal implementere cascade på soft-delete. ETA er denne uken (uke 25) Avventer til det er på plass.
                    // men det kan finnes oppgaver/beskjeder på avtalen uten at det er en sak der (fra gammelt oppsett) må uansett slette de.

                    // SAK
                    val softDeleteSakQuery = softDeleteSak(avtaleHendelse.tiltakstype.arbeidsgiverNotifikasjonMerkelapp, avtaleHendelse.avtaleId.toString())
                    softDeleteSak(softDeleteSakQuery)
                }

                // BESKJEDER
                HendelseType.AVTALE_INNGÅTT -> {
                    log.info("Avtale inngått: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.AVTALE_INNGÅTT, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                    opprettNyBeskjed(nyBeskjed, notifikasjon)
                }
                HendelseType.AVTALE_FORLENGET -> {
                    log.info("Avtale forlenget: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.AVTALE_FORLENGET, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                    opprettNyBeskjed(nyBeskjed, notifikasjon)
                }
                HendelseType.AVTALE_FORKORTET -> {
                    log.info("Avtale forkortet: lager beskjed. avtaleId: ${avtaleHendelse.avtaleId}")
                    val nyBeskjed = nyBeskjed(avtaleHendelse, altinnProperties)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Beskjed, Varslingsformål.AVTALE_FORKORTET, nyBeskjed)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                    opprettNyBeskjed(nyBeskjed, notifikasjon)
                }

                else -> {}
            }
        }
    }

    fun lukkÅpneOppgaverPåAvtale(notifikasjonerPåAvtale: MineNotifikasjonerResultat?, avtaleHendelse: AvtaleHendelseMelding) {
        runBlocking {
            if (notifikasjonerPåAvtale is NotifikasjonConnection) {
                if (notifikasjonerPåAvtale.pageInfo.hasNextPage) {
                    log.error("Det er flere sider med notifikasjoner på avtaleId: ${avtaleHendelse.avtaleId} enn det som er hentet (${notifikasjonerPåAvtale.edges.size}). Limit skal være 1k")
                    return@runBlocking
                }

                log.info("Fant ${notifikasjonerPåAvtale.edges.size} notifikasjoner på avtaleId: ${avtaleHendelse.avtaleId} Lukker de som er åpne/ny.")
                notifikasjonerPåAvtale.edges.forEach {
                    val notifikasjon = it.node
                    if (notifikasjon is Oppgave) {
                        // lukk hver oppgave
                        if (notifikasjon.oppgave.tilstand != OppgaveTilstand.NY) return@forEach
                        val oppgaveId = notifikasjon.metadata.id
                        val oppgaveUtfoert = oppgaveUtført(oppgaveId)
                        val notifikasjonOppgave = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.FerdigstillOppgave, Varslingsformål.INGEN_VARSLING, oppgaveUtfoert)
                        arbeidsgivernotifikasjonRepository.save(notifikasjonOppgave)
                        val response = notifikasjonGraphQlClient.execute(oppgaveUtfoert)
                        if (response.errors != null) {
                            log.error("GraphQl-kall for å lukke oppgave feilet: ${response.errors}")
                            notifikasjonOppgave.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
                            notifikasjonOppgave.feilmelding = response.errors.toString()
                        } else {
                            notifikasjonOppgave.sendt = Instant.now()
                            log.info("Oppgave $oppgaveId lukket vellykket. avtaleId: ${avtaleHendelse.avtaleId}")
                        }
                        arbeidsgivernotifikasjonRepository.save(notifikasjonOppgave)
                    }
                }
            }
        }
    }

    fun softDeleteSak(softDeleteSakQuery: SoftDeleteSakByGrupperingsid) {
        runBlocking {
            val response = notifikasjonGraphQlClient.execute(softDeleteSakQuery)
            if (response.errors != null) {
                log.error("GraphQl-kall for å softDelete sak feilet: ${response.errors}")
            } else {
                val resultat = response.data?.softDeleteSakByGrupperingsid
                if (resultat is SoftDeleteSakVellykket) {
                    log.info("Sak slettet vellykket. grupperingsId: ${softDeleteSakQuery.variables.grupperingsid}")
                } else if (resultat is SakFinnesIkke) {
                    log.info("Sak finnes ikke. Må slette notifikasjoner mauelt. grupperingsId: ${softDeleteSakQuery.variables.grupperingsid}")
                } else {
                    log.error("Sak sletting gikk ikke med resultatet: ${response.data?.softDeleteSakByGrupperingsid}")
                }
            }
        }
    }

    fun opprettNySak(nySak: NySak, notifikasjon: Arbeidsgivernotifikasjon) {
        runBlocking {
            val response = notifikasjonGraphQlClient.execute(nySak)

            if (response.errors != null) {
                log.error("GraphQl-kall for å opprette sak feilet: ${response.errors}")
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
                notifikasjon.feilmelding = response.errors.toString()
                arbeidsgivernotifikasjonRepository.save(notifikasjon)
                return@runBlocking
            }

            val nySakResultat = response.data?.nySak
            if (nySakResultat is NySakVellykket) {
                log.info("Sak opprettet vellykket. avtaleId: ${notifikasjon.avtaleId}")
                notifikasjon.responseId = nySakResultat.id
                notifikasjon.sendt = Instant.now()
            } else {
                // NySakVellykket | UgyldigMerkelapp | UgyldigMottaker | DuplikatGrupperingsid | DuplikatGrupperingsidEtterDelete| UkjentProdusent | UkjentRolle
                log.error("opprett sak gikk ikke med resultatet: ${response.data?.nySak}")
                val sakResultat = response.data?.nySak.toString() // TODO: JSON serialisere her, evt. hente ut kun feilmelding.
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
                log.error("GraphQl-kall for å opprette oppgave feilet: ${response.errors}")
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
                notifikasjon.feilmelding = response.errors.toString()
                arbeidsgivernotifikasjonRepository.save(notifikasjon)
                return@runBlocking
            }

            val nyOppgaveResultat = response.data?.nyOppgave
            if (nyOppgaveResultat is NyOppgaveVellykket) {
                log.info("Oppgave opprettet vellykket. avtaleId: ${notifikasjon.avtaleId}")
                notifikasjon.responseId = nyOppgaveResultat.id
                notifikasjon.sendt = Instant.now()
            } else {
                // NyOppgaveVellykket | UgyldigMerkelapp | UgyldigMottaker | DuplikatGrupperingsid | DuplikatGrupperingsidEtterDelete| UkjentProdusent | UkjentRolle
                log.error("opprett oppgave gikk ikke med resultatet: ${response.data?.nyOppgave}")
                val oppgaveResultat = response.data?.nyOppgave.toString() // TODO: JSON serialisere her, evt. hente ut kun feilmelding.
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
                log.error("GraphQl-kall for å opprette beskjed feilet: ${response.errors}")
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
                notifikasjon.feilmelding = response.errors.toString()
                arbeidsgivernotifikasjonRepository.save(notifikasjon)
                return@runBlocking
            }

            val nyBeskjedResultat = response.data?.nyBeskjed
            if (nyBeskjedResultat is NyBeskjedVellykket) {
                log.info("Beskjed opprettet vellykket. avtaleId: ${notifikasjon.avtaleId}")
                notifikasjon.responseId = nyBeskjedResultat.id
                notifikasjon.sendt = Instant.now()
            } else {
                // NyBeskjedVellykket | UgyldigMerkelapp | UgyldigMottaker | DuplikatGrupperingsid | DuplikatGrupperingsidEtterDelete| UkjentProdusent | UkjentRolle
                log.error("opprett beskjed gikk ikke med resultatet: ${response.data?.nyBeskjed}")
                val beskjedResultat = response.data?.nyBeskjed.toString()
                notifikasjon.feilmelding = beskjedResultat
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_OPPRETTELSE_HOS_FAGER
            }
            arbeidsgivernotifikasjonRepository.save(notifikasjon)
        }
    }



    private fun nyArbeidsgivernotifikasjon(
        avtaleHendelse: AvtaleHendelseMelding, type: ArbeidsgivernotifikasjonType, varslingsformål: Varslingsformål, oppgaveUtfoert: OppgaveUtfoert
    ): Arbeidsgivernotifikasjon =
        nyArbeidsgivernotifikasjon(avtaleHendelse, type, varslingsformål, jacksonMapper().writeValueAsString(oppgaveUtfoert))

    fun nyArbeidsgivernotifikasjon(
        avtaleHendelse: AvtaleHendelseMelding, type: ArbeidsgivernotifikasjonType, varslingsformål: Varslingsformål, nyBeskjed: NyBeskjed
    ) = nyArbeidsgivernotifikasjon(avtaleHendelse, type, varslingsformål, jacksonMapper().writeValueAsString(nyBeskjed))

    fun nyArbeidsgivernotifikasjon(
        avtaleHendelse: AvtaleHendelseMelding, type: ArbeidsgivernotifikasjonType, varslingsformål: Varslingsformål, nyOppgave: NyOppgave
    ) = nyArbeidsgivernotifikasjon(avtaleHendelse, type, varslingsformål, jacksonMapper().writeValueAsString(nyOppgave))

    fun nyArbeidsgivernotifikasjon(
        avtaleHendelse: AvtaleHendelseMelding, type: ArbeidsgivernotifikasjonType, varslingsformål: Varslingsformål, nySak: NySak
    ) = nyArbeidsgivernotifikasjon(avtaleHendelse, type, varslingsformål, jacksonMapper().writeValueAsString(nySak))

    private fun nyArbeidsgivernotifikasjon(
        avtaleHendelse: AvtaleHendelseMelding, type: ArbeidsgivernotifikasjonType, varslingsformål: Varslingsformål, notifikasjonJson: String
    ): Arbeidsgivernotifikasjon {
        return Arbeidsgivernotifikasjon(
            id = ulid(),
            varselId = null,
            avtaleMeldingJson = jacksonMapper().writeValueAsString(avtaleHendelse),
            notifikasjonJson = notifikasjonJson,
            type = type,
            status = ArbeidsgivernotifikasjonStatus.BEHANDLET,
            bedriftNr = avtaleHendelse.bedriftNr,
            avtaleHendelseType = avtaleHendelse.hendelseType,
            varslingsformål = varslingsformål,
            avtaleId = avtaleHendelse.avtaleId.toString(),
            avtaleNr = avtaleHendelse.avtaleNr,
            opprettet = Instant.now()
        )
    }


    fun hentMineSaker(avtaleId: String, merkelapp: String): GraphQLClientResponse<MineNotifikasjoner.Result> {
        val mineNotifikasjonerQuery = mineNotifikasjoner(merkelapp, avtaleId)
        log.info("laget request for mine saker på avtaleId $avtaleId og merkelapp $merkelapp")

        return runBlocking {
            notifikasjonGraphQlClient.execute(mineNotifikasjonerQuery)
        }
    }

}
