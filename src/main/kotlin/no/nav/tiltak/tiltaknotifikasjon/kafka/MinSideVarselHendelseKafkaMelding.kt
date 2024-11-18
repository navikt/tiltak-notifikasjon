package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import no.nav.tms.varsel.action.EksternKanal

data class MinSideVarselHendelseKafkaMelding(
    @JsonProperty("@event_name")
    val eventName: EventName,
    val status: EksternStatusOppdatertStatus?,
    val varseltype: Varseltype,
    val varselId: String,
    val feilmelding: String?,
    val kanal: EksternKanal?,
    val renotifikasjon: Boolean?,
    val namespace: String,
    val appnavn: String
)


enum class EventName {
    OPPRETTET,
    INAKTIVERT,
    SLETTET,
    EKSTERNSTATUSOPPDATERT
}
enum class EksternStatusOppdatertStatus {
    BESTILT,
    SENDT,
    FEILET,
    VENTER,
    KANSELLERT,
    FERDIGSTILT
}
enum class Varseltype {
    OPPGAVE,
    BESKJED,
    INNBOKS
}
