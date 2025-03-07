package no.nav.tiltak.tiltaknotifikasjon.avtale

enum class HendelseType(val tekst: String) {
    OPPRETTET("Avtale er opprettet av veileder"),
    ENDRET("Avtale endret"),
    GODKJENT_AV_VEILEDER("Avtale er godkjent av veileder"),
    OPPRETTET_AV_ARBEIDSGIVER("Avtale er opprettet av arbeidsgiver"),
    AVTALE_INNGÅTT("Avtale godkjent av NAV"),
    AVTALE_FORKORTET("Avtale forkortet"),
    AVTALE_FORLENGET("Avtale forlenget av veileder"),
    STATUSENDRING("Statusendring"),


    GODKJENT_AV_ARBEIDSGIVER("Avtale er godkjent av arbeidsgiver"),
    GODKJENT_AV_DELTAKER("Avtale er godkjent av deltaker"),
    SIGNERT_AV_MENTOR("Mentor har signert taushetserklæring"),
    GODKJENT_PAA_VEGNE_AV("Veileder godkjente avtalen på vegne av seg selv og deltaker"),
    GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER("Veileder godkjente avtalen på vegne av seg selv, deltaker og arbeidsgiver"),
    GODKJENT_PAA_VEGNE_AV_ARBEIDSGIVER("Veileder godkjente avtalen på vegne av seg selv og arbeidsgiver"),
    GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER("Avtalens godkjenninger er opphevet av arbeidsgiver"),
    GODKJENNINGER_OPPHEVET_AV_VEILEDER("Avtalens godkjenninger er opphevet av veileder"),
    DELT_MED_DELTAKER("Avtale delt med deltaker"),
    DELT_MED_ARBEIDSGIVER("Avtale delt med arbeidsgiver"),
    DELT_MED_MENTOR("Avtale delt med mentor"),
    AVBRUTT("Avtale avbrutt av veileder"),
    ANNULLERT("Avtale annullert av veileder"),
    LÅST_OPP("Avtale låst opp av veileder"),
    GJENOPPRETTET("Avtale gjenopprettet"),
    NY_VEILEDER("Avtale tildelt ny veileder"),
    AVTALE_FORDELT("Avtale tildelt veileder"),
    TILSKUDDSPERIODE_AVSLATT("Tilskuddsperiode har blitt sendt i retur av "),
    TILSKUDDSPERIODE_GODKJENT("Tilskuddsperiode har blitt godkjent av beslutter"),
    MÅL_ENDRET("Mål endret av veileder"),
    INKLUDERINGSTILSKUDD_ENDRET("Inkluderingstilskudd endret av veileder"),
    OM_MENTOR_ENDRET("Om mentor endret av veileder"),
    TILSKUDDSBEREGNING_ENDRET("Tilskuddsberegning endret av veileder"),
    KONTAKTINFORMASJON_ENDRET("Kontaktinformasjon endret av veileder"),
    STILLINGSBESKRIVELSE_ENDRET("Stillingsbeskrivelse endret av veileder"),
    OPPFØLGING_OG_TILRETTELEGGING_ENDRET("Oppfølging og tilrettelegging endret av veileder"),
    REFUSJON_KLAR("Refusjon klar"),
    REFUSJON_KLAR_REVARSEL("Refusjon klar, revarsel"),
    REFUSJON_FRIST_FORLENGET("Frist for refusjon forlenget"),
    REFUSJON_KORRIGERT("Refusjon korrigert"),
    VARSLER_SETT("Varsler lest"),
    AVTALE_SLETTET("Avtale slettet av veileder"),
    GODKJENT_FOR_ETTERREGISTRERING("Avtale er godkjent for etterregistrering"),
    FJERNET_ETTERREGISTRERING("Fjernet etterregistrering på avtale"),
    DELTAKERS_GODKJENNING_OPPHEVET_AV_VEILEDER("Deltakers godkjenning opphevet av veileder"),
    DELTAKERS_GODKJENNING_OPPHEVET_AV_ARBEIDSGIVER("Deltakers godkjenning opphevet av arbeidsgiver"),
    ARBEIDSGIVERS_GODKJENNING_OPPHEVET_AV_VEILEDER("Arbeidsgivers godkjenning opphevet av veileder"),
    ENDRET_AV_ARENA("Avtale synkronisert med fagsystem (Arena)"),
    OPPRETTET_AV_ARENA("Avtale er opprettet av fagsystem (Arena)"),
    AVTALE_FORKORTET_AV_ARENA("Avtale er forkortet av fagsystem (Arena)"),
    AVTALE_FORLENGET_AV_ARENA("Avtale er forlenget av fagsystem (Arena)"),
    PATCH("Retting av feil i data på tidligere meldinger")
}

fun HendelseType.skalSendeSmsTilArbeidsgiver(): Boolean =
    when (this) {
        HendelseType.ARBEIDSGIVERS_GODKJENNING_OPPHEVET_AV_VEILEDER -> true
        HendelseType.AVTALE_INNGÅTT -> true
        HendelseType.AVTALE_FORLENGET -> true
        HendelseType.AVTALE_FORKORTET -> true
        HendelseType.OPPRETTET -> false // Dette lar seg dessverre ikke gjøre, fordi vi ikke har arbeidsgivers tlfnr på dette tidspunktet.
        else -> false
    }
