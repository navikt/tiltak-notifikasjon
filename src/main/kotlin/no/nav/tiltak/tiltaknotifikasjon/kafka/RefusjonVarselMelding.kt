package no.nav.tiltak.tiltaknotifikasjon.kafka

import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.Varslingsformål
import java.time.LocalDate
import java.util.UUID

data class RefusjonVarselMelding(
    val avtaleId: UUID,
    val tilskuddsperiodeId: UUID,
    val refusjonVarselType: RefusjonVarselType,
    val fristForGodkjenning: LocalDate?,
)

enum class RefusjonVarselType {
    KLAR,
    REVARSEL,
    FRIST_FORLENGET,
    KORRIGERT;

    fun fraRefusjonVarselType(): Varslingsformål {
        return when (this) {
            KLAR -> Varslingsformål.REFUSJON_KLAR
            REVARSEL -> Varslingsformål.REFUSJON_REVARSEL
            FRIST_FORLENGET -> Varslingsformål.REFUSJON_FRIST_FORLENGET
            KORRIGERT -> Varslingsformål.REFUSJON_KORRIGERT
        }

    }
}
