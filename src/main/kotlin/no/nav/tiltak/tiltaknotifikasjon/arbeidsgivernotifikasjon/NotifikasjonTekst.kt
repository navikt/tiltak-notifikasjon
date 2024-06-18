package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype


enum class NotifikasjonTekst(val tekst: (Tiltakstype) -> String) {
    TILTAK_AVTALE_OPPRETTET_SAK({ tiltakstype ->  "Avtale om ${tiltakstype.beskrivelse}" }),
    TILTAK_AVTALE_OPPRETTET({ tiltakstype ->  "Ny avtale om ${tiltakstype.beskrivelse} opprettet. Åpne avtalen og fyll ut innholdet." }),
    TILTAK_AVTALE_INNGATT({ tiltakstype ->  "Avtale om ${tiltakstype.beskrivelse} godkjent." }),
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
    TILTAK_KONTAKTINFORMASJON_ENDRET({ "Kontaktinformasjon i avtale endret av veileder." })
}
