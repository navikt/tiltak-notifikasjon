package no.nav.tiltak.tiltaknotifikasjon.avtale

enum class Avtalerolle {
    DELTAKER, MENTOR, ARBEIDSGIVER, VEILEDER, BESLUTTER;

    fun erInternBruker(): Boolean {
        return this == Avtalerolle.VEILEDER || this == Avtalerolle.BESLUTTER
    }

    fun erBeslutter(): Boolean {
        return this == Avtalerolle.BESLUTTER
    }
}
