package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.fasterxml.jackson.module.kotlin.readValue
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

@Component
@Profile("prod-gcp", "dev-gcp", "dockercompose")
class RefusjonVarselConsumer(
    private val arbeidsgiverRefusjonKontaktpersonRepository: ArbeidsgiverRefusjonKontaktpersonRepository,
    private val arbeidsgiverRefusjonNotifikasjonRepository: ArbeidsgiverRefusjonNotifikasjonRepository,
    arbeidsgivernotifikasjonProperties: ArbeidsgivernotifikasjonProperties,
    @Qualifier("azureWebClientBuilder") azureWebClientBuilder: WebClient.Builder,
) {
    private val mapper = jacksonMapper()
    private val log = LoggerFactory.getLogger(javaClass)
    val notifikasjonGraphQlClient = GraphQLWebClient(arbeidsgivernotifikasjonProperties.url, builder = azureWebClientBuilder)

    @KafkaListener(topics = [Topics.TILTAK_VARSEL])
    fun konsumer(melding: ConsumerRecord<String, String>) {
        try {
            val refusjonVarselMelding: RefusjonVarselMelding = mapper.readValue(melding.value())
            if (refusjonVarselMelding.refusjonVarselType != RefusjonVarselType.KLAR) return
            val refusjonKontaktpersonEntitet = arbeidsgiverRefusjonKontaktpersonRepository.findByAvtaleId(refusjonVarselMelding.avtaleId)
            if (refusjonKontaktpersonEntitet == null) {
                log.warn("AG: Fant ingen refusjonkontaktperson for avtaleId ${refusjonVarselMelding.avtaleId} Skipper melding med offset ${melding.offset()}")
                return
            }

            val refusjonId = melding.key().split("-").first() // val meldingId = "${refusjonId}-$varselType" (tiltak-refusjon-api)
            if (!finnesSak(refusjonVarselMelding.avtaleId.toString(), refusjonKontaktpersonEntitet.tiltakstype)) {
                // Sak for alle refusjonene på avtalen
                opprettNySak(refusjonId, refusjonVarselMelding, refusjonKontaktpersonEntitet)
            }
            // Beskjed med sms om refusjon klar
            opprettNyBeskjed(refusjonVarselMelding, refusjonKontaktpersonEntitet, refusjonId)


        } catch (e: Exception) {
            log.error(
                "Feil ved konsumering av refusjon varsel. Skipper melding fra topic ${melding.topic()}, " +
                        "partition ${melding.partition()}, offset ${melding.offset()}",
                e,
            )
        }
    }

    fun finnesSak(avtaleId: String, tiltakstype: Tiltakstype): Boolean {
        var sakFinnes = false
        val sakQuery = nyHentSakQuery(avtaleId, tiltakstype.arbeidsgiverNotifikasjonMerkelapp)
        runBlocking {
            val response = notifikasjonGraphQlClient.execute(sakQuery)
            if (response.errors != null) {
                log.error("AG: GraphQl-kall for å hente refusjon-sak med grupperingsid feilet: ${response.errors}")
            }
            if (response.data?.hentSakMedGrupperingsid is HentetSak) {
                sakFinnes = true
            }
        }
        return sakFinnes
    }

    fun opprettNyBeskjed(refusjonVarselMelding: RefusjonVarselMelding, refusjonKontaktperson: RefusjonKontaktpersonEntitet, refusjonId: String) {
        val refusjonBeskjed = nyBeskjedRefusjoner(refusjonKontaktperson, refusjonId)
        val notifikasjon = ArbeidsgiverRefusjonNotifikasjon(
            ulid(),
            arbeidsgivernotifikasjonJson = jacksonMapper().writeValueAsString(refusjonBeskjed),
            type = ArbeidsgivernotifikasjonType.Beskjed,
            status = ArbeidsgivernotifikasjonStatus.BEHANDLET,
            bedriftNr = refusjonKontaktperson.bedriftNr,
            varslingsformål = refusjonVarselMelding.refusjonVarselType.fraRefusjonVarselType(),
            avtaleId = refusjonKontaktperson.avtaleId.toString(),
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

    fun opprettNySak(refusjonId: String, refusjonVarselMelding: RefusjonVarselMelding, refusjonKontaktperson: RefusjonKontaktpersonEntitet) {
        val refusjonSak = nySakRefusjoner(refusjonKontaktperson, refusjonId)
        val notifikasjon = ArbeidsgiverRefusjonNotifikasjon(
            ulid(),
            arbeidsgivernotifikasjonJson = jacksonMapper().writeValueAsString(refusjonSak),
            type = ArbeidsgivernotifikasjonType.Sak,
            status = ArbeidsgivernotifikasjonStatus.SAK_MOTTATT,
            bedriftNr = refusjonKontaktperson.bedriftNr,
            varslingsformål = refusjonVarselMelding.refusjonVarselType.fraRefusjonVarselType(),
            avtaleId = refusjonVarselMelding.avtaleId.toString(),
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
