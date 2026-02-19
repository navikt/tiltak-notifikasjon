package no.nav.tiltak.tiltaknotifikasjon.persondata

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.team_tiltak.felles.persondata.PersondataClient
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.log
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("prod-gcp", "dev-gcp", "dockercompose")
class PersondataService(
    clientConfigurationProperties: ClientConfigurationProperties,
    persondataProperties: PersondataProperties,
    val oAuth2AccessTokenService: OAuth2AccessTokenService,
) {
    private val clientProperties = clientConfigurationProperties.registration["pdl-api"]

    private val persondataClient =
        PersondataClient(persondataProperties.uri) { clientProperties?.let { oAuth2AccessTokenService.getAccessToken(it).access_token } }

    fun hentDiskresjonskode(fnr: String): Diskresjonskode {
        repeat(3) {
            try {
                val diskresjonskoder = persondataClient.hentDiskresjonskoder(setOf(fnr))
                val kode = diskresjonskoder[fnr]?.get()
                if (kode != null) {
                    return kode
                } else {
                    log.warn("Fant ingen diskresjonskoder")
                    return Diskresjonskode.UGRADERT
                }
            } catch (e: Exception) {
                log.error("Fant ikke diskresjonskode på forsøk ${it + 1} av 3", e)
                if (it == 2) throw e // Rethrow the exception on the last attempt
            }
        }
        throw IllegalStateException("Uventet feil") // This should never be reached
    }

}
