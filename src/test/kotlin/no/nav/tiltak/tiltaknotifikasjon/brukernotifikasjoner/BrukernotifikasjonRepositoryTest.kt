package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
import no.nav.tms.varsel.action.Varseltype
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("dockercompose")
class BrukernotifikasjonRepositoryTest {

    @Autowired
    lateinit var brukernotifikasjonRepository: BrukernotifikasjonRepository

        @Test
        fun `skal_kunne_lagre_og_finne_entitet_i_db`() {
            val id = ulid()
            val brukernotifikasjonEntitet = BrukernotifikasjonEntitet(
                avtaleMeldingJson = "avtaleMeldingJson",
                minSideJson = "minSideJson",
                id = id,
                type = Varseltype.Beskjed,
                status = BrukernotifikasjonStatus.MOTTATT
            )
            brukernotifikasjonRepository.save(brukernotifikasjonEntitet)
            val brukernotifikasjon = brukernotifikasjonRepository.findById(id)
            assertNotNull(brukernotifikasjon)
        }
}