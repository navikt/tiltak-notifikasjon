package no.nav.tiltak.tiltaknotifikasjon.kafka

import java.time.LocalDate
import java.util.UUID

data class RefusjonVarselMelding(
    val avtaleId: UUID,
    val tilskuddsperiodeId: UUID,
    val varselType: VarselType,
    val fristForGodkjenning: LocalDate?,
)

enum class VarselType {
    KLAR,
    REVARSEL,
    FRIST_FORLENGET,
    KORRIGERT,
}
