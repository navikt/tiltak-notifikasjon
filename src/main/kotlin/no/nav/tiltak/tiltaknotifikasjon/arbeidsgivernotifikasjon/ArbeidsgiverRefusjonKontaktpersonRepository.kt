package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.ArbeidsgiverRefusjonKontaktperson.Companion.ARBEIDSGIVER_REFUSJON_KONTAKTPERSON
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.records.ArbeidsgiverRefusjonKontaktpersonRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
class ArbeidsgiverRefusjonKontaktpersonRepository(val dsl: DSLContext) {

    fun save(refusjonKontaktperson: RefusjonKontaktperson) {
        val record = ArbeidsgiverRefusjonKontaktpersonRecord(
            id = refusjonKontaktperson.id,
            refusjonKontaktpersonTlf = refusjonKontaktperson.refusjonKontaktpersonTlf,
            arbeidsgiverOnskerOgsaVarsling = refusjonKontaktperson.arbeidsgiverOnskerOgsaVarsling,
            avtaleId = refusjonKontaktperson.avtaleId,
            avtaleInnholdVersjon = refusjonKontaktperson.avtaleInnholdVersjon,
            avtaleHendelseType = refusjonKontaktperson.avtaleHendelseType.name,
            avtaleHendelseSistEndret = refusjonKontaktperson.avtaleHendelseSistEndret.atOffset(ZoneOffset.UTC),
            topicOffset = refusjonKontaktperson.topicOffset,
            innlestTidspunkt = refusjonKontaktperson.innlestTidspunkt.atOffset(ZoneOffset.UTC),
        )
        dsl
            .insertInto(ARBEIDSGIVER_REFUSJON_KONTAKTPERSON)
            .set(record)
            .onDuplicateKeyUpdate()
            .set(record)
            .execute()
    }
}
