package no.nav.tiltak.tiltaknotifikasjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication
class TiltakNotifikasjonApplication

fun main(args: Array<String>) {
	runApplication<TiltakNotifikasjonApplication>(*args) {
		if (System.getenv("MILJO") == null) {
			println("Kan ikke startes uten miljøvariabel MILJO. Lokalt kan LokalTiltakRefusjonApplication kjøres.")
			exitProcess(1)
		}
		setAdditionalProfiles(System.getenv("MILJO"))
	}
}
