package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import de.huxhorn.sulky.ulid.ULID
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.Brukernotifikasjon.Companion.BRUKERNOTIFIKASJON
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.records.BrukernotifikasjonRecord
import no.nav.tms.varsel.action.Varseltype
import org.jooq.impl.DSL
import java.util.UUID

class BrukernotifikasjonRepository {

    fun findById(id: UUID): BrukernotifikasjonEntitet? {
        return DSL.select()
            .from(BRUKERNOTIFIKASJON)
            .where(BRUKERNOTIFIKASJON.ID.equal(id.toString()))
            .fetchOneInto(BrukernotifikasjonRecord::class.java)
            ?.map { mapToBrukernotifikasjonEntitet(it as BrukernotifikasjonRecord)}

    }

    fun mapToBrukernotifikasjonEntitet(record: BrukernotifikasjonRecord) :BrukernotifikasjonEntitet {
        return BrukernotifikasjonEntitet(
            id = record.id,
            avtaleMeldingJson = record.avtaleMeldingJson,
            minSideJson = record.minSideJson,
            type = record.type as Varseltype,
            status = record.status as BrukernotifikasjonStatus,
            feilmelding = record.feilmelding
        )
    }
}