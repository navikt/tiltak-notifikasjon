package no.nav.tiltak.tiltaknotifikasjon.avtale

enum class Tiltakstype(val beskrivelse: String, val arbeidsgiverNotifikasjonMerkelapp: String, val ressursId : String) {
    ARBEIDSTRENING(beskrivelse = "Arbeidstrening", arbeidsgiverNotifikasjonMerkelapp = "Arbeidstrening", ressursId = "nav_tiltak_arbeidstrening"),
    MIDLERTIDIG_LONNSTILSKUDD(beskrivelse = "Midlertidig lønnstilskudd", arbeidsgiverNotifikasjonMerkelapp = "Lønnstilskudd", ressursId = "nav_tiltak_midlertidig-lonnstilskudd"),
    VARIG_LONNSTILSKUDD(beskrivelse = "Varig lønnstilskudd", arbeidsgiverNotifikasjonMerkelapp = "Lønnstilskudd", ressursId = "nav_tiltak_varig-lonnstilskudd"),
    MENTOR(beskrivelse = "Mentor", arbeidsgiverNotifikasjonMerkelapp = "Mentor", ressursId = "nav_tiltak_mentor"),
    INKLUDERINGSTILSKUDD(beskrivelse = "Inkluderingstilskudd", arbeidsgiverNotifikasjonMerkelapp = "Inkluderingstilskudd", ressursId = "nav_tiltak_inkluderingstilskudd"),
    SOMMERJOBB(beskrivelse = "Sommerjobb", arbeidsgiverNotifikasjonMerkelapp = "Sommerjobb", ressursId = "nav_tiltak_sommerjobb"),
    VTAO(beskrivelse = "Varig tilrettelagt arbeid i ordinær virksomhet", arbeidsgiverNotifikasjonMerkelapp = "Varig tilrettelagt arbeid", ressursId = "nav_tiltak_varig-tilrettelagt-arbeid-ordinaer"),
    FIREARIG_LONNSTILSKUDD(beskrivelse ="Fireårig lønnstilskudd for unge", arbeidsgiverNotifikasjonMerkelapp = "Lønnstilskudd" , ressursId = "nav_tiltak_firearig-lonnstilskudd-for-unge"),;
}

fun Tiltakstype.altinnRessursId(): String {
    return when (this) {
        Tiltakstype.ARBEIDSTRENING -> this.ressursId
        Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD -> this.ressursId
        Tiltakstype.VARIG_LONNSTILSKUDD -> this.ressursId
        Tiltakstype.SOMMERJOBB -> this.ressursId
        Tiltakstype.MENTOR -> this.ressursId
        Tiltakstype.INKLUDERINGSTILSKUDD -> this.ressursId
        Tiltakstype.VTAO -> this.ressursId
        Tiltakstype.FIREARIG_LONNSTILSKUDD -> this.ressursId
    }
}


