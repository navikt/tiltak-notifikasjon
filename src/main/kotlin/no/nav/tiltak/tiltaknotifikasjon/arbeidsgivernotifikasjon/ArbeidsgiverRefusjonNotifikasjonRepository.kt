package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.ArbeidsgiverRefusjonNotifikasjon.Companion.ARBEIDSGIVER_REFUSJON_NOTIFIKASJON
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.records.ArbeidsgiverRefusjonNotifikasjonRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
class ArbeidsgiverRefusjonNotifikasjonRepository(val dsl: DSLContext) {

    fun save(arbeidsgiverRefusjonNotifikasjon: ArbeidsgiverRefusjonNotifikasjon) {
        val record = mapToDatabaseRecord(arbeidsgiverRefusjonNotifikasjon)
        dsl
            .insertInto(ARBEIDSGIVER_REFUSJON_NOTIFIKASJON)
            .set(record)
            .onDuplicateKeyUpdate()
            .set(record)
            .execute()
    }

    fun findById(id: String): ArbeidsgiverRefusjonNotifikasjon? {
        return dsl
            .selectFrom(ARBEIDSGIVER_REFUSJON_NOTIFIKASJON)
            .where(ARBEIDSGIVER_REFUSJON_NOTIFIKASJON.ID.eq(id))
            .fetchOne()
            ?.let { mapToArbeidsgiverRefusjonNotifikasjon(it) }
    }

    fun findAll(): List<ArbeidsgiverRefusjonNotifikasjon> {
        return dsl
            .selectFrom(ARBEIDSGIVER_REFUSJON_NOTIFIKASJON)
            .fetch()
            .map { mapToArbeidsgiverRefusjonNotifikasjon(it) }
    }

    private fun mapToArbeidsgiverRefusjonNotifikasjon(record: ArbeidsgiverRefusjonNotifikasjonRecord) =
        ArbeidsgiverRefusjonNotifikasjon(
            id = record.id,
            arbeidsgivernotifikasjonJson = record.arbeidsgivernotifikasjonJson,
            type = ArbeidsgivernotifikasjonType.valueOf(record.type),
            status = ArbeidsgivernotifikasjonStatus.valueOf(record.status),
            bedriftNr = record.bedriftNr,
            feilmelding = record.feilmelding,
            sendtTidspunkt = record.sendtTidspunkt?.toInstant(),
            opprettetTidspunkt = record.opprettetTidspunkt.toInstant(),
            varslingsformål = Varslingsformål.valueOf(record.varslingsformål),
            avtaleId = record.avtaleId,
            responseId = record.responseId,
            hardDeleteSkedulertTidspunkt = record.hardDeleteSkedulertTidspunkt,
        )

    private fun mapToDatabaseRecord(arbeidsgiverRefusjonNotifikasjon: ArbeidsgiverRefusjonNotifikasjon) =
        ArbeidsgiverRefusjonNotifikasjonRecord(
            id = arbeidsgiverRefusjonNotifikasjon.id,
            arbeidsgivernotifikasjonJson = arbeidsgiverRefusjonNotifikasjon.arbeidsgivernotifikasjonJson,
            type = arbeidsgiverRefusjonNotifikasjon.type.name,
            status = arbeidsgiverRefusjonNotifikasjon.status.name,
            bedriftNr = arbeidsgiverRefusjonNotifikasjon.bedriftNr,
            feilmelding = arbeidsgiverRefusjonNotifikasjon.feilmelding,
            sendtTidspunkt = arbeidsgiverRefusjonNotifikasjon.sendtTidspunkt?.atOffset(ZoneOffset.UTC),
            opprettetTidspunkt = arbeidsgiverRefusjonNotifikasjon.opprettetTidspunkt.atOffset(ZoneOffset.UTC),
            varslingsformål = arbeidsgiverRefusjonNotifikasjon.varslingsformål.name,
            avtaleId = arbeidsgiverRefusjonNotifikasjon.avtaleId,
            responseId = arbeidsgiverRefusjonNotifikasjon.responseId,
            hardDeleteSkedulertTidspunkt = arbeidsgiverRefusjonNotifikasjon.hardDeleteSkedulertTidspunkt,
        )
}
