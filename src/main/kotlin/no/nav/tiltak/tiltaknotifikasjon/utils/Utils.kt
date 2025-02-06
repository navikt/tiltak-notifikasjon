package no.nav.tiltak.tiltaknotifikasjon.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.huxhorn.sulky.ulid.ULID
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
