package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.Brukernotifikasjon.Companion.BRUKERNOTIFIKASJON
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.records.BrukernotifikasjonRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class BrukernotifikasjonRepository(val dsl: DSLContext) {

    fun findById(id: String): Brukernotifikasjon? {
        return dsl.select()
            .from(BRUKERNOTIFIKASJON)
            .where(BRUKERNOTIFIKASJON.ID.equal(id))
            .fetchOneInto(BrukernotifikasjonRecord::class.java)
            ?.map { mapToBrukernotifikasjon(it as BrukernotifikasjonRecord) }

    }

    fun findByVarselId(varselId: String): Brukernotifikasjon? {
        return dsl.select()
            .from(BRUKERNOTIFIKASJON)
            .where(BRUKERNOTIFIKASJON.VARSEL_ID.equal(varselId))
            .fetchOneInto(BrukernotifikasjonRecord::class.java)
            ?.map { mapToBrukernotifikasjon(it as BrukernotifikasjonRecord) }
    }

    fun findAllbyAvtaleId(avtaleId: String): List<Brukernotifikasjon> {
        return dsl.select()
            .from(BRUKERNOTIFIKASJON)
            .where(BRUKERNOTIFIKASJON.AVTALE_ID.equal(avtaleId))
            .fetchInto(BrukernotifikasjonRecord::class.java)
            .map { mapToBrukernotifikasjon(it as BrukernotifikasjonRecord) }
    }

    fun findAllByAvtaleIdAndType(avtaleId: String, type: BrukernotifikasjonType): List<Brukernotifikasjon> {
        return dsl.select()
            .from(BRUKERNOTIFIKASJON)
            .where(BRUKERNOTIFIKASJON.AVTALE_ID.equal(avtaleId))
            .and(BRUKERNOTIFIKASJON.TYPE.equal(type.name))
            .fetchInto(BrukernotifikasjonRecord::class.java)
            .map { mapToBrukernotifikasjon(it as BrukernotifikasjonRecord) }
    }

    fun findAll(): List<Brukernotifikasjon> {
        return dsl.select()
            .from(BRUKERNOTIFIKASJON)
            .fetchInto(BrukernotifikasjonRecord::class.java)
            .map { mapToBrukernotifikasjon(it as BrukernotifikasjonRecord) }
    }

    fun save(brukernotifikasjon: Brukernotifikasjon) {
        val brukernotifikasjonRecord = mapToRecord(brukernotifikasjon)
        dsl
            .insertInto(BRUKERNOTIFIKASJON)
            .set(brukernotifikasjonRecord)
            .onDuplicateKeyUpdate()
            .set(brukernotifikasjonRecord)
            .execute()
    }

    fun deleteAll() {
        dsl.deleteFrom(BRUKERNOTIFIKASJON).execute()
    }


    fun mapToBrukernotifikasjon(record: BrukernotifikasjonRecord): Brukernotifikasjon {
        return Brukernotifikasjon(
            id = record.id,
            avtaleMeldingJson = record.avtaleMeldingJson,
            minSideJson = record.minSideJson,
            type = if (record.type != null) enumValueOf<BrukernotifikasjonType>(record.type!!) else null,
            status = enumValueOf<BrukernotifikasjonStatus>(record.status),
            feilmelding = record.feilmelding,
            avtaleId = record.avtaleId,
            avtaleNr = record.avtaleNr,
            deltakerFnr = record.deltakerFnr,
            avtaleHendelseType = if (record.avtaleHendelseType != null) enumValueOf<HendelseType>(record.avtaleHendelseType!!) else null,
            varselId = record.varselId,
            sendt = record.sendt?.toInstant(),
            opprettet = record.opprettet.toInstant(),
            varslingsformål = if (record.varslingsformål != null) enumValueOf<Varslingsformål>(record.varslingsformål!!) else null
        )
    }

    private fun mapToRecord(brukernotifikasjon: Brukernotifikasjon): BrukernotifikasjonRecord {
        return BrukernotifikasjonRecord(
            id = brukernotifikasjon.id,
            avtaleMeldingJson = brukernotifikasjon.avtaleMeldingJson,
            minSideJson = brukernotifikasjon.minSideJson,
            type = brukernotifikasjon.type?.name,
            status = brukernotifikasjon.status.name,
            feilmelding = brukernotifikasjon.feilmelding,
            avtaleId = brukernotifikasjon.avtaleId,
            avtaleNr = brukernotifikasjon.avtaleNr,
            deltakerFnr = brukernotifikasjon.deltakerFnr,
            avtaleHendelseType = brukernotifikasjon.avtaleHendelseType?.name,
            varselId = brukernotifikasjon.varselId,
            sendt = if (brukernotifikasjon.sendt != null) brukernotifikasjon.sendt?.atOffset(ZoneOffset.UTC) else null,
            opprettet = brukernotifikasjon.opprettet.atOffset(ZoneOffset.UTC),
            varslingsformål = brukernotifikasjon.varslingsformål?.name
        )
    }

}
