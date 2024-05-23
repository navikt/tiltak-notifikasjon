package no.nav.tiltak.tiltaknotifikasjon.sikkerhet

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient

@EnableOAuth2Client(cacheEnabled = true)
@EnableJwtTokenValidation
@Configuration
@Profile("dev-gcp")
class AzureSecurityConfig {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun azureWebClientBuilder(
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService
    ): WebClient.Builder {
        val clientProperties = clientConfigurationProperties.registration["notifikasjoner"]
            ?: return WebClient.builder()// throw RuntimeException("could not find oauth2 client config for aad")
        return WebClient.builder().filter { request, next ->
            try {
                val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
                log.info("Got response from oAuth2AccessTokenService.getAccessToken: $response")
                //request.headers().setBearerAuth(response.accessToken!!)
                request.headers().setBearerAuth(response.access_token!!)
                next.exchange(request)
            } catch (e: Exception) {
                throw RuntimeException("Failed to get access token", e)
            }
        }
    }
}