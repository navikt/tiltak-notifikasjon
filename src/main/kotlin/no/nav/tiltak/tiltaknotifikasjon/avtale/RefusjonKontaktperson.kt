package no.nav.tiltak.tiltaknotifikasjon.avtale

data class RefusjonKontaktperson(
    val refusjonKontaktpersonFornavn: String? = null,
    val refusjonKontaktpersonEtternavn: String? = null,
    val refusjonKontaktpersonTlf: String? = null,
    val ønskerVarslingOmRefusjon: Boolean? = null
)
