package no.nav.tiltak.tiltaknotifikasjon.utils

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.cfg.EnumFeature
import tools.jackson.databind.introspect.DefaultAccessorNamingStrategy
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder
import de.huxhorn.sulky.ulid.ULID
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleOpphav
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleStatus
import no.nav.tiltak.tiltaknotifikasjon.avtale.Avtalerolle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ulidGenerator = ULID()
fun ulid(): String = ulidGenerator.nextULID()

fun jacksonMapper(): JsonMapper = jacksonMapperBuilder()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
    .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
    .accessorNaming(DefaultAccessorNamingStrategy.Provider().withFirstCharAcceptance(true, true)) // Uten dette droppes properties som starter med æ/ø/å stille
    .build()

fun norskDatoFormat(dato: LocalDate) = dato.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))



/**
 * Deserialisering av JSON uten å bry seg om casing på enum-verdier.
 * Eksempelvis får vi noen enums i lowercased form fra MinSide. Da kan lage enums i kotlin med uppercase navn, og fortsatt deserialisere JSON.
 */
fun jacksonMapperSomIkkeBryrSegOmEnumCase(): JsonMapper = jacksonMapperBuilder()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
    .configure(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
    .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .accessorNaming(DefaultAccessorNamingStrategy.Provider().withFirstCharAcceptance(true, true)) // Uten dette droppes properties som starter med æ/ø/å stille
    .build()

fun erOpphavArenaOgErKlarforvisning(avtaleHendelseMelding: AvtaleHendelseMelding, rolle: Avtalerolle): Boolean {
    if (avtaleHendelseMelding.opphav != AvtaleOpphav.ARENA) return true // Denn e sjekken gjelder kun for opphav Arena (de skjules for arbeidsgiver)
    if (avtaleHendelseMelding.avtaleInngått != null) return true // Avtale er inngått, vis uansett for alle roller

    // DELTAKER
    if (rolle == Avtalerolle.DELTAKER) {
        // Avtalestatus Påbegynt betyr at ikke alle felter er fylt ut enda.
        return avtaleHendelseMelding.avtaleStatus != AvtaleStatus.PÅBEGYNT
    }
    if (rolle != Avtalerolle.ARBEIDSGIVER) {
        throw IllegalArgumentException("Skal ikke brukes for rolle: $rolle")
    }
    // ARBEIDSGIVER

    val påkrevdeMentorFelter = mapOf(
        "deltakerFornavn" to avtaleHendelseMelding.deltakerFornavn,
        "deltakerEtternavn" to avtaleHendelseMelding.deltakerEtternavn,
        "deltakerTlf" to avtaleHendelseMelding.deltakerTlf,
        "bedriftNavn" to avtaleHendelseMelding.bedriftNavn,
        "arbeidsgiverFornavn" to avtaleHendelseMelding.arbeidsgiverFornavn,
        "arbeidsgiverEtternavn" to avtaleHendelseMelding.arbeidsgiverEtternavn,
        "arbeidsgiverTlf" to avtaleHendelseMelding.arbeidsgiverTlf,
        "veilederFornavn" to avtaleHendelseMelding.veilederFornavn,
        "veilederEtternavn" to avtaleHendelseMelding.veilederEtternavn,
        "veilederTlf" to avtaleHendelseMelding.veilederTlf,
        "startDato" to avtaleHendelseMelding.startDato,
        "sluttDato" to avtaleHendelseMelding.sluttDato,
        "oppfolging" to avtaleHendelseMelding.oppfolging,
        "tilrettelegging" to avtaleHendelseMelding.tilrettelegging,
        "mentorFornavn" to avtaleHendelseMelding.mentorFornavn,
        "mentorEtternavn" to avtaleHendelseMelding.mentorEtternavn,
        "mentorOppgaver" to avtaleHendelseMelding.mentorOppgaver,
        "mentorAntallTimer" to avtaleHendelseMelding.mentorAntallTimer,
        "mentorTimelonn" to avtaleHendelseMelding.mentorTimelonn,
        "mentorTlf" to avtaleHendelseMelding.mentorTlf,
        // Tilskuddsperiode-felter (tidligere bak MentorTilskuddsperioderToggle)
        "arbeidsgiverKontonummer" to avtaleHendelseMelding.arbeidsgiverKontonummer,
        "feriepengesats" to avtaleHendelseMelding.feriepengesats,
        "otpSats" to avtaleHendelseMelding.otpSats,
        "arbeidsgiveravgift" to avtaleHendelseMelding.arbeidsgiveravgift,
        "mentorValgtLonnstype" to avtaleHendelseMelding.mentorValgtLonnstype,
        "mentorValgtLonnstypeBelop" to avtaleHendelseMelding.mentorValgtLonnstypeBelop,
        // Familietilknytning
        "harFamilietilknytning" to avtaleHendelseMelding.harFamilietilknytning,
        "familietilknytningForklaring" to avtaleHendelseMelding.familietilknytningForklaring,
    )

    val tommefelter = påkrevdeMentorFelter
        .filter { (key, value) -> erTom(value) }
        .keys

    // Alt er fylt ut, eller kun familietilknytning-felter mangler
    return tommefelter.all { it == "harFamilietilknytning" || it == "familietilknytningForklaring" }
}

private fun erTom(verdi: Any?): Boolean {
    return when (verdi) {
        null -> true
        is String -> verdi.isEmpty()
        is Collection<*> -> verdi.isEmpty()
        else -> false
    }
}
