package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AvtaleHendelseLås(private val dsl: DSLContext) {
    @Transactional
    fun <T> medAvtaleLås(avtaleId: String, behandling: () -> T): T {
        dsl.query(
            "select pg_advisory_xact_lock(hashtextextended(?, 0))",
            "tiltak-notifikasjon:arbeidsgivernotifikasjon:$avtaleId"
        ).execute()
        return behandling()
    }
}
