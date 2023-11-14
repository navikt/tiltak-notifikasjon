package no.nav.tiltak.tiltaknotifikasjon.avtale

enum class AvtaleStatus(beskrivelse: String) {
    ANNULLERT("Annullert"),
    AVBRUTT("Avbrutt"),
    PÅBEGYNT("Påbegynt"),
    MANGLER_GODKJENNING("Mangler godkjenning"),
    KLAR_FOR_OPPSTART("Klar for oppstart"),
    GJENNOMFØRES("Gjennomføres"),
    AVSLUTTET("Avsluttet");
}