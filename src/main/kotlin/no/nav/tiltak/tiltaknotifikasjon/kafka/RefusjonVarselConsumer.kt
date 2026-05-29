package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgiverRefusjonKontaktpersonRepository
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.nySak
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@Profile("prod-gcp", "dev-gcp", "dockercompose")
class RefusjonVarselConsumer(private val arbeidsgiverRefusjonKontaktpersonRepository: ArbeidsgiverRefusjonKontaktpersonRepository) {
    private val mapper = jacksonMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [Topics.TILTAK_VARSEL])
    fun konsumer(melding: ConsumerRecord<String, String>) {
        try {
            val refusjonVarselMelding: RefusjonVarselMelding = mapper.readValue(melding.value())
            // TODO: Implementer logikk for å sende SMS basert på varselType
            // Slå opp kontaktperson fra arbeidsgiver_refusjon_kontaktperson-tabellen
            val refusjonKontaktperson = arbeidsgiverRefusjonKontaktpersonRepository.findByAvtaleId(refusjonVarselMelding.avtaleId)
            if (refusjonKontaktperson != null) {
                // Vi vil lage en sak for alle refusjoner på en avtale. GrupperingsID kan være noe sånt som avtaleId-refusjon,
                // og da vil alle varsler for refusjoner på samme avtale grupperes i samme sak, og kan styres på egen tilgang (Tiltaksrefusjon).
                // Vi må først finne ut om vi skal opprette en sak eller ikke. Vi kan forsøke å opprette og se om vi får DuplikatSak Feilmelding.
                val sak = nySak(
                    avtaleId = refusjonVarselMelding.avtaleId,
                    tiltakstype = refusjonKontaktperson.tiltakstype,
                    bedriftsnummer = refusjonKontaktperson.,
                    varselType = refusjonVarselMelding.varselType,
                    fristForGodkjenning = refusjonVarselMelding.fristForGodkjenning,
                ))



                // Når vi skal opprette en sak så må vi oppgi avtaleId (grupperingsid), men vi trenger også tiltakstype og bedriftsnummer.
                // Dette er ikke informasjon som kommer på denne topicen, men! Vi kan vel anta at alle refusjon_klar varsler har en tidligere Sak knyttet til avtalen.
                // Jeg tror den mest fornuftige løsningen her er å utvide TILTAK_VARSEL topicen til å inkludere litt mer informasjon.
                // Det er kun tiltaksgjennomforing-api som lytter på denne topicen i dag og den skal ikke feile på ukjente properties. Da har vi en god plan!!

            }


            log.info(
                "Mottok refusjon varsel for avtale ${refusjonVarselMelding.avtaleId}, " +
                        "type ${refusjonVarselMelding.varselType}, " +
                        "offset ${melding.offset()}"
            )
        } catch (e: Exception) {
            log.error(
                "Feil ved konsumering av refusjon varsel. Skipper melding fra topic ${melding.topic()}, " +
                        "partition ${melding.partition()}, offset ${melding.offset()}",
                e,
            )
        }
    }
}
