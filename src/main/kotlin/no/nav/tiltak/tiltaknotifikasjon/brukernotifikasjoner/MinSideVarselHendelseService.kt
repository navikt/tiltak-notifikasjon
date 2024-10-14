package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideVarselHendelseKafkaMelding


class MinSideVarselHendelseService(val brukernotifikasjonRepository: BrukernotifikasjonRepository) {

    fun behandleVarselHendelse(hendelseMelding: MinSideVarselHendelseKafkaMelding) {
        val brukernotifikasjon = brukernotifikasjonRepository.findByVarselId(hendelseMelding.varselId)
        if (brukernotifikasjon == null) {
            log.error()
        }
    }

}
