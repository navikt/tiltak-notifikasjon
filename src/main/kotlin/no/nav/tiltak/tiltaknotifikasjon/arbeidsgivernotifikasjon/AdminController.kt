package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.types.GraphQLClientResponse
import no.nav.security.token.support.core.api.Unprotected
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.MineNotifikasjoner
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.minenotifikasjoner.NotifikasjonConnection
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@Profile("dev-gcp")
@Unprotected
@RestController
@RequestMapping("/internal/admin")
class AdminController(val arbeidsgiverNotifikasjonService: ArbeidsgiverNotifikasjonService) {
    private val log = LoggerFactory.getLogger(javaClass)

    //@Unprotected
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


}

data class MineSaker(val avtaleId: String, val tiltakstype: Tiltakstype)