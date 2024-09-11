package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.avtale.lagArbeidsgivernotifikasjonTekst
import no.nav.tiltak.tiltaknotifikasjon.jsonAvtaleOpprettetMelding
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class NotifikasjonTekstTest {

    @Test
    fun `skal lage tekst for ulike hendelser`() {
        val melding = jacksonMapper().readValue<AvtaleHendelseMelding>(jsonAvtaleOpprettetMelding)

        assertEquals("Avtale om Midlertidig lønnstilskudd for Donald Duck", NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET_SAK.tekst(melding))
        assertEquals("Ny avtale om Midlertidig lønnstilskudd opprettet. Åpne avtalen og fyll ut innholdet.", NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET.tekst(melding))
        assertEquals("Avtale om Midlertidig lønnstilskudd godkjent.", NotifikasjonTekst.TILTAK_AVTALE_INNGATT.tekst(melding))
        assertEquals("Du kan nå søke om refusjon.", NotifikasjonTekst.TILTAK_AVTALE_KLAR_REFUSJON.tekst(melding))
        assertEquals("Stillingsbeskrivelse i avtale endret av veileder.", NotifikasjonTekst.TILTAK_STILLINGSBESKRIVELSE_ENDRET.tekst(melding))
        assertEquals("Mål i avtale endret av veileder.", NotifikasjonTekst.TILTAK_MÅL_ENDRET.tekst(melding))
        assertEquals("Inkluderingstilskudd i avtalen endret av veileder.", NotifikasjonTekst.TILTAK_INKLUDERINGSTILSKUDD_ENDRET.tekst(melding))
        assertEquals("Om mentor i avtale endret av veileder.", NotifikasjonTekst.TILTAK_OM_MENTOR_ENDRET.tekst(melding))
        assertEquals("Oppfølging og tilrettelegging i avtale endret av veileder.", NotifikasjonTekst.TILTAK_OPPFØLGING_OG_TILRETTELEGGING_ENDRET.tekst(melding))
        assertEquals("Avtale forkortet.", NotifikasjonTekst.TILTAK_AVTALE_FORKORTET.tekst(melding))

        val saksMelding = melding.lagArbeidsgivernotifikasjonTekst(true)
        assertEquals("Avtale om Midlertidig lønnstilskudd for Donald Duck", saksMelding)

        val melding2 = melding.copy(hendelseType = HendelseType.NY_VEILEDER)
        assertThrows(IllegalArgumentException::class.java) { melding2.lagArbeidsgivernotifikasjonTekst(false) }


    }
}