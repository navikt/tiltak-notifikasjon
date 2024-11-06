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
        val avtaleHendelseMelding3 = avtaleHendelseMelding.copy(arbeidsgiverTlf = "52666543")
        val avtaleHendelseMelding4 = avtaleHendelseMelding.copy(arbeidsgiverTlf = "92345678")
        val avtaleHendelseMelding5 = avtaleHendelseMelding.copy(arbeidsgiverTlf = "42345678")
        val avtaleHendelseMelding6 = avtaleHendelseMelding.copy(arbeidsgiverTlf = "423456789")

        val avtaleHendelseMelding7 = avtaleHendelseMelding.copy(arbeidsgiverTlf = "+4742345678")
        val avtaleHendelseMelding8 = avtaleHendelseMelding.copy(arbeidsgiverTlf = "0046423456789")
        val avtaleHendelseMelding9 = avtaleHendelseMelding.copy(arbeidsgiverTlf = "00464234567895")


        // Ugyldige telefonnummere
        assertFalse(avtaleHendelseMelding.erArbeidsgiversTlfGyldigNorskMobilnr())
        assertFalse(avtaleHendelseMelding2.erArbeidsgiversTlfGyldigNorskMobilnr())
        assertFalse(avtaleHendelseMelding3.erArbeidsgiversTlfGyldigNorskMobilnr())
        assertFalse(avtaleHendelseMelding6.erArbeidsgiversTlfGyldigNorskMobilnr())
        assertFalse(avtaleHendelseMelding9.erArbeidsgiversTlfGyldigNorskMobilnr())
        assertFalse(avtaleHendelseMelding8.erArbeidsgiversTlfGyldigNorskMobilnr())
        // Gyldige telefonnummere
        assertTrue(avtaleHendelseMelding7.erArbeidsgiversTlfGyldigNorskMobilnr())
        assertTrue(avtaleHendelseMelding4.erArbeidsgiversTlfGyldigNorskMobilnr())
        assertTrue(avtaleHendelseMelding5.erArbeidsgiversTlfGyldigNorskMobilnr())
    }


}
