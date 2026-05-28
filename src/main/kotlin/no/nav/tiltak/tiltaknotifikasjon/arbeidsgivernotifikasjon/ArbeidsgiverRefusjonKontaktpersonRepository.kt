package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.ArbeidsgiverRefusjonKontaktperson.Companion.ARBEIDSGIVER_REFUSJON_KONTAKTPERSON
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.records.ArbeidsgiverRefusjonKontaktpersonRecord
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneOffset

@Component
class ArbeidsgiverRefusjonKontaktpersonRepository(val dsl: DSLContext) {

    @Transactional
    fun save(refusjonKontaktperson: RefusjonKontaktpersonEntitet) {
        val idPåEksisterendeEntitet = finnIdPåEksisterende(refusjonKontaktperson)

        val record = ArbeidsgiverRefusjonKontaktpersonRecord(
            id = idPåEksisterendeEntitet ?: ulid(),
            avtaleId = refusjonKontaktperson.avtaleId,
            refusjonKontaktpersonTlf = refusjonKontaktperson.refusjonKontaktpersonTlf,
            arbeidsgiverOnskerOgsaVarsling = refusjonKontaktperson.arbeidsgiverOnskerOgsaVarsling,
            arbeidsgiverTlf = refusjonKontaktperson.arbeidsgiverTlf,
            tiltakstype = refusjonKontaktperson.tiltakstype.name,
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
            .set(record)
            .execute()
    }

    fun findAll(): List<RefusjonKontaktpersonEntitet> {
        return dsl
            .selectFrom(ARBEIDSGIVER_REFUSJON_KONTAKTPERSON)
            .fetch()
            .map { mapToEntitet(it) }
    }

    private fun mapToEntitet(record: ArbeidsgiverRefusjonKontaktpersonRecord) = RefusjonKontaktpersonEntitet(
        avtaleId = record.avtaleId!!,
        refusjonKontaktpersonTlf = record.refusjonKontaktpersonTlf,
        arbeidsgiverOnskerOgsaVarsling = record.arbeidsgiverOnskerOgsaVarsling,
        arbeidsgiverTlf = record.arbeidsgiverTlf,
        tiltakstype = Tiltakstype.valueOf(record.tiltakstype!!),
        avtaleInnholdVersjon = record.avtaleInnholdVersjon!!,
        avtaleHendelseType = HendelseType.valueOf(record.avtaleHendelseType!!),
        avtaleHendelseSistEndret = record.avtaleHendelseSistEndret!!.toInstant(),
        topicOffset = record.topicOffset!!,
        innlestTidspunkt = record.innlestTidspunkt!!.toInstant(),
    )

    private fun finnIdPåEksisterende(entitet: RefusjonKontaktpersonEntitet): String? {
        return dsl
            .selectFrom(ARBEIDSGIVER_REFUSJON_KONTAKTPERSON)
            .where(ARBEIDSGIVER_REFUSJON_KONTAKTPERSON.AVTALE_ID.eq(entitet.avtaleId))
            .forUpdate()
            .fetchOne()
            ?.id
    }
}
