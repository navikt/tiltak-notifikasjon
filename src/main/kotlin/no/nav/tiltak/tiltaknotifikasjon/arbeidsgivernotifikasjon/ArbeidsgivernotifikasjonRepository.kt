package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.Arbeidsgivernotifikasjon.Companion.ARBEIDSGIVERNOTIFIKASJON
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.records.ArbeidsgivernotifikasjonRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
class ArbeidsgivernotifikasjonRepository(val dsl: DSLContext) {

    fun findById(id: String): Arbeidsgivernotifikasjon? {
        return dsl.select()
            .from(ARBEIDSGIVERNOTIFIKASJON)
            .where(ARBEIDSGIVERNOTIFIKASJON.ID.equal(id))
            .fetchOneInto(ArbeidsgivernotifikasjonRecord::class.java)
            ?.map { mapToArbeidsgivernotifikasjon(it as ArbeidsgivernotifikasjonRecord) }
    }

    fun save(arbeidsgivernotifikasjon: Arbeidsgivernotifikasjon) {
        val arbeidsgivernotifikasjonRecord = mapToRecord(arbeidsgivernotifikasjon)
        dsl
            .insertInto(ARBEIDSGIVERNOTIFIKASJON)
            .set(arbeidsgivernotifikasjonRecord)
            .onDuplicateKeyUpdate()
            .set(arbeidsgivernotifikasjonRecord)
            .execute()
    }

    private fun mapToArbeidsgivernotifikasjon(record: ArbeidsgivernotifikasjonRecord): Arbeidsgivernotifikasjon {
        return Arbeidsgivernotifikasjon(
            id = record.id,
            varselId = record.varselId,
            avtaleMeldingJson = record.avtaleMeldingJson,
            notifikasjonJson = record.arbeidsgivernotifikasjonJson,
            type = if (record.type != null) ArbeidsgivernotifikasjonType.valueOf(record.type!!) else null,
            status = enumValueOf<ArbeidsgivernotifikasjonStatus>(record.status),
            bedriftNr = record.bedriftNr,
            avtaleHendelseType = if (record.avtaleHendelseType != null) HendelseType.valueOf(record.avtaleHendelseType!!) else null,
            feilmelding = record.feilmelding,
            sendt = record.sendt?.toInstant(),
            opprettet = record.opprettet.toInstant(),
            varslingsformål = if (record.varslingsformål != null) Varslingsformål.valueOf(record.varslingsformål!!) else null,
            responseId = record.responseId
        )
    }

    private fun mapToRecord(arbeidsgivernotifikasjon: Arbeidsgivernotifikasjon): ArbeidsgivernotifikasjonRecord {
        return ArbeidsgivernotifikasjonRecord(
            id = arbeidsgivernotifikasjon.id,
            varselId = arbeidsgivernotifikasjon.varselId,
            avtaleMeldingJson = arbeidsgivernotifikasjon.avtaleMeldingJson,
            arbeidsgivernotifikasjonJson = arbeidsgivernotifikasjon.notifikasjonJson,
            type = arbeidsgivernotifikasjon.type?.name,
            status = arbeidsgivernotifikasjon.status.name,
            bedriftNr = arbeidsgivernotifikasjon.bedriftNr,
            avtaleHendelseType = arbeidsgivernotifikasjon.avtaleHendelseType?.name,
            feilmelding = arbeidsgivernotifikasjon.feilmelding,
            sendt = if (arbeidsgivernotifikasjon.sendt != null) arbeidsgivernotifikasjon.sendt?.atOffset(ZoneOffset.UTC) else null ,
            opprettet = arbeidsgivernotifikasjon.opprettet.atOffset(ZoneOffset.UTC),
            varslingsformål = arbeidsgivernotifikasjon.varslingsformål?.name,
            responseId = arbeidsgivernotifikasjon.responseId
        )
    }

}