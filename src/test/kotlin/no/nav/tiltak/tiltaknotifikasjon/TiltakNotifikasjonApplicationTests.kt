package no.nav.tiltak.tiltaknotifikasjon

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Profile

@SpringBootTest
@Profile("dockercompose")
class TiltakNotifikasjonApplicationTests {

	@Test
	fun contextLoads() {
	}

}
