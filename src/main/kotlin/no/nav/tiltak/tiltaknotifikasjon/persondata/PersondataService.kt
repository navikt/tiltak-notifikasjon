package no.nav.tiltak.tiltaknotifikasjon.persondata

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.team_tiltak.felles.persondata.PersondataClient
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode
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
        return persondataClient.hentDiskresjonskode(fnr).orElse(Diskresjonskode.UGRADERT)
    }

    // TRENGER VEL IKKE DENNE
    fun hentDiskresjonskoder(fnrSet: Set<String>): Map<String, Diskresjonskode> {
        return persondataClient.hentDiskresjonskoderEllerDefault(
            fnrSet.map { it }.toSet(),
            { fnr: String -> fnr },
            Diskresjonskode.UGRADERT
        )
    }

}
