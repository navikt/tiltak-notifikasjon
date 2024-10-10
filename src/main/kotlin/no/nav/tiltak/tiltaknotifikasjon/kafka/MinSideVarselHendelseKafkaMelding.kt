package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.fasterxml.jackson.annotation.JsonProperty
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
    opprettet,
    inaktivert,
    slettet,
    eksternStatusOppdatert
}

enum class EksternStatusOppdatertStatus {
    bestilt,
    sendt,
    feilet
}

enum class Varseltype {
    OPPGAVE,
    BESKJED,
    INNBOKS,
}
