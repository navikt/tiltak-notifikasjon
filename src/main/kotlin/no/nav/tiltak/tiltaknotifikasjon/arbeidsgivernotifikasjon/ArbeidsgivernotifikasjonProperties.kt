package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "tiltak-notifikasjon.produsent-api")
data class ArbeidsgivernotifikasjonProperties(
    var url:String = ""
    )