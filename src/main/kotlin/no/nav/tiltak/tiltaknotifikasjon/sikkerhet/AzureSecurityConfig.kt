package no.nav.tiltak.tiltaknotifikasjon.sikkerhet

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient

@EnableOAuth2Client(cacheEnabled = true)
@Configuration
@Profile("dev-gcp")
class AzureSecurityConfig {

    @Bean
    fun azureWebClientBuilder(
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService
    ): WebClient.Builder {
        val clientProperties = clientConfigurationProperties.registration["notifikasjoner"]
            ?: throw RuntimeException("could not find oauth2 client config for aad")
        return WebClient.builder().filter { request, next ->
            try {
                val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
                request.headers().setBearerAuth(response.accessToken!!)
                next.exchange(request)
            } catch (e: Exception) {
                throw RuntimeException("Failed to get access token", e)
            }
        }
    }

}