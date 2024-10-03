package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.deleteAll
import no.nav.tiltak.tiltaknotifikasjon.jsonGodkjentAvArbeidsgiverMelding
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers


@SpringBootTest
@ActiveProfiles("test-containers")
@Testcontainers
class BrukernotifikasjonKtTest{

    @Autowired
    lateinit var brukernotifikasjonService: BrukernotifikasjonService
    @Autowired
    lateinit var brukernotifikasjonRepository: BrukernotifikasjonRepository
    @MockkBean(relaxed = true)
    lateinit var minSideProdusent: MinSideProdusent

    @BeforeEach
    fun setup() {
        brukernotifikasjonRepository.deleteAll()
    }


    @Test
    fun `godkjent_av_ag_skal_ha_sms_sendt_true`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvArbeidsgiverMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertTrue(brukernotifikasjoner[0].sendtSms())
    }

}