package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.fasterxml.jackson.module.kotlin.readValue
import io.getunleash.Unleash
import kotlinx.coroutines.runBlocking
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.*
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.hentsakmedgrupperingsid.HentetSak
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nybeskjed.NyBeskjedVellykket
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nysak.NySakVellykket
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant
import java.time.format.TextStyle
import java.util.Locale

@Component
@Profile("prod-gcp", "dev-gcp", "dockercompose")
class RefusjonVarselConsumer(
    private val arbeidsgiverRefusjonKontaktpersonRepository: ArbeidsgiverRefusjonKontaktpersonRepository,
    private val arbeidsgiverRefusjonNotifikasjonRepository: ArbeidsgiverRefusjonNotifikasjonRepository,
    arbeidsgivernotifikasjonProperties: ArbeidsgivernotifikasjonProperties,
    @Qualifier("azureWebClientBuilder") azureWebClientBuilder: WebClient.Builder,
    private val unleash: Unleash,
) {
    private val mapper = jacksonMapper()
    private val log = LoggerFactory.getLogger(javaClass)
    val notifikasjonGraphQlClient = GraphQLWebClient(arbeidsgivernotifikasjonProperties.url, builder = azureWebClientBuilder)

    @KafkaListener(topics = [Topics.TILTAK_VARSEL])
    fun konsumer(melding: ConsumerRecord<String, String>) {
        try {
            if (!unleash.isEnabled("refusjon-klar-i-tiltak-notifikasjon")) return // Switch som gjør motsatt togling på dagens kode i tiltaksgjennomforing-api.
            val refusjonVarselMelding: RefusjonVarselMelding = mapper.readValue(melding.value())
            if (refusjonVarselMelding.refusjonVarselType != RefusjonVarselType.KLAR) return
            val refusjonKontaktpersonEntitet = arbeidsgiverRefusjonKontaktpersonRepository.findByAvtaleId(refusjonVarselMelding.avtaleId)
            if (refusjonKontaktpersonEntitet == null) {
                log.warn("AG: Fant ingen refusjonkontaktperson for avtaleId ${refusjonVarselMelding.avtaleId} Skipper melding med offset ${melding.offset()}")
                return
            }

            /* Vi kjører idempotens-sjekk eksplisitt på sak og beskjed på refusjonId og varselType.  */
            if (!finnesSak(refusjonKontaktpersonEntitet.grupperingsId(), refusjonKontaktpersonEntitet.tiltakstype)) {
                // Sak for alle refusjonene på avtalen
                opprettNySak(refusjonVarselMelding.refusjonId, refusjonVarselMelding, refusjonKontaktpersonEntitet, melding)
            }
            // Beskjed med sms om refusjon klar
            if (finnesNotifikasjon(refusjonVarselMelding.refusjonId, refusjonVarselMelding.refusjonVarselType)) {
                log.warn("AG: Notifikasjon for refusjonId ${refusjonVarselMelding.refusjonId} og varseltype ${refusjonVarselMelding.refusjonVarselType} finnes allerede. Skipper opprettelse av ny notifikasjon. Skipper melding med offset ${melding.offset()}")
                return
            }
            val måned: String = refusjonVarselMelding.tilskuddFom.month.getDisplayName(TextStyle.FULL, Locale.of("no"))
            opprettNyBeskjed(refusjonVarselMelding, refusjonKontaktpersonEntitet, refusjonVarselMelding.refusjonId, refusjonVarselMelding.refusjonsnummer, melding, måned)


        } catch (e: Exception) {
            val notifikasjon = ArbeidsgiverRefusjonNotifikasjon(
                ulid(),
                arbeidsgivernotifikasjonJson = melding.value(),
                kafkaOffset = melding.offset(),
                kafkaKey = melding.key(),
                type = ArbeidsgivernotifikasjonType.Ukjent,
                status = ArbeidsgivernotifikasjonStatus.FEILET_VED_BEHANDLING,
                bedriftNr = null,
                varslingsformål = Varslingsformål.INGEN_VARSLING,
                avtaleId = null,
                refusjonId = null,
                feilmelding = e.stackTraceToString()
            )
            arbeidsgiverRefusjonNotifikasjonRepository.save(notifikasjon)
            log.error("Feil ved konsumering av refusjon varsel fra topic ${melding.topic()} offset ${melding.offset()}", e)
        }
    }

    fun finnesSak(grupperingsId: String, tiltakstype: Tiltakstype): Boolean {
        var sakFinnes = false
        val sakQuery = nyHentSakQuery(grupperingsId, tiltakstype.arbeidsgiverNotifikasjonMerkelapp)
        runBlocking {
            val response = notifikasjonGraphQlClient.execute(sakQuery)
            if (response.errors != null) {
                // om sak skulle finnes vil vi bare få duplikat-feilmelding i graphql, så det gjør ikke så mye å forsøke å opprette 2 ganger.
                log.error("AG: GraphQl-kall for å hente refusjon-sak med grupperingsid feilet: ${response.errors}")
            }
            if (response.data?.hentSakMedGrupperingsid is HentetSak) {
                sakFinnes = true
            }
        }
        return sakFinnes
    }
    fun finnesNotifikasjon(refusjonId: String, varselType: RefusjonVarselType): Boolean {
        val notifikasjoner = arbeidsgiverRefusjonNotifikasjonRepository.findAllByRefusjonId(refusjonId)
        val find = notifikasjoner.find { it.varslingsformål == varselType.tilVarslingsformål() }
        return find != null
    }

    fun opprettNyBeskjed(
        refusjonVarselMelding: RefusjonVarselMelding,
        refusjonKontaktperson: RefusjonKontaktpersonEntitet,
        refusjonId: String,
        refusjonsnummer: String,
        melding: ConsumerRecord<String, String>,
        måned: String
    ) {
        val refusjonBeskjed = nyBeskjedRefusjoner(refusjonKontaktperson, refusjonId, refusjonsnummer, måned)
        val notifikasjon = ArbeidsgiverRefusjonNotifikasjon(
            ulid(),
            arbeidsgivernotifikasjonJson = jacksonMapper().writeValueAsString(refusjonBeskjed),
            type = ArbeidsgivernotifikasjonType.Beskjed,
            status = ArbeidsgivernotifikasjonStatus.BEHANDLET,
            bedriftNr = refusjonKontaktperson.bedriftNr,
            varslingsformål = refusjonVarselMelding.refusjonVarselType.tilVarslingsformål(),
            avtaleId = refusjonKontaktperson.avtaleId.toString(),
            refusjonId = refusjonId,
            kafkaOffset = melding.offset(),
            kafkaKey = melding.key(), // FOR IDEMPOTENS SJEKK,
        )
        arbeidsgiverRefusjonNotifikasjonRepository.save(notifikasjon)
        runBlocking {
            val response = notifikasjonGraphQlClient.execute(refusjonBeskjed)
            val resultat = response.data?.nyBeskjed
            if (resultat is NyBeskjedVellykket) {
                settVellykket(notifikasjon, resultat.id, "refusjon-beskjed")
            } else {
                settFeilet(notifikasjon, response, "refusjon-beskjed")
            }
        }
    }

    fun opprettNySak(
        refusjonId: String,
        refusjonVarselMelding: RefusjonVarselMelding,
        refusjonKontaktperson: RefusjonKontaktpersonEntitet,
        melding: ConsumerRecord<String, String>
    ) {
        val refusjonSak = nySakRefusjoner(refusjonKontaktperson, refusjonId)
        val notifikasjon = ArbeidsgiverRefusjonNotifikasjon(
            ulid(),
            arbeidsgivernotifikasjonJson = jacksonMapper().writeValueAsString(refusjonSak),
            type = ArbeidsgivernotifikasjonType.Sak,
            status = ArbeidsgivernotifikasjonStatus.SAK_MOTTATT,
            bedriftNr = refusjonKontaktperson.bedriftNr,
            varslingsformål = refusjonVarselMelding.refusjonVarselType.tilVarslingsformål(),
            avtaleId = refusjonVarselMelding.avtaleId.toString(),
            refusjonId = refusjonId,
            kafkaOffset = melding.offset(),
            kafkaKey = melding.key(),
        )
        arbeidsgiverRefusjonNotifikasjonRepository.save(notifikasjon)
        runBlocking {
            val response = notifikasjonGraphQlClient.execute(refusjonSak)
            val resultat = response.data?.nySak
            if (resultat is NySakVellykket) {
                settVellykket(notifikasjon, resultat.id, "refusjon-sak")
            } else {
                settFeilet(notifikasjon, response, "refusjon-sak")
            }
        }
    }

    private fun settVellykket(notifikasjon: ArbeidsgiverRefusjonNotifikasjon, responseId: String, beskrivelse: String) {
        log.info("AG: $beskrivelse opprettet vellykket. avtaleId: ${notifikasjon.avtaleId}")
        notifikasjon.responseId = responseId
        notifikasjon.sendtTidspunkt = Instant.now()
        arbeidsgiverRefusjonNotifikasjonRepository.save(notifikasjon)
    }

    private fun settFeilet(notifikasjon: ArbeidsgiverRefusjonNotifikasjon, response: GraphQLClientResponse<*>, beskrivelse: String) {
        if (response.errors != null) {
            log.error("AG: GraphQl-kall for å opprette $beskrivelse feilet: ${response.errors}")
            notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING
            notifikasjon.feilmelding = response.errors.toString()
        } else {
            // UgyldigMerkelapp | UgyldigMottaker | DuplikatGrupperingsid | DuplikatGrupperingsidEtterDelete | UkjentProdusent | UkjentRolle
            log.error("AG: opprett $beskrivelse gikk ikke med resultatet: ${response.data}")
            notifikasjon.feilmelding = response.data.toString()
            notifikasjon.status = ArbeidsgivernotifikasjonStatus.FEILET_VED_OPPRETTELSE_HOS_FAGER
        }
        arbeidsgiverRefusjonNotifikasjonRepository.save(notifikasjon)
    }
}
