package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.jsonGodkjentAvDeltaker
import no.nav.tiltak.tiltaknotifikasjon.jsonManglerGodkjenningEndretAvtaleMelding
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test-containers")
@Testcontainers
class BrukernotifikasjonServiceTest {

    @MockkBean(relaxed = true)
    lateinit var minSideProdusent: MinSideProdusent
    @Autowired
    lateinit var brukernotifikasjonService: BrukernotifikasjonService
    @Autowired
    lateinit var brukernotifikasjonRepository: BrukernotifikasjonRepository

    @BeforeEach
    fun setup() {
        brukernotifikasjonRepository.deleteAll()
    }

    @Test
    fun `skal lage kun 1 oppgave ved flere mangler godkjenning status meldinger`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding =
            jacksonMapper().readValue(jsonManglerGodkjenningEndretAvtaleMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding, jsonManglerGodkjenningEndretAvtaleMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding, jsonManglerGodkjenningEndretAvtaleMelding)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
    }

    @Test
    fun `skal inaktivere oppgave om godkjenning når deltaker har godkjent`() {
        // Endret melding med status mangler godkjenning
        val avtaleHendelseMelding1: AvtaleHendelseMelding =
            jacksonMapper().readValue(jsonManglerGodkjenningEndretAvtaleMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding1, jsonManglerGodkjenningEndretAvtaleMelding)
        // Melding med godkjent av deltaker
        val avtaleHendelseMelding2: AvtaleHendelseMelding =
            jacksonMapper().readValue(jsonGodkjentAvDeltaker)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding2, jsonGodkjentAvDeltaker)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(2)
    }

}