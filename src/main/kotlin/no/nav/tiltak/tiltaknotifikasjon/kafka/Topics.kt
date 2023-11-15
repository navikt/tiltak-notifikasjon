package no.nav.tiltak.tiltaknotifikasjon.kafka

object Topics {
    const val AVTALE_HENDELSE = "arbeidsgiver.tiltak-avtale-hendelse"
    const val AVTALE_HENDELSE_COMPACT = "arbeidsgiver.tiltak-avtale-hendelse-compact"
    const val BRUKERNOTIFIKASJON_BESKJED = "aapen-brukernotifikasjon-beskjed"
    const val BRUKERNOTIFIKASJON_OPPGAVE = "aapen-brukernotifikasjon-oppgave"
    const val BRUKERNOTIFIKASJON_DONE = "aapen-brukernotifikasjon-done"
}