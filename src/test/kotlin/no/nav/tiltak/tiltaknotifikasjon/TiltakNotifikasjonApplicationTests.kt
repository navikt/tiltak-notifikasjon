package no.nav.tiltak.tiltaknotifikasjon

import com.ninjasquad.springmockk.MockkBean
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgivernotifikasjonRepository
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.Arbeidsgivernotifikasjon.Companion.ARBEIDSGIVERNOTIFIKASJON
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import no.nav.tiltak.tiltaknotifikasjon.utils.Cluster
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test-containers")
@Testcontainers
class TiltakNotifikasjonApplicationTests {

	@MockkBean(relaxed = true)
	lateinit var minSideProdusent: MinSideProdusent

	@Test
	fun contextLoads() {
	}

}

fun ArbeidsgivernotifikasjonRepository.deleteAll() {
	dsl.deleteFrom(ARBEIDSGIVERNOTIFIKASJON).execute()
}