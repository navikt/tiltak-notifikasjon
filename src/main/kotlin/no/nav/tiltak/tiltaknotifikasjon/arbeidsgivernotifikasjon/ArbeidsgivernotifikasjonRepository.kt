package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.Arbeidsgivernotifikasjon.Companion.ARBEIDSGIVERNOTIFIKASJON
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.records.ArbeidsgivernotifikasjonRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
class ArbeidsgivernotifikasjonRepository(val dsl: DSLContext) {

    fun save(arbeidsgivernotifikasjon: Arbeidsgivernotifikasjon) {
        val arbeidsgivernotifikasjonRecord = mapToDatabaseRecord(arbeidsgivernotifikasjon)
        dsl
            .insertInto(ARBEIDSGIVERNOTIFIKASJON)
            .set(arbeidsgivernotifikasjonRecord)
            .onDuplicateKeyUpdate()
            .set(arbeidsgivernotifikasjonRecord)
            .execute()
    }

    /** responseId: ID'en til selve notifikasjonen som blir oprrettet av min-side-arbeidsgiver når nySak/nyOppgave/nyBeskjed går gjennom, da returneres denne id'en og lagres i 'response_id'
     * Det kan være flere entiteter med samme responseId i basen, fordi: Når vi lager f.eks. en nysak får denne en ID, og når vi kaller softDeleteNotifikasjon på den saken så får vi tilbake den samme ID'en, altså IDen som ble slettet */
    fun findAllByResponseId(id: String): List<Arbeidsgivernotifikasjon> {
        return dsl
            .select()
            .from(ARBEIDSGIVERNOTIFIKASJON)
            .where(ARBEIDSGIVERNOTIFIKASJON.RESPONSE_ID.eq(id))
            .fetchInto(ArbeidsgivernotifikasjonRecord::class.java)
            .map { mapToArbeidsgivernotifikasjon(it as ArbeidsgivernotifikasjonRecord) }
    }
    fun findOppgaveByResponseId(responseId: String): Arbeidsgivernotifikasjon? {
        return dsl
            .select()
            .from(ARBEIDSGIVERNOTIFIKASJON)
            .where(ARBEIDSGIVERNOTIFIKASJON.RESPONSE_ID.eq(responseId))
            .and(ARBEIDSGIVERNOTIFIKASJON.TYPE.eq(ArbeidsgivernotifikasjonType.Oppgave.name))
            .fetchOneInto(ArbeidsgivernotifikasjonRecord::class.java)
            ?.map { mapToArbeidsgivernotifikasjon(it as ArbeidsgivernotifikasjonRecord) }
    }

    /** Finner notifikasjon basert på responseId, altså en Sak, Oppgave eller Beskjed. En responseId kan kun referere til en notifikasjon, men den kan også være referert i en softDelete eller OppgaveUtført*/
    fun findNotifikasjonByResponseId(responseId: String): Arbeidsgivernotifikasjon? {
        return dsl
            .select()
            .from(ARBEIDSGIVERNOTIFIKASJON)
            .where(ARBEIDSGIVERNOTIFIKASJON.RESPONSE_ID.eq(responseId))
            // Beskjed or Oppgave or Sak:
            .and(ARBEIDSGIVERNOTIFIKASJON.TYPE.eq(ArbeidsgivernotifikasjonType.Beskjed.name)
                .or(ARBEIDSGIVERNOTIFIKASJON.TYPE.eq(ArbeidsgivernotifikasjonType.Oppgave.name))
                .or(ARBEIDSGIVERNOTIFIKASJON.TYPE.eq(ArbeidsgivernotifikasjonType.Sak.name)))
            .fetchOneInto(ArbeidsgivernotifikasjonRecord::class.java)
            ?.map { mapToArbeidsgivernotifikasjon(it as ArbeidsgivernotifikasjonRecord) }
    }

    // Fra DB-record til Arbeidsgivernotifikasjon
    private fun mapToArbeidsgivernotifikasjon(record: ArbeidsgivernotifikasjonRecord): Arbeidsgivernotifikasjon {
        return Arbeidsgivernotifikasjon(
            id = record.id,
            varselId = record.varselId,
            avtaleMeldingJson = record.avtaleMeldingJson,
            arbeidsgivernotifikasjonJson = record.arbeidsgivernotifikasjonJson,
            type = if (record.type != null) ArbeidsgivernotifikasjonType.valueOf(record.type!!) else null,
            status = enumValueOf<ArbeidsgivernotifikasjonStatus>(record.status),
            bedriftNr = record.bedriftNr,
            avtaleHendelseType = if (record.avtaleHendelseType != null) HendelseType.valueOf(record.avtaleHendelseType!!) else null,
            feilmelding = record.feilmelding,
            sendt = record.sendt?.toInstant(),
            opprettet = record.opprettet.toInstant(),
            varslingsformål = if (record.varslingsformål != null) Varslingsformål.valueOf(record.varslingsformål!!) else null,
            responseId = record.responseId,
            avtaleId = record.avtaleId,
            avtaleNr = record.avtaleNr,
            hardDeleteSkedulertTidspunkt = record.hardDeleteSkedulertTidspunkt
        )
    }

