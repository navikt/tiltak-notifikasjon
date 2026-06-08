package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.ninjasquad.springmockk.MockkBean
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.ArbeidsgiverRefusjonKontaktperson.Companion.ARBEIDSGIVER_REFUSJON_KONTAKTPERSON
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import no.nav.tiltak.tiltaknotifikasjon.kafka.TiltakNotifikasjonKvitteringProdusent
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test-containers")
@Testcontainers
class ArbeidsgiverRefusjonKontaktpersonRepositoryTest {

    @MockkBean(relaxed = true)
    lateinit var minSideProdusent: MinSideProdusent

    @MockkBean(relaxed = true)
    lateinit var tiltakNotifikasjonKvitteringProdusent: TiltakNotifikasjonKvitteringProdusent

    @Autowired
    lateinit var arbeidsgiverRefusjonKontaktpersonRepository: ArbeidsgiverRefusjonKontaktpersonRepository

    @Autowired
    lateinit var dsl: DSLContext

    @BeforeEach
    fun setup() {
        dsl.deleteFrom(ARBEIDSGIVER_REFUSJON_KONTAKTPERSON).execute()
    }

    @Test
    fun `skal lagre refusjon kontaktperson`() {
        val entitet = lagEntitet()

        arbeidsgiverRefusjonKontaktpersonRepository.save(entitet)

        val result = arbeidsgiverRefusjonKontaktpersonRepository.findAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].avtaleId).isEqualTo(entitet.avtaleId)
        assertThat(result[0].refusjonKontaktpersonTlf).isEqualTo(entitet.refusjonKontaktpersonTlf)
        assertThat(result[0].arbeidsgiverOnskerOgsaVarsling).isEqualTo(entitet.arbeidsgiverOnskerOgsaVarsling)
        assertThat(result[0].avtaleInnholdVersjon).isEqualTo(entitet.avtaleInnholdVersjon)
        assertThat(result[0].avtaleHendelseType).isEqualTo(entitet.avtaleHendelseType)
        assertThat(result[0].topicOffset).isEqualTo(entitet.topicOffset)
    }

    @Test
    fun `skal oppdatere eksisterende rad ved upsert paa samme avtaleId`() {
        val avtaleId = UUID.randomUUID()
        val foerste = lagEntitet(avtaleId = avtaleId, tlf = "11111111", versjon = 1)
        val oppdatert = lagEntitet(avtaleId = avtaleId, tlf = "22222222", versjon = 2)

        arbeidsgiverRefusjonKontaktpersonRepository.save(foerste)
        arbeidsgiverRefusjonKontaktpersonRepository.save(oppdatert)

        val result = arbeidsgiverRefusjonKontaktpersonRepository.findAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].refusjonKontaktpersonTlf).isEqualTo("22222222")
        assertThat(result[0].avtaleInnholdVersjon).isEqualTo(2)
    }

    @Test
    fun `id skal ikke endres ved upsert men andre felt skal oppdateres`() {
        val avtaleId = UUID.randomUUID()
        val foerste = lagEntitet(avtaleId = avtaleId, tlf = "11111111", onskerVarsling = false, versjon = 1)

        arbeidsgiverRefusjonKontaktpersonRepository.save(foerste)

        // Hent original id direkte fra DB (ikke eksponert i entiteten)
        val originalId = dsl.selectFrom(ARBEIDSGIVER_REFUSJON_KONTAKTPERSON).fetchOne()!!.id

        val oppdatert = lagEntitet(avtaleId = avtaleId, tlf = "22222222", onskerVarsling = true, versjon = 2)
        arbeidsgiverRefusjonKontaktpersonRepository.save(oppdatert)

        val idEtterUpdate = dsl.selectFrom(ARBEIDSGIVER_REFUSJON_KONTAKTPERSON).fetchOne()!!.id
        // id og avtale_id skal IKKE endres
        assertThat(idEtterUpdate).isEqualTo(originalId)
        // Andre felt SKAL oppdateres
        val result = arbeidsgiverRefusjonKontaktpersonRepository.findAll()
        assertThat(result[0].avtaleId).isEqualTo(avtaleId)
        assertThat(result[0].refusjonKontaktpersonTlf).isEqualTo("22222222")
        assertThat(result[0].arbeidsgiverOnskerOgsaVarsling).isEqualTo(true)
        assertThat(result[0].avtaleInnholdVersjon).isEqualTo(2)
    }

    @Test
    fun `skal haandtere nullable arbeidsgiverOnskerOgsaVarsling`() {
        val entitet = lagEntitet(onskerVarsling = null)

        arbeidsgiverRefusjonKontaktpersonRepository.save(entitet)

        val result = arbeidsgiverRefusjonKontaktpersonRepository.findAll()
        assertThat(result[0].arbeidsgiverOnskerOgsaVarsling).isNull()
    }

    private fun lagEntitet(
        avtaleId: UUID = UUID.randomUUID(),
        tlf: String? = "99887766",
        onskerVarsling: Boolean? = true,
        arbeidsgiverTlf: String = "44556677",
        tiltakstype: Tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
        versjon: Int = 1,
    ) = RefusjonKontaktpersonEntitet(
        avtaleId = avtaleId,
        bedriftNr = "123456789",
        refusjonKontaktpersonTlf = tlf,
        arbeidsgiverOnskerOgsaVarsling = onskerVarsling,
        arbeidsgiverTlf = arbeidsgiverTlf,
        tiltakstype = tiltakstype,
        avtaleInnholdVersjon = versjon,
        avtaleHendelseType = HendelseType.ENDRET,
        avtaleHendelseSistEndret = Instant.now(),
        topicOffset = 42L,
        innlestTidspunkt = Instant.now(),
        deltakerFornavn = "Ola",
        deltakerEtternavn = "Nordmann",
    )
}
