package no.nav.tiltak.tiltaknotifikasjon.avtale

import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.AltinnProperties

enum class Tiltakstype(val beskrivelse: String, val arbeidsgiverNotifikasjonMerkelapp: String) {
    ARBEIDSTRENING(beskrivelse = "Arbeidstrening", arbeidsgiverNotifikasjonMerkelapp = "Arbeidstrening"),
    MIDLERTIDIG_LONNSTILSKUDD(beskrivelse = "Midlertidig lønnstilskudd", arbeidsgiverNotifikasjonMerkelapp = "Lønnstilskudd"),
    VARIG_LONNSTILSKUDD(beskrivelse = "Varig lønnstilskudd", arbeidsgiverNotifikasjonMerkelapp = "Lønnstilskudd"),
    MENTOR(beskrivelse = "Mentor", arbeidsgiverNotifikasjonMerkelapp = "Mentor"),
    INKLUDERINGSTILSKUDD(beskrivelse = "Inkluderingstilskudd", arbeidsgiverNotifikasjonMerkelapp = "Inkluderingstilskudd"),
    SOMMERJOBB(beskrivelse = "Sommerjobb", arbeidsgiverNotifikasjonMerkelapp = "Sommerjobb");
}



fun Tiltakstype.serviceCode(altinnProperties: AltinnProperties): String {
    return when (this) {
        Tiltakstype.ARBEIDSTRENING -> altinnProperties.arbtreningServiceCode
        Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD -> altinnProperties.ltsMidlertidigServiceCode
        Tiltakstype.VARIG_LONNSTILSKUDD -> altinnProperties.ltsVarigServiceCode
        Tiltakstype.MENTOR -> altinnProperties.mentorServiceCode
        Tiltakstype.INKLUDERINGSTILSKUDD -> altinnProperties.inkluderingstilskuddServiceCode
        Tiltakstype.SOMMERJOBB -> altinnProperties.sommerjobbServiceCode
    }
}

fun Tiltakstype.serviceEdition(altinnProperties: AltinnProperties): String {
    return when (this) {
        Tiltakstype.ARBEIDSTRENING -> altinnProperties.arbtreningServiceEdition
        Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD -> altinnProperties.ltsMidlertidigServiceEdition
        Tiltakstype.VARIG_LONNSTILSKUDD -> altinnProperties.ltsVarigServiceEdition
        Tiltakstype.MENTOR -> altinnProperties.mentorServiceEdition
        Tiltakstype.INKLUDERINGSTILSKUDD -> altinnProperties.inkluderingstilskuddServiceEdition
        Tiltakstype.SOMMERJOBB -> altinnProperties.sommerjobbServiceEdition
    }
}