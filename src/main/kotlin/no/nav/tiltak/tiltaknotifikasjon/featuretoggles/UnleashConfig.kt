package no.nav.tiltak.tiltaknotifikasjon.featuretoggles

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.context.annotation.RequestScope

@Configuration
@Profile("prod-gcp", "dev-gcp", "dockercompose")
class UnleashConfig {
    @Bean
    @Profile("prod-gcp", "dev-gcp")
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

    @Bean
    @RequestScope
    @Profile("dockercompose")
    fun unleashMock(@Autowired request: HttpServletRequest): Unleash {
        val fakeUnleash: FakeFakeUnleash = FakeFakeUnleash()
        val allEnabled = "enabled" == request.getHeader("features")
        if (allEnabled) {
            fakeUnleash.enableAll()
        } else {
            fakeUnleash.disableAll()
        }
        return fakeUnleash
    }

}