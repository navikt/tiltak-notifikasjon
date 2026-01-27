package no.nav.tiltak.tiltaknotifikasjon.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.huxhorn.sulky.ulid.ULID
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleOpphav
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleStatus
import no.nav.tiltak.tiltaknotifikasjon.avtale.Avtalerolle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ulidGenerator = ULID()
fun ulid(): String = ulidGenerator.nextULID()

fun jacksonMapper(): ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)

fun norskDatoFormat(dato: LocalDate) = dato.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))



/**
 * Deserialisering av JSON uten å bry seg om casing på enum-verdier.
 * Eksempelvis får vi noen enums i lowercased form fra MinSide. Da kan lage enums i kotlin med uppercase navn, og fortsatt deserialisere JSON.
 */
fun jacksonMapperSomIkkeBryrSegOmEnumCase(): ObjectMapper = jacksonMapperBuilder()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
    .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .build()

fun mentorAvtaleErKlarForVisningForEksterne(avtaleHendelseMelding: AvtaleHendelseMelding, rolle: Avtalerolle): Boolean {
    // DELTAKER
    if (rolle == Avtalerolle.DELTAKER) {
        // Avtalestatus Påbegynt betyr at ikke alle felter er fylt ut enda.
        return avtaleHendelseMelding.avtaleStatus != AvtaleStatus.PÅBEGYNT
    }
    if (rolle != Avtalerolle.ARBEIDSGIVER) {
        throw IllegalArgumentException("Skal ikke brukes for rolle: $rolle")
    }
    // ARBEIDSGIVER
    if (avtaleHendelseMelding.opphav != AvtaleOpphav.ARENA) return true // Denne sjekken gjelder kun for opphav Arena (de skjules for arbeidsgiver)

    val påkrevdeFelter = mapOf(
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

    val tommefelter = påkrevdeFelter
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
