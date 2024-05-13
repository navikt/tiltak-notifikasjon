package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "tiltak-notifikasjon.altinn-tilgangstyring")
data class AltinnProperties(
    var arbtreningServiceCode:String = "",
    var arbtreningServiceEdition:String = "",
    var ltsMidlertidigServiceCode:String = "",
    var ltsMidlertidigServiceEdition:String = "",
    var ltsVarigServiceCode:String = "",
    var ltsVarigServiceEdition:String = "",
    var sommerjobbServiceCode:String = "",
    var sommerjobbServiceEdition:String = "",
    var mentorServiceCode:String = "",
    var mentorServiceEdition:String = "",
    var inkluderingstilskuddServiceCode:String = "",
    var inkluderingstilskuddServiceEdition:String = "",
    )






//@Component
//@ConfigurationProperties(prefix = "tiltak-refusjon.norg")
//data class NorgProperties(
//    var uri: String = ""
//)

