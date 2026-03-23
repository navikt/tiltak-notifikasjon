package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "tiltak-notifikasjon.altinn-tilgangstyring")
data class AltinnProperties(
    var arbeidstrening: String = "",
    var midlertidigLonnstilskudd: String = "",
    var varigLonnstilskudd: String = "",
    var sommerjobb: String = "",
    var mentor: String = "",
    var inkluderingstilskudd: String = "",
    var vtao : String= "",
    var firearigLonnstilskudd: String = ""
    )






//@Component
//@ConfigurationProperties(prefix = "tiltak-refusjon.norg")
//data class NorgProperties(
//    var uri: String = ""
//)

