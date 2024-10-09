package no.nav.tiltak.tiltaknotifikasjon.kafka


import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.TiltakNotifikasjonKvittering.Companion.TILTAK_NOTIFIKASJON_KVITTERING
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.records.TiltakNotifikasjonKvitteringRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
class TiltakNotifikasjonKvitteringRepository(val dsl: DSLContext) {

    fun save(tiltakNotifikasjonKvitteringDto: TiltakNotifikasjonKvitteringDto) {
        val tiltakNotifikasjonKvitteringRecord = mapToDatabaseRecord(tiltakNotifikasjonKvitteringDto)
        dsl
            .insertInto(TILTAK_NOTIFIKASJON_KVITTERING)
            .set(tiltakNotifikasjonKvitteringRecord)
            .onDuplicateKeyUpdate()
            .set(tiltakNotifikasjonKvitteringRecord)
            .execute()
    }

    private fun mapToDatabaseRecord(tiltakNotifikasjonKvitteringDto: TiltakNotifikasjonKvitteringDto): TiltakNotifikasjonKvitteringRecord {
        return TiltakNotifikasjonKvitteringRecord(
            id = tiltakNotifikasjonKvitteringDto.id,
            notifikasjonstype = tiltakNotifikasjonKvitteringDto.notifikasjonstype.name,
            payload = tiltakNotifikasjonKvitteringDto.payload,
            feilmelding = tiltakNotifikasjonKvitteringDto.feilmelding,
            sendtTidspunkt = tiltakNotifikasjonKvitteringDto.sendtTidspunkt?.atOffset(ZoneOffset.UTC),
            avtaleHendelseType = tiltakNotifikasjonKvitteringDto.avtaleHendelseType.name,
            mottaker = tiltakNotifikasjonKvitteringDto.mottaker,
            sendtSms = tiltakNotifikasjonKvitteringDto.sendtSms,
            avtaleId = tiltakNotifikasjonKvitteringDto.avtaleId,
            opprettetTidspunkt = tiltakNotifikasjonKvitteringDto.opprettetTidspunkt.atOffset(ZoneOffset.UTC),
            notifikasjonId = tiltakNotifikasjonKvitteringDto.notifikasjonId,
        )
    }

    private fun mapToTiltakNotifikasjonKvittering(tiltakNotifikasjonKvitteringRecord: TiltakNotifikasjonKvitteringRecord): TiltakNotifikasjonKvitteringDto {
        return TiltakNotifikasjonKvitteringDto(
            id = tiltakNotifikasjonKvitteringRecord.id,
            notifikasjonstype = NotifikasjonsType.valueOf(tiltakNotifikasjonKvitteringRecord.notifikasjonstype),
            payload = tiltakNotifikasjonKvitteringRecord.payload,
            feilmelding = tiltakNotifikasjonKvitteringRecord.feilmelding,
            sendtTidspunkt = tiltakNotifikasjonKvitteringRecord.sendtTidspunkt?.toInstant(),
            avtaleHendelseType = HendelseType.valueOf(tiltakNotifikasjonKvitteringRecord.avtaleHendelseType),
            mottaker = tiltakNotifikasjonKvitteringRecord.mottaker,
            sendtSms = tiltakNotifikasjonKvitteringRecord.sendtSms,
            avtaleId = tiltakNotifikasjonKvitteringRecord.avtaleId,
            notifikasjonId = tiltakNotifikasjonKvitteringRecord.notifikasjonId,
            opprettetTidspunkt = tiltakNotifikasjonKvitteringRecord.opprettetTidspunkt.toInstant(),
        )
    }


}
