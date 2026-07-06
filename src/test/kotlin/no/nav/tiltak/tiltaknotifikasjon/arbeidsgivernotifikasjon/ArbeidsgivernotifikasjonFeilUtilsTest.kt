package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.jackson.types.JacksonGraphQLError
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Rene enhetstester for [erUgyldigMobilnummerFeil] - trenger ikke Spring-kontekst/testcontainers,
 * siden funksjonen kun gjør tekstmatching på en liste med GraphQL-feil.
 */
class ArbeidsgivernotifikasjonFeilUtilsTest {

    private fun graphQlError(message: String) = JacksonGraphQLError(message, emptyList(), emptyList(), emptyMap())

    @Test
    fun `skal returnere true når feilmeldingen inneholder tekst om ugyldig mobilnummer`() {
        val errors = listOf(
            graphQlError("Exception while fetching data (/nyBeskjed) : Kontaktinfo.tlf: verdien er ikke et gyldig norsk mobilnummer. Nummerserien 4273xxxx er blokkert. (se: NKOM E164).")
        )
        assertTrue(erUgyldigMobilnummerFeil(errors))
    }

    @Test
    fun `skal matche uavhengig av store og små bokstaver`() {
        val errors = listOf(graphQlError("IKKE ET GYLDIG NORSK MOBILNUMMER"))
        assertTrue(erUgyldigMobilnummerFeil(errors))
    }

    @Test
    fun `skal returnere true når minst en av flere feil matcher`() {
        val errors = listOf(
            graphQlError("UgyldigMerkelapp: merkelappen finnes ikke"),
            graphQlError("Kontaktinfo.tlf: verdien er ikke et gyldig norsk mobilnummer.")
        )
        assertTrue(erUgyldigMobilnummerFeil(errors))
    }

    @Test
    fun `skal returnere false når ingen feil handler om mobilnummer`() {
        val errors = listOf(
            graphQlError("UgyldigMerkelapp: merkelappen finnes ikke"),
            graphQlError("UkjentProdusent: produsenten er ikke registrert")
        )
        assertFalse(erUgyldigMobilnummerFeil(errors))
    }

    @Test
    fun `skal returnere false for tom feilliste`() {
        assertFalse(erUgyldigMobilnummerFeil(emptyList()))
    }
}
