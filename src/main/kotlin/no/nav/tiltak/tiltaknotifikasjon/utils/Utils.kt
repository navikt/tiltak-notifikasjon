package no.nav.tiltak.tiltaknotifikasjon.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.huxhorn.sulky.ulid.ULID
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ulidGenerator = ULID()
fun ulid(): String = ulidGenerator.nextULID()

fun jacksonMapper(): ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

fun norskDatoFormat(dato: LocalDate) = dato.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))