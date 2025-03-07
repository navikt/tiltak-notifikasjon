package no.nav.tiltak.tiltaknotifikasjon.kafka

object Topics {
    const val AVTALE_HENDELSE = "arbeidsgiver.tiltak-avtale-hendelse"
    const val AVTALE_HENDELSE_COMPACT = "arbeidsgiver.tiltak-avtale-hendelse-compact"
    const val BRUKERNOTIFIKASJON_BRUKERVARSEL = "min-side.aapen-brukervarsel-v1"
    const val BRUKERNOTIFIKASJON_HENDELSE = "min-side.aapen-varsel-hendelse-v1"
    const val NOTIFIKASJON_KVITTERING = "team-tiltak.tiltak-notifikasjon-kvittering"
}
