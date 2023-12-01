package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import com.ninjasquad.springmockk.MockkBean
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import no.nav.tms.varsel.action.Varseltype
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
class BrukernotifikasjonRepositoryTest {

    @MockkBean(relaxed = true)
    lateinit var minSideProdusent: MinSideProdusent
    @Autowired
    lateinit var brukernotifikasjonRepository: BrukernotifikasjonRepository

    @BeforeEach
    fun setup() {
        brukernotifikasjonRepository.deleteAll()
    }

        @Test
        fun `skal_kunne_lagre_og_finne_entitet_i_db`() {
            val id = ulid()
            val brukernotifikasjonEntitet = Brukernotifikasjon(
                varselId = "varselId",
                avtaleMeldingJson = "avtaleMeldingJson",
                minSideJson = "minSideJson",
                id = id,
                type = Varseltype.Beskjed,
                status = BrukernotifikasjonStatus.MOTTATT,
                deltakerFnr = "12345678910",
                avtaleId = "12345678910",
                avtaleNr = 123,
                avtaleHendelseType = HendelseType.ENDRET
            )
            brukernotifikasjonRepository.save(brukernotifikasjonEntitet)
            val brukernotifikasjon = brukernotifikasjonRepository.findById(id)
            assertNotNull(brukernotifikasjon)
        }
}