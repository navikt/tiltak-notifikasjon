package no.nav.tiltak.tiltaknotifikasjon.avtale

import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.AltinnProperties

enum class Tiltakstype(val beskrivelse: String, val arbeidsgiverNotifikasjonMerkelapp: String) {
    ARBEIDSTRENING(beskrivelse = "Arbeidstrening", arbeidsgiverNotifikasjonMerkelapp = "Arbeidstrening"),
    MIDLERTIDIG_LONNSTILSKUDD(beskrivelse = "Midlertidig lønnstilskudd", arbeidsgiverNotifikasjonMerkelapp = "Lønnstilskudd"),
    VARIG_LONNSTILSKUDD(beskrivelse = "Varig lønnstilskudd", arbeidsgiverNotifikasjonMerkelapp = "Lønnstilskudd"),
    MENTOR(beskrivelse = "Mentor", arbeidsgiverNotifikasjonMerkelapp = "Mentor"),
    INKLUDERINGSTILSKUDD(beskrivelse = "Inkluderingstilskudd", arbeidsgiverNotifikasjonMerkelapp = "Inkluderingstilskudd"),
    SOMMERJOBB(beskrivelse = "Sommerjobb", arbeidsgiverNotifikasjonMerkelapp = "Sommerjobb"),
    VTAO(beskrivelse = "Varig tilrettelagt arbeid i ordinær virksomhet", arbeidsgiverNotifikasjonMerkelapp = "Varig tilrettelagt arbeid"),
    FIREARIG_LONNSTILSKUDD(beskrivelse ="Fireårig lønnstilskudd for unge", arbeidsgiverNotifikasjonMerkelapp = "Lønnstilskudd");
}

fun Tiltakstype.ressurser(altinnProperties: AltinnProperties): String {
    return when (this) {
        Tiltakstype.ARBEIDSTRENING -> altinnProperties.arbeidstrening
        Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD -> altinnProperties.midlertidigLonnstilskudd
        Tiltakstype.VARIG_LONNSTILSKUDD -> altinnProperties.varigLonnstilskudd
        Tiltakstype.SOMMERJOBB -> altinnProperties.sommerjobb
        Tiltakstype.MENTOR -> altinnProperties.mentor
        Tiltakstype.INKLUDERINGSTILSKUDD -> altinnProperties.inkluderingstilskudd
        Tiltakstype.VTAO -> altinnProperties.vtao
        Tiltakstype.FIREARIG_LONNSTILSKUDD -> altinnProperties.firearigLonnstilskudd
    }
}


