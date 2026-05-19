package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.ArbeidsgiverRefusjonKontaktperson.Companion.ARBEIDSGIVER_REFUSJON_KONTAKTPERSON
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.records.ArbeidsgiverRefusjonKontaktpersonRecord
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
class ArbeidsgiverRefusjonKontaktpersonRepository(val dsl: DSLContext) {

    fun save(refusjonKontaktperson: RefusjonKontaktpersonEntitet) {
        val record = ArbeidsgiverRefusjonKontaktpersonRecord(
            id = ulid(),
            avtaleId = refusjonKontaktperson.avtaleId,
            refusjonKontaktpersonTlf = refusjonKontaktperson.refusjonKontaktpersonTlf,
            arbeidsgiverOnskerOgsaVarsling = refusjonKontaktperson.arbeidsgiverOnskerOgsaVarsling,
            avtaleInnholdVersjon = refusjonKontaktperson.avtaleInnholdVersjon,
            avtaleHendelseType = refusjonKontaktperson.avtaleHendelseType.name,
            avtaleHendelseSistEndret = refusjonKontaktperson.avtaleHendelseSistEndret.atOffset(ZoneOffset.UTC),
            topicOffset = refusjonKontaktperson.topicOffset,
            innlestTidspunkt = refusjonKontaktperson.innlestTidspunkt.atOffset(ZoneOffset.UTC),
        )
        dsl
            .insertInto(ARBEIDSGIVER_REFUSJON_KONTAKTPERSON)
            .set(record)
            .onConflict(ARBEIDSGIVER_REFUSJON_KONTAKTPERSON.AVTALE_ID)
            .doUpdate()
            // Oppdater alle felt unntatt id og avtale_id ved konflikt
            .set(record.apply {
                changed(ARBEIDSGIVER_REFUSJON_KONTAKTPERSON.ID, false)
                changed(ARBEIDSGIVER_REFUSJON_KONTAKTPERSON.AVTALE_ID, false)
            })
            .execute()
    }
}
