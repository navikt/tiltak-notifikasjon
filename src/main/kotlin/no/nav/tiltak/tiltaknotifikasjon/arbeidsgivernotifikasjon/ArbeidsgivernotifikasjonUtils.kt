package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.types.GraphQLClientError

private const val UGYLDIG_MOBILNUMMER_FEILTEKST = "ikke et gyldig norsk mobilnummer"

/**
 * Fager (arbeidsgiver-notifikasjon) validerer telefonnummer mot en dynamisk NKOM-blokkeringsliste
 * (nummerserier kan bli blokkert over tid). Dette kan ikke sjekkes på forhånd med en statisk
 * regex, så vi må håndtere feilen når den oppstår ved å prøve på nytt uten SMS-varsel.
 * Telefonnummer/SMS-varsel er ikke påkrevd av Fager - `eksterneVarsler` kan være en tom liste.
 */
fun erUgyldigMobilnummerFeil(errors: List<GraphQLClientError>): Boolean =
    errors.any { it.message.contains(UGYLDIG_MOBILNUMMER_FEILTEKST, ignoreCase = true) }

