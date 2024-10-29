package no.nav.tiltak.tiltaknotifikasjon.avtale

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltak.tiltaknotifikasjon.jsonAvtaleOpprettetMelding
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class AvtaleHendelseMeldingKtTest {

    @Test
    fun `valider telefonnummere`() {
        val avtaleHendelseMelding = jacksonMapper().readValue<AvtaleHendelseMelding>(jsonAvtaleOpprettetMelding).copy(arbeidsgiverTlf = "12345678")
        val avtaleHendelseMelding2 = jacksonMapper().readValue<AvtaleHendelseMelding>(jsonAvtaleOpprettetMelding) // null
        val avtaleHendelseMelding3 = jacksonMapper().readValue<AvtaleHendelseMelding>(jsonAvtaleOpprettetMelding).copy(arbeidsgiverTlf = "52666543")
        val avtaleHendelseMelding4 = jacksonMapper().readValue<AvtaleHendelseMelding>(jsonAvtaleOpprettetMelding).copy(arbeidsgiverTlf = "92345678")
        val avtaleHendelseMelding5 = jacksonMapper().readValue<AvtaleHendelseMelding>(jsonAvtaleOpprettetMelding).copy(arbeidsgiverTlf = "42345678")
        val avtaleHendelseMelding6 = jacksonMapper().readValue<AvtaleHendelseMelding>(jsonAvtaleOpprettetMelding).copy(arbeidsgiverTlf = "423456789")
        // Ugyldige telefonnummere
        assertFalse(avtaleHendelseMelding.erArbeidsgiversTlfGyldig())
        assertFalse(avtaleHendelseMelding2.erArbeidsgiversTlfGyldig())
        assertFalse(avtaleHendelseMelding3.erArbeidsgiversTlfGyldig())
        assertFalse(avtaleHendelseMelding6.erArbeidsgiversTlfGyldig())
        // Gyldige telefonnummere
        assertTrue(avtaleHendelseMelding4.erArbeidsgiversTlfGyldig())
        assertTrue(avtaleHendelseMelding5.erArbeidsgiversTlfGyldig())
    }


}
