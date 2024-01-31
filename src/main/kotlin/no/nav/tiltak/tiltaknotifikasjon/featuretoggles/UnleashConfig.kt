package no.nav.tiltak.tiltaknotifikasjon.featuretoggles

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("dev-gcp", "dockercompose")
class UnleashConfig {
    @Bean
    fun initializeUnleash(
        @Value("\${tiltak-notifikasjon.unleash.api-uri}") unleashUrl: String,
        @Value("\${tiltak-notifikasjon.unleash.api-token}") apiKey: String,
    ): Unleash {
        val APP_NAME = "tiltak-notifikasjon"
        val config = UnleashConfig.builder()
            .appName(APP_NAME)
            .instanceId(APP_NAME)
            .unleashAPI(unleashUrl)
            .apiKey(apiKey)
            .build()
        return DefaultUnleash(config)
    }
}