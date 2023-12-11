package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.Brukernotifikasjon.Companion.BRUKERNOTIFIKASJON
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.records.BrukernotifikasjonRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class BrukernotifikasjonRepository(val dsl: DSLContext) {

    fun findById(id: String): Brukernotifikasjon? {
        return dsl.select()
            .from(BRUKERNOTIFIKASJON)
            .where(BRUKERNOTIFIKASJON.ID.equal(id))
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
            type = enumValueOf<BrukernotifikasjonType>(record.type!!),
            status = enumValueOf<BrukernotifikasjonStatus>(record.status!!),
            feilmelding = record.feilmelding,
            avtaleId = record.avtaleId!!,
            avtaleNr = record.avtaleNr!!,
            deltakerFnr = record.deltakerFnr!!,
            avtaleHendelseType = enumValueOf<HendelseType>(record.avtaleHendelseType!!),
            varselId = record.varselId!!,
            sendt = record.sendt!!
        )
    }

    private fun mapToRecord(brukernotifikasjon: Brukernotifikasjon): BrukernotifikasjonRecord {
        return BrukernotifikasjonRecord(
            id = brukernotifikasjon.id,
            avtaleMeldingJson = brukernotifikasjon.avtaleMeldingJson,
            minSideJson = brukernotifikasjon.minSideJson,
            type = brukernotifikasjon.type.name,
            status = brukernotifikasjon.status.name,
            feilmelding = brukernotifikasjon.feilmelding,
            avtaleId = brukernotifikasjon.avtaleId,
            avtaleNr = brukernotifikasjon.avtaleNr,
            deltakerFnr = brukernotifikasjon.deltakerFnr,
            avtaleHendelseType = brukernotifikasjon.avtaleHendelseType.name,
            varselId = brukernotifikasjon.varselId,
            sendt = brukernotifikasjon.sendt
        )
    }

}