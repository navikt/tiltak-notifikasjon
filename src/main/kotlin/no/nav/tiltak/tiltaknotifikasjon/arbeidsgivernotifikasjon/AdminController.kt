package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.MineNotifikasjoner
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.NotifikasjonConnection
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
import no.nav.tiltak.tiltaknotifikasjon.kafka.RefusjonVarselConsumer
import no.nav.tiltak.tiltaknotifikasjon.kafka.Topics
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
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

    @PostMapping("rekjor-feilet-arbeidsgivermelding")
    fun rekjorFeiletArbeidsgivernotifikasjon(@RequestBody idRequest: IdRequest): ResponseEntity<String> {
        val feiletRad = arbeidsgiverNotifikasjonService.arbeidsgivernotifikasjonRepository.findById(idRequest.id)
            ?: return ResponseEntity.notFound().build()

        val melding: AvtaleHendelseMelding = jacksonMapper().readValue(feiletRad.avtaleMeldingJson)
        val meldingerForAvtale =
            arbeidsgiverNotifikasjonService.arbeidsgivernotifikasjonRepository.findAllbyAvtaleId(melding.avtaleId.toString())
                .filterNot { it.status == ArbeidsgivernotifikasjonStatus.FEILET_VED_BEHANDLING }
                .map {
                    jacksonMapper().readValue<AvtaleHendelseMelding>(it.avtaleMeldingJson)
                }

        val alleredeBehandletMelding = meldingerForAvtale.any { it.sistEndret.equals(melding.sistEndret) }
        if (alleredeBehandletMelding) {
            return ResponseEntity.badRequest().body("Har allerede rebehandlet denne meldingen")
        }

        val harNyereMelding = meldingerForAvtale.any { it.sistEndret.isAfter(melding.sistEndret) }

        if (harNyereMelding) {
            return ResponseEntity.badRequest().body("Fant nyere melding for avtaleId: ${melding.avtaleId}")
        }

        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(melding)

        return ResponseEntity.ok().build()
    }

    @PostMapping("rekjor-feilet-refusjonsvarsel")
    fun rekjorFeiletRefusjonsvarsel(@RequestBody refusjonNotifikasjonRequest: IdRequest): ResponseEntity<Map<String, Any>> {
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

        return ResponseEntity.ok().body(mapOf("rekjort" to refusjonNotifikasjonRequest.id))
    }
}

data class MineSaker(val avtaleId: String, val tiltakstype: Tiltakstype)

data class IdRequest(
    val id: String
)
