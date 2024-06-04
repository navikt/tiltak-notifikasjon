package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import kotlinx.coroutines.runBlocking
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.MineNotifikasjoner
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.NyBeskjed
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.NyOppgave
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.NySak
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.OppgaveTilstand
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.MineNotifikasjonerResultat
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.NotifikasjonConnection
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.Oppgave
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nybeskjed.NyBeskjedVellykket
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nyoppgave.NyOppgaveVellykket
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nysak.NySakVellykket
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
    @Qualifier("azureWebClientBuilder") azureWebClientBuilder: WebClient.Builder,
    val arbeidsgivernotifikasjonRepository: ArbeidsgivernotifikasjonRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)
    val notifikasjonGraphQlClient = GraphQLWebClient(arbeidsgivernotifikasjonProperties.url, builder = azureWebClientBuilder)

    fun behandleAvtaleHendelseMelding(avtaleHendelse: AvtaleHendelseMelding) {
        runBlocking {
            when (avtaleHendelse.hendelseType) {

                HendelseType.OPPRETTET -> {
                    // Sak
                    val nySak = nySak(avtaleHendelse)
                    val notifikasjonJson = jacksonMapper().writeValueAsString(nySak)
                    val notifikasjon = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Sak, Varslingsformål.GODKJENNING_AV_AVTALE, notifikasjonJson)
                    arbeidsgivernotifikasjonRepository.save(notifikasjon)
                    opprettNySak(nySak, notifikasjon)
                    //Oppgave (på saken - via grupperingsId)
                    val nyOppgave = nyOppgave(avtaleHendelse)
                    val notifikasjonJsonOppgave = jacksonMapper().writeValueAsString(nyOppgave)
                    val notifikasjonOppgave = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Oppgave, Varslingsformål.GODKJENNING_AV_AVTALE, notifikasjonJsonOppgave)
                    arbeidsgivernotifikasjonRepository.save(notifikasjonOppgave)
                    opprettNyOppgave(nyOppgave, notifikasjonOppgave)
                }

                HendelseType.GODKJENT_AV_ARBEIDSGIVER -> {
                    // Lukk alle oppgaver
                    val mineNotifikasjonerQuery = mineNotifikasjoner(avtaleHendelse.tiltakstype.beskrivelse, avtaleHendelse.avtaleId.toString())
                    val response = notifikasjonGraphQlClient.execute(mineNotifikasjonerQuery)
                    val notifikasjoner = response.data?.mineNotifikasjoner
                    lukkÅpneOppgaverPåAvtale(notifikasjoner, avtaleHendelse)
                }

                HendelseType.ARBEIDSGIVERS_GODKJENNING_OPPHEVET_AV_VEILEDER -> {
                    //Oppgave (på saken - via grupperingsId)
                    val nyOppgave = nyOppgave(avtaleHendelse)
                    val notifikasjonJsonOppgave = jacksonMapper().writeValueAsString(nyOppgave)
                    val notifikasjonOppgave = nyArbeidsgivernotifikasjon(avtaleHendelse, ArbeidsgivernotifikasjonType.Oppgave, Varslingsformål.GODKJENNING_AV_AVTALE, notifikasjonJsonOppgave)
                    arbeidsgivernotifikasjonRepository.save(notifikasjonOppgave)
                    opprettNyOppgave(nyOppgave, notifikasjonOppgave)
                }

                else -> {}
            }
            //  HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER
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
                        val notifikasjonJsonOppgave = jacksonMapper().writeValueAsString(oppgaveUtfoert)
                        val notifikasjonOppgave = nyArbeidsgivernotifikasjon(
                            avtaleHendelse, ArbeidsgivernotifikasjonType.Oppgave, Varslingsformål.GODKJENNING_AV_AVTALE, notifikasjonJsonOppgave
                        )
                        arbeidsgivernotifikasjonRepository.save(notifikasjonOppgave)
                        val response = notifikasjonGraphQlClient.execute(oppgaveUtfoert)
                        if (response.errors != null) {
                            log.error("GraphQl-kall for å lukke oppgave feilet: ${response.errors}")
                            notifikasjonOppgave.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
                            notifikasjonOppgave.feilmelding = response.errors.toString()
                            arbeidsgivernotifikasjonRepository.save(notifikasjonOppgave)
                        } else {
                            notifikasjonOppgave.sendt = Instant.now()
                            arbeidsgivernotifikasjonRepository.save(notifikasjonOppgave)
                        }
                    }
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

            val nySak = response.data?.nySak
            if (nySak is NySakVellykket) {
                log.info("Sak opprettet vellykket. avtaleId: ${notifikasjon.avtaleId}")
                notifikasjon.responseId = nySak.id
                notifikasjon.sendt = Instant.now()
            } else {
                // NySakVellykket | UgyldigMerkelapp | UgyldigMottaker | DuplikatGrupperingsid | DuplikatGrupperingsidEtterDelete| UkjentProdusent | UkjentRolle
                log.error("opprett sak gikk ikke med resultatet: ${response.data?.nySak}")
                val sakResultat = response.data?.nySak.toString()
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

            val nyOppgave = response.data?.nyOppgave
            if (nyOppgave is NyOppgaveVellykket) {
                log.info("Oppgave opprettet vellykket. avtaleId: ${notifikasjon.avtaleId}")
                notifikasjon.responseId = nyOppgave.id
                notifikasjon.sendt = Instant.now()
            } else {
                // NyOppgaveVellykket | UgyldigMerkelapp | UgyldigMottaker | DuplikatGrupperingsid | DuplikatGrupperingsidEtterDelete| UkjentProdusent | UkjentRolle
                log.error("opprett oppgave gikk ikke med resultatet: ${response.data?.nyOppgave}")
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
                log.error("GraphQl-kall for å opprette beskjed feilet: ${response.errors}")
                notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
                notifikasjon.feilmelding = response.errors.toString()
                arbeidsgivernotifikasjonRepository.save(notifikasjon)
                return@runBlocking
            }

            val nyBeskjed = response.data?.nyBeskjed
            if (nyBeskjed is NyBeskjedVellykket) {
                log.info("Beskjed opprettet vellykket. avtaleId: ${notifikasjon.avtaleId}")
                notifikasjon.responseId = nyBeskjed.id
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


    fun nyArbeidsgivernotifikasjon(
        avtaleHendelse: AvtaleHendelseMelding, type: ArbeidsgivernotifikasjonType, varslingsformål: Varslingsformål, notifikasjonJson: String
    ): Arbeidsgivernotifikasjon {
        return Arbeidsgivernotifikasjon(
            id = ulid(),
            varselId = "123",
            avtaleMeldingJson = jacksonMapper().writeValueAsString(avtaleHendelse),
            notifikasjonJson = notifikasjonJson,
            type = type,
            status = ArbeidsgivernotifikasjonStatus.BEHANDLET,
            bedriftNr = avtaleHendelse.bedriftNr,
            avtaleHendelseType = avtaleHendelse.hendelseType,
            varslingsformål = varslingsformål
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
