package no.nav.tiltak.tiltaknotifikasjon.avtale

import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.AltinnProperties

enum class Tiltakstype(val beskrivelse: String, val skalTilAktivitetsplan: Boolean, val arbeidsgiverNotifikasjonMerkelapp: String, val serviceCode: String, val serviceEdition: String) {
    ARBEIDSTRENING(
        beskrivelse = "Arbeidstrening",
        skalTilAktivitetsplan = false,
        arbeidsgiverNotifikasjonMerkelapp = "Arbeidstrening",
        serviceCode = AltinnProperties().arbtreningServiceCode,
        serviceEdition = AltinnProperties().arbtreningServiceEdition
    ),
    MIDLERTIDIG_LONNSTILSKUDD(
        beskrivelse = "Midlertidig lønnstilskudd",
        skalTilAktivitetsplan = true,
        arbeidsgiverNotifikasjonMerkelapp = "Lønnstilskudd",
        serviceCode = AltinnProperties().ltsMidlertidigServiceCode,
        serviceEdition = AltinnProperties().ltsMidlertidigServiceEdition
    ),
    VARIG_LONNSTILSKUDD(
        beskrivelse = "Varig lønnstilskudd",
        skalTilAktivitetsplan = true,
        arbeidsgiverNotifikasjonMerkelapp = "Lønnstilskudd",
        serviceCode = AltinnProperties().ltsVarigServiceCode,
        serviceEdition = AltinnProperties().ltsVarigServiceEdition
    ),
    MENTOR(
        beskrivelse = "Mentor",
        skalTilAktivitetsplan = false,
        arbeidsgiverNotifikasjonMerkelapp = "Mentor",
        serviceCode = AltinnProperties().mentorServiceCode,
        serviceEdition = AltinnProperties().mentorServiceEdition
    ),
    INKLUDERINGSTILSKUDD(
        beskrivelse = "Inkluderingstilskudd",
        skalTilAktivitetsplan = false,
        arbeidsgiverNotifikasjonMerkelapp = "Inkluderingstilskudd",
        serviceCode = AltinnProperties().inkluderingstilskuddServiceCode,
        serviceEdition = AltinnProperties().inkluderingstilskuddServiceEdition
    ),
    SOMMERJOBB(
        beskrivelse = "Sommerjobb",
        skalTilAktivitetsplan = false,
        arbeidsgiverNotifikasjonMerkelapp = "Sommerjobb",
        serviceCode = AltinnProperties().sommerjobbServiceCode,
        serviceEdition = AltinnProperties().sommerjobbServiceEdition
    );
}