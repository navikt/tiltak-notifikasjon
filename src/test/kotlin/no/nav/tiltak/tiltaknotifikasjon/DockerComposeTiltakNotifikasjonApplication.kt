package no.nav.tiltak.tiltaknotifikasjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class DockerComposeTiltakNotifikasjonApplication

fun main(args: Array<String>) {
    runApplication<DockerComposeTiltakNotifikasjonApplication>(*args) {
        setAdditionalProfiles("dockercompose")
    }
}
