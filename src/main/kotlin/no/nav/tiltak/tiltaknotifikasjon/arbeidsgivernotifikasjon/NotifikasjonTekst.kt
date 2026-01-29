package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype


enum class NotifikasjonTekst(val tekst: (AvtaleHendelseMelding) -> String) {
    TILTAK_AVTALE_OPPRETTET_SAK({ avtaleHendelseMelding ->  "Avtale om ${avtaleHendelseMelding.tiltakstype.beskrivelse} ${medNavn(avtaleHendelseMelding)}" }),
    TILTAK_AVTALE_INNGATT_SAK({ avtaleHendelseMelding ->  "Avtale om ${avtaleHendelseMelding.tiltakstype.beskrivelse} ${medNavn(avtaleHendelseMelding)}" }),
    TILTAK_AVTALE_ENDRET_SAK({ avtaleHendelseMelding ->  "Avtale om ${avtaleHendelseMelding.tiltakstype.beskrivelse} ${medNavn(avtaleHendelseMelding)}" }),
    TILTAK_AVTALE_OPPRETTET({ avtaleHendelseMelding ->  "Ny avtale om ${avtaleHendelseMelding.tiltakstype.beskrivelse} opprettet. Åpne avtalen og fyll ut innholdet." }),
    TILTAK_AVTALE_ENDRET({ avtaleHendelseMelding ->  "Ny avtale om ${avtaleHendelseMelding.tiltakstype.beskrivelse} opprettet. Åpne avtalen og fyll ut innholdet." }),
    TILTAK_AVTALE_INNGATT({ avtaleHendelseMelding ->  "Avtale om ${avtaleHendelseMelding.tiltakstype.beskrivelse} godkjent." }),
    TILTAK_AVTALE_KLAR_REFUSJON({ "Du kan nå søke om refusjon." }),
    TILTAK_STILLINGSBESKRIVELSE_ENDRET({"Stillingsbeskrivelse i avtale endret av veileder." }),
    TILTAK_MÅL_ENDRET({ "Mål i avtale endret av veileder." }),
    TILTAK_INKLUDERINGSTILSKUDD_ENDRET({ "Inkluderingstilskudd i avtalen endret av veileder." }),
    TILTAK_OM_MENTOR_ENDRET({ "Om mentor i avtale endret av veileder." }),
    TILTAK_OPPFØLGING_OG_TILRETTELEGGING_ENDRET({ "Oppfølging og tilrettelegging i avtale endret av veileder." }),
    TILTAK_AVTALE_FORKORTET({ "Avtale forkortet." }),
    TILTAK_AVTALE_FORLENGET({ "Avtale forlenget av veileder." }),
    TILTAK_TILSKUDDSBEREGNING_ENDRET({ "Tilskuddsberegning i avtale endret av veileder." }),
    TILTAK_GODKJENNINGER_OPPHEVET_AV_VEILEDER({ "Avtalen må godkjennes på nytt." }),
    TILTAK_KONTAKTINFORMASJON_ENDRET({ "Kontaktinformasjon i avtale endret av veileder." }),
    TILTAK_AVTALE_ANNULLERT({ "Avtale ble avlyst." }),
}

fun medNavn(avtaleHendelseMelding: AvtaleHendelseMelding): String? {
    if (avtaleHendelseMelding.deltakerFornavn !== null && avtaleHendelseMelding.deltakerEtternavn !== null) {
        return "for ${avtaleHendelseMelding.deltakerFornavn} ${avtaleHendelseMelding.deltakerEtternavn}"
    } else {
        return null
    }
}
