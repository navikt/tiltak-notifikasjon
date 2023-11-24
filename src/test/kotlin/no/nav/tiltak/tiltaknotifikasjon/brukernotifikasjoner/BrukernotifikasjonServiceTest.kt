package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.jsonManglerGodkjenningEndretAvtaleMelding
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles


@ActiveProfiles("dockercompose")
@SpringBootTest
class BrukernotifikasjonServiceTest {

    @Autowired
    lateinit var brukernotifikasjonService: BrukernotifikasjonService

    @Autowired
    lateinit var brukernotifikasjonRepository: BrukernotifikasjonRepository

    @Test
    fun `skal_lage_1_beskjed_ved_avtale_inngått`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding =
            jacksonMapper().readValue(jsonManglerGodkjenningEndretAvtaleMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        println(brukernotifikasjoner)
        assertThat(brukernotifikasjoner).hasSize(1)
    }

}