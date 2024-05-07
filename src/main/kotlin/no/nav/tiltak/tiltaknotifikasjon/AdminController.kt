package no.nav.tiltak.tiltaknotifikasjon

import com.expediagroup.graphql.client.types.GraphQLClientResponse
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgiverNotifikasjonService
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.MineNotifikasjoner
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@Profile("dev-gcp")
@RestController
@RequestMapping("/internal/admin")
class AdminController(val arbeidsgiverNotifikasjonService: ArbeidsgiverNotifikasjonService) {

    //@Unprotected
    @GetMapping("hent-mine-saker")
    suspend fun hentMineSaker(@RequestBody mineSaker: MineSaker): GraphQLClientResponse<MineNotifikasjoner.Result> {
        val mineSaker = arbeidsgiverNotifikasjonService.hentMineSaker(mineSaker.avtaleId, mineSaker.merkelapp)
        return mineSaker
    }


}

data class MineSaker(val avtaleId: String, val merkelapp: String)