package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import de.huxhorn.sulky.ulid.ULID
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.Brukernotifikasjon.Companion.BRUKERNOTIFIKASJON
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.records.BrukernotifikasjonRecord
import no.nav.tms.varsel.action.Varseltype
import org.jooq.impl.DSL

class BrukernotifikasjonRepository {

    fun findById(id: ULID): BrukernotifikasjonEntitet? {
        return DSL.select()
            .from(BRUKERNOTIFIKASJON)
            .where(BRUKERNOTIFIKASJON.ID.equal(id.toString()))
            .fetchOneInto(BrukernotifikasjonRecord::class.java)
            ?.map { mapToBrukernotifikasjonEntitet(it as BrukernotifikasjonRecord) }

    }

    fun save(brukernotifikasjonEntitet: BrukernotifikasjonEntitet) {
        val brukernotifikasjonRecord = mapToRecord(brukernotifikasjonEntitet)
        DSL
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
            type = record.type as Varseltype,
            status = record.status as BrukernotifikasjonStatus,
            feilmelding = record.feilmelding
        )
    }
    private fun mapToRecord(brukernotifikasjonEntitet: BrukernotifikasjonEntitet): BrukernotifikasjonRecord {
        return BrukernotifikasjonRecord(
            id = brukernotifikasjonEntitet.id,
            avtaleMeldingJson = brukernotifikasjonEntitet.avtaleMeldingJson,
            minSideJson = brukernotifikasjonEntitet.minSideJson,
            type = brukernotifikasjonEntitet.type.name,
            status = brukernotifikasjonEntitet.status.name,
            feilmelding = brukernotifikasjonEntitet.feilmelding
        )
    }

}