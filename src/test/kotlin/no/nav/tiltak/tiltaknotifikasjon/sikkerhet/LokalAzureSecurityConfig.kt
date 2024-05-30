package no.nav.tiltak.tiltaknotifikasjon.sikkerhet


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class LokalAzureSecurityConfig {

    @Bean
    fun azureWebClientBuilder(): WebClient.Builder {
        return WebClient.builder()// throw RuntimeException("could not find oauth2 client config for aad")
    }
}