    // fra Arbeidsgivernotifikasjon klasse til DB-record som skal lagres
    private fun mapToDatabaseRecord(arbeidsgivernotifikasjon: Arbeidsgivernotifikasjon): ArbeidsgivernotifikasjonRecord {
        return ArbeidsgivernotifikasjonRecord(
            id = arbeidsgivernotifikasjon.id,
            varselId = arbeidsgivernotifikasjon.varselId,
            avtaleMeldingJson = arbeidsgivernotifikasjon.avtaleMeldingJson,
            arbeidsgivernotifikasjonJson = arbeidsgivernotifikasjon.arbeidsgivernotifikasjonJson,
            type = arbeidsgivernotifikasjon.type?.name,
            status = arbeidsgivernotifikasjon.status.name,
            bedriftNr = arbeidsgivernotifikasjon.bedriftNr,
            avtaleHendelseType = arbeidsgivernotifikasjon.avtaleHendelseType?.name,
            feilmelding = arbeidsgivernotifikasjon.feilmelding,
            sendt = if (arbeidsgivernotifikasjon.sendt != null) arbeidsgivernotifikasjon.sendt?.atOffset(ZoneOffset.UTC) else null,
            opprettet = arbeidsgivernotifikasjon.opprettet.atOffset(ZoneOffset.UTC),
            varslingsformål = arbeidsgivernotifikasjon.varslingsformål?.name,
            responseId = arbeidsgivernotifikasjon.responseId,
            avtaleId = arbeidsgivernotifikasjon.avtaleId,
            avtaleNr = arbeidsgivernotifikasjon.avtaleNr,
            hardDeleteSkedulertTidspunkt = arbeidsgivernotifikasjon.hardDeleteSkedulertTidspunkt
        )
    }
    //private fun mapToDatabaseRecord(arbeidsgivernotifikasjon: Arbeidsgivernotifikasjon) = jacksonMapper().convertValue<ArbeidsgivernotifikasjonRecord>(arbeidsgivernotifikasjon)
    // TODO: Se på dette en dag

    fun findAll(): List<Arbeidsgivernotifikasjon> {
        return dsl
            .select()
            .from(ARBEIDSGIVERNOTIFIKASJON)
            .fetchInto(ArbeidsgivernotifikasjonRecord::class.java)
            .map { mapToArbeidsgivernotifikasjon(it as ArbeidsgivernotifikasjonRecord) }
    }

    fun findAllbyAvtaleId(avtaleId: String): List<Arbeidsgivernotifikasjon> {
        return dsl
            .select()
            .from(ARBEIDSGIVERNOTIFIKASJON)
            .where(ARBEIDSGIVERNOTIFIKASJON.AVTALE_ID.eq(avtaleId))
            .fetchInto(ArbeidsgivernotifikasjonRecord::class.java)
            .map { mapToArbeidsgivernotifikasjon(it as ArbeidsgivernotifikasjonRecord) }
    }

    fun findSakByResponseId(responseId: String): Arbeidsgivernotifikasjon? {
        return dsl
            .select()
            .from(ARBEIDSGIVERNOTIFIKASJON)
            .where(ARBEIDSGIVERNOTIFIKASJON.RESPONSE_ID.eq(responseId))
            .and(ARBEIDSGIVERNOTIFIKASJON.TYPE.eq(ArbeidsgivernotifikasjonType.Sak.name))
            .fetchOneInto(ArbeidsgivernotifikasjonRecord::class.java)
            ?.map { mapToArbeidsgivernotifikasjon(it as ArbeidsgivernotifikasjonRecord) }
    }

    fun findSakByAvtaleId(avtaleId: String): Arbeidsgivernotifikasjon? {
        return dsl
            .select()
            .from(ARBEIDSGIVERNOTIFIKASJON)
            .where(ARBEIDSGIVERNOTIFIKASJON.AVTALE_ID.eq(avtaleId))
            .and(ARBEIDSGIVERNOTIFIKASJON.TYPE.eq(ArbeidsgivernotifikasjonType.Sak.name))
            .fetchOneInto(ArbeidsgivernotifikasjonRecord::class.java)
            ?.map { mapToArbeidsgivernotifikasjon(it as ArbeidsgivernotifikasjonRecord) }
    }


}