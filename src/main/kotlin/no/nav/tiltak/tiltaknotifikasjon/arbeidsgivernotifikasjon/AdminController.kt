package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.types.GraphQLClientResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.MineNotifikasjoner
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.NotifikasjonConnection
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
import no.nav.tiltak.tiltaknotifikasjon.kafka.RefusjonVarselConsumer
import no.nav.tiltak.tiltaknotifikasjon.kafka.Topics
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Duration


@ProtectedWithClaims(issuer = "azure-access-token", claimMap = ["groups=fb516b74-0f2e-4b62-bad8-d70b82c3ae0b"])
@Profile("prod-gcp", "dev-gcp")
@RestController
@RequestMapping("/internal/admin")
class AdminController(
    val arbeidsgiverNotifikasjonService: ArbeidsgiverNotifikasjonService,
    val arbeidsgiverRefusjonNotifikasjonRepository: ArbeidsgiverRefusjonNotifikasjonRepository,
    val arbeidsgiverRefusjonVarselConsumer: RefusjonVarselConsumer,
    val seekingKafkaConsumer: KafkaConsumer<String, String>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("hent-mine-saker")
    fun hentMineSaker(@RequestBody mineSaker: MineSaker): GraphQLClientResponse<MineNotifikasjoner.Result> {
        log.info("Henter mine saker")
        val mineSakerResponse = arbeidsgiverNotifikasjonService.hentMineSaker(
            avtaleId = mineSaker.avtaleId,
            tiltakstype = mineSaker.tiltakstype
        )
        if (mineSakerResponse.data?.mineNotifikasjoner is NotifikasjonConnection) {
            val copy = mineSakerResponse.data!!.mineNotifikasjoner as NotifikasjonConnection
            copy.edges.forEach {
                log.info("Fant notifikasjon: ${it.node}")
            }
        }
        return mineSakerResponse
    }

    @PostMapping("rekjor-feilet-refusjonsvarsel")
    fun rekjorFeiletRefusjonsvarsel(@RequestBody refusjonNotifikasjonRequest: RefusjonNotifikasjonRequest): ResponseEntity<Map<String, Any>> {
        val rad = arbeidsgiverRefusjonNotifikasjonRepository.findById(refusjonNotifikasjonRequest.id)
            ?: return ResponseEntity.notFound().build()
        if (rad.status != ArbeidsgivernotifikasjonStatus.FEILET_VED_BEHANDLING && rad.status != ArbeidsgivernotifikasjonStatus.FEILET_VED_SENDING) {
            return ResponseEntity.badRequest().body(
                mapOf("feilmelding" to "Notifikasjon har ugyldig status: ${rad.status}")
            )
        }

        arbeidsgiverRefusjonVarselConsumer.konsumer(
            ConsumerRecord(
                Topics.TILTAK_VARSEL,
                0,
                rad.kafkaOffset,
                rad.kafkaKey,
                rad.arbeidsgivernotifikasjonJson
            )
        )

        val antallSlettet = arbeidsgiverRefusjonNotifikasjonRepository.deleteById(rad.id)
        if (antallSlettet != 1) {
            log.warn("Uventet feil ved sletting av melding; forventet å slette en rad, men slettet $antallSlettet")
        }

        return ResponseEntity.ok().body(mapOf("rekjort" to refusjonNotifikasjonRequest.id))
    }

    @PostMapping("les-kafka-melding")
    fun lesKafkaMelding(@RequestBody lesKafkaMeldingRequest: LesRefusjonKafkaMeldingRequest): ResponseEntity<Map<String, Any>> {
        val offset = lesKafkaMeldingRequest.offsetNr
        val partition = TopicPartition(Topics.TILTAK_VARSEL, 0)

        // seekingKafkaClient er en egen KafkaConsumer med egen consumer group-id (se KafkaAdminConfig),
        // brukt manuelt via assign+seek, slik at oppslag her ikke påvirker offsettene til de vanlige @KafkaListener-consumerne.
        val record: ConsumerRecord<String, String>? = synchronized(seekingKafkaConsumer) {
            seekingKafkaConsumer.assign(listOf(partition))
            seekingKafkaConsumer.seek(partition, offset)
            val records = seekingKafkaConsumer.poll(Duration.ofSeconds(15))
            records.firstOrNull { it.offset() == offset }
        }

        if (record == null) {
            log.warn("Fant ingen kafkamelding på topic ${Topics.TILTAK_VARSEL} offset $offset")
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok().body(
            mapOf(
                "key" to record.key(),
                "offset" to offset,
                "message" to record.value()
            )
        )
    }
}

data class MineSaker(val avtaleId: String, val tiltakstype: Tiltakstype)

data class RefusjonNotifikasjonRequest(
    val id: String
)

data class LesRefusjonKafkaMeldingRequest(
    val offsetNr: Long
)
