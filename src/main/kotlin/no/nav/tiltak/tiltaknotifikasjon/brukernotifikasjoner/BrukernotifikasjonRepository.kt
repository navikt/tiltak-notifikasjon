package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.Brukernotifikasjon.Companion.BRUKERNOTIFIKASJON
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.records.BrukernotifikasjonRecord
import no.nav.tms.varsel.action.Varseltype
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class BrukernotifikasjonRepository(val dsl: DSLContext) {

    fun findById(id: String): BrukernotifikasjonEntitet? {
        return dsl.select()
            .from(BRUKERNOTIFIKASJON)
            .where(BRUKERNOTIFIKASJON.ID.equal(id))
            .fetchOneInto(BrukernotifikasjonRecord::class.java)
            ?.map { mapToBrukernotifikasjonEntitet(it as BrukernotifikasjonRecord) }

    }

    fun findAll(): List<BrukernotifikasjonEntitet> {
        return dsl.select()
            .from(BRUKERNOTIFIKASJON)
            .fetchInto(BrukernotifikasjonRecord::class.java)
            .map { mapToBrukernotifikasjonEntitet(it as BrukernotifikasjonRecord) }
    }

    fun save(brukernotifikasjonEntitet: BrukernotifikasjonEntitet) {
        val brukernotifikasjonRecord = mapToRecord(brukernotifikasjonEntitet)
        dsl
            .insertInto(BRUKERNOTIFIKASJON)
            .set(brukernotifikasjonRecord)
            .onDuplicateKeyUpdate()
            .set(brukernotifikasjonRecord)
            .execute()
    }


    fun mapToBrukernotifikasjonEntitet(record: BrukernotifikasjonRecord): BrukernotifikasjonEntitet {
        return BrukernotifikasjonEntitet(
            id = record.id,
            avtaleMeldingJson = record.avtaleMeldingJson,
            minSideJson = record.minSideJson,
            type = enumValueOf<Varseltype>(record.type!!),
            status = enumValueOf<BrukernotifikasjonStatus>(record.status!!),
            feilmelding = record.feilmelding,
            avtaleId = record.avtaleId,
            avtaleNr = record.avtaleNr,
            deltakerFnr = record.delakerFnr
        )
    }

    private fun mapToRecord(brukernotifikasjonEntitet: BrukernotifikasjonEntitet): BrukernotifikasjonRecord {
        return BrukernotifikasjonRecord(
            id = brukernotifikasjonEntitet.id,
            avtaleMeldingJson = brukernotifikasjonEntitet.avtaleMeldingJson,
            minSideJson = brukernotifikasjonEntitet.minSideJson,
            type = brukernotifikasjonEntitet.type.name,
            status = brukernotifikasjonEntitet.status.name,
            feilmelding = brukernotifikasjonEntitet.feilmelding,
            avtaleId = brukernotifikasjonEntitet.avtaleId,
            avtaleNr = brukernotifikasjonEntitet.avtaleNr,
            deltakerFnr = brukernotifikasjonEntitet.deltakerFnr
        )
    }

}