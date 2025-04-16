package no.nav.tiltak.tiltaknotifikasjon.persondata

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "tiltak-notifikasjon.pdl-api")
data class PersondataProperties(
    var uri: String = "",
)
