package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.MineNotifikasjoner
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.NotifikasjonConnection
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*


@ProtectedWithClaims(issuer = "azure-access-token", claimMap = ["groups=fb516b74-0f2e-4b62-bad8-d70b82c3ae0b"])
@RestController
@RequestMapping("/internal/admin")
class AdminController(
    private val arbeidsgiverNotifikasjonService: ArbeidsgiverNotifikasjonService,
    private val arbeidsgivernotifikasjonRepository: ArbeidsgivernotifikasjonRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)


    @GetMapping("hent-mine-saker")
    fun hentMineSaker(@RequestBody mineSaker: MineSaker): GraphQLClientResponse<MineNotifikasjoner.Result> {
        log.info("Henter mine saker")
        val mineSakerResponse = arbeidsgiverNotifikasjonService.hentMineSaker(avtaleId = mineSaker.avtaleId, tiltakstype = mineSaker.tiltakstype)
        if (mineSakerResponse.data?.mineNotifikasjoner is NotifikasjonConnection) {
            val copy = mineSakerResponse.data!!.mineNotifikasjoner as NotifikasjonConnection
            copy.edges.forEach {
                log.info("Fant notifikasjon: ${it.node}")
            }
        }
        return mineSakerResponse
    }

    @PostMapping("rekjor-feilede-notifikasjoner")
    fun rekjorFeiledeNotifikasjoner(@RequestBody feilmeldingTilRekjoring: String) {
        log.info("Rekjører feilede notifikasjoner som feilet med feilmelding: $feilmeldingTilRekjoring")
        val feiledeNotifikasjoner = arbeidsgivernotifikasjonRepository.findAllByFeilmelding(feilmeldingTilRekjoring)
        if (feiledeNotifikasjoner.isEmpty()) {
            log.info("Ingen feilede notifikasjoner funnet for feilmelding: $feilmeldingTilRekjoring")
            return
        }
        log.info("Fant ${feiledeNotifikasjoner.size} feilede notifikasjoner med feilkode $feilmeldingTilRekjoring som skal rekjøres")
        feiledeNotifikasjoner.forEach { notifikasjon ->
            val melding: AvtaleHendelseMelding = jacksonMapper().readValue(notifikasjon.avtaleMeldingJson)
            arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(melding)
        }
    }

}

data class MineSaker(val avtaleId: String, val tiltakstype: Tiltakstype)
