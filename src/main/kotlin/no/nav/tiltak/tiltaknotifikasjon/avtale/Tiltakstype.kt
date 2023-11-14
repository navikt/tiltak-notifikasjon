package no.nav.tiltak.tiltaknotifikasjon.avtale

enum class Tiltakstype(val beskrivelse: String, val skalTilAktivitetsplan: Boolean) {
    ARBEIDSTRENING("Arbeidstrening", false),
    MIDLERTIDIG_LONNSTILSKUDD("Midlertidig lønnstilskudd", true),
    VARIG_LONNSTILSKUDD("Varig lønnstilskudd", true),
    MENTOR("Mentor", false),
    INKLUDERINGSTILSKUDD("Inkluderingstilskudd", false),
    SOMMERJOBB("Sommerjobb", false);
}