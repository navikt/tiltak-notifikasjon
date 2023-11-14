package no.nav.tiltak.tiltaknotifikasjon

import de.huxhorn.sulky.ulid.ULID

private val ulidGenerator = ULID()
fun ulid(): String = ulidGenerator.nextULID()