package no.nav.tiltak.tiltaknotifikasjon.avtale

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

fun Tiltakstype.altinnRessursId(): String {
    return when (this) {
        Tiltakstype.ARBEIDSTRENING -> "nav_tiltak_arbeidstrening"
        Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD -> "nav_tiltak_midlertidig-lonnstilskudd"
        Tiltakstype.VARIG_LONNSTILSKUDD -> "nav_tiltak_varig-lonnstilskudd"
        Tiltakstype.SOMMERJOBB -> "nav_tiltak_sommerjobb"
        Tiltakstype.MENTOR -> "nav_tiltak_mentor"
        Tiltakstype.INKLUDERINGSTILSKUDD -> "nav_tiltak_inkluderingstilskudd"
        Tiltakstype.VTAO -> "nav_tiltak_varig-tilrettelagt-arbeid-ordinaer"
        Tiltakstype.FIREARIG_LONNSTILSKUDD -> "nav_tiltak_firearig-lonnstilskudd-for-unge"
    }
}


