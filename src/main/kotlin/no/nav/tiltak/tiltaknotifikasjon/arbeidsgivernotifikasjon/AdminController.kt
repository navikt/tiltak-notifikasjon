package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.types.GraphQLClientResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.MineNotifikasjoner
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.NotifikasjonConnection
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
import no.nav.tiltak.tiltaknotifikasjon.kafka.RefusjonVarselConsumer
import no.nav.tiltak.tiltaknotifikasjon.kafka.Topics
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@ProtectedWithClaims(issuer = "azure-access-token", claimMap = ["groups=fb516b74-0f2e-4b62-bad8-d70b82c3ae0b"])
@Profile("prod-gcp", "dev-gcp")
@RestController
@RequestMapping("/internal/admin")
class AdminController(
    val arbeidsgiverNotifikasjonService: ArbeidsgiverNotifikasjonService,
    val arbeidsgiverRefusjonNotifikasjonRepository: ArbeidsgiverRefusjonNotifikasjonRepository,
    val arbeidsgiverRefusjonVarselConsumer: RefusjonVarselConsumer
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
        if (rad.status != ArbeidsgivernotifikasjonStatus.FEILET_VED_BEHANDLING) {
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
}

data class MineSaker(val avtaleId: String, val tiltakstype: Tiltakstype)

data class RefusjonNotifikasjonRequest(
    val id: String
)
