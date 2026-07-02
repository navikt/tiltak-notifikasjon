package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.ninjasquad.springmockk.MockkBean
import no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner.tables.ArbeidsgiverRefusjonNotifikasjon.Companion.ARBEIDSGIVER_REFUSJON_NOTIFIKASJON
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
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test-containers")
@Testcontainers
class ArbeidsgiverRefusjonNotifikasjonRepositoryTest {

    @MockkBean(relaxed = true)
    lateinit var minSideProdusent: MinSideProdusent

    @MockkBean(relaxed = true)
    lateinit var tiltakNotifikasjonKvitteringProdusent: TiltakNotifikasjonKvitteringProdusent

    @Autowired
    lateinit var arbeidsgiverRefusjonNotifikasjonRepository: ArbeidsgiverRefusjonNotifikasjonRepository

    @Autowired
    lateinit var dsl: DSLContext

    @BeforeEach
    fun setup() {
        dsl.deleteFrom(ARBEIDSGIVER_REFUSJON_NOTIFIKASJON).execute()
    }

    @Test
    fun `skal lagre arbeidsgiver refusjon notifikasjon`() {
        val notifikasjon = lagNotifikasjon()

        arbeidsgiverRefusjonNotifikasjonRepository.save(notifikasjon)

        val result = arbeidsgiverRefusjonNotifikasjonRepository.findById(notifikasjon.id)
        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(notifikasjon.id)
        assertThat(result.arbeidsgivernotifikasjonJson).isEqualTo(notifikasjon.arbeidsgivernotifikasjonJson)
        assertThat(result.type).isEqualTo(notifikasjon.type)
        assertThat(result.status).isEqualTo(notifikasjon.status)
        assertThat(result.bedriftNr).isEqualTo(notifikasjon.bedriftNr)
        assertThat(result.varslingsformål).isEqualTo(notifikasjon.varslingsformål)
        assertThat(result.avtaleId).isEqualTo(notifikasjon.avtaleId)
        assertThat(result.responseId).isEqualTo(notifikasjon.responseId)
        assertThat(result.refusjonId).isEqualTo(notifikasjon.refusjonId)
        assertThat(result.hardDeleteSkedulertTidspunkt).isEqualTo(notifikasjon.hardDeleteSkedulertTidspunkt)
    }

    @Test
    fun `skal oppdatere eksisterende rad ved upsert paa samme id`() {
        val id = "ref-notifikasjon-1"
        val foerste = lagNotifikasjon(id = id, status = ArbeidsgivernotifikasjonStatus.SAK_MOTTATT, responseId = null)
        val oppdatert = lagNotifikasjon(
            id = id,
            status = ArbeidsgivernotifikasjonStatus.BEHANDLET,
            feilmelding = "ferdig behandlet",
            responseId = "response-123",
            sendtTidspunkt = Instant.parse("2026-01-02T10:15:30Z"),
        )

        arbeidsgiverRefusjonNotifikasjonRepository.save(foerste)
        arbeidsgiverRefusjonNotifikasjonRepository.save(oppdatert)

        val result = arbeidsgiverRefusjonNotifikasjonRepository.findAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(id)
        assertThat(result[0].status).isEqualTo(ArbeidsgivernotifikasjonStatus.BEHANDLET)
        assertThat(result[0].feilmelding).isEqualTo("ferdig behandlet")
        assertThat(result[0].responseId).isEqualTo("response-123")
        assertThat(result[0].sendtTidspunkt).isEqualTo(Instant.parse("2026-01-02T10:15:30Z"))
    }

    @Test
    fun `skal haandtere nullable felt`() {
        val notifikasjon = lagNotifikasjon(
            feilmelding = null,
            sendtTidspunkt = null,
            responseId = null,
            hardDeleteSkedulertTidspunkt = null,
        )

        arbeidsgiverRefusjonNotifikasjonRepository.save(notifikasjon)

        val result = arbeidsgiverRefusjonNotifikasjonRepository.findById(notifikasjon.id)
        assertThat(result).isNotNull
        assertThat(result!!.feilmelding).isNull()
        assertThat(result.sendtTidspunkt).isNull()
        assertThat(result.responseId).isNull()
        assertThat(result.hardDeleteSkedulertTidspunkt).isNull()
    }

    private fun lagNotifikasjon(
        id: String = "ref-notifikasjon-1",
        arbeidsgivernotifikasjonJson: String = "{\"event\":\"ny-sak\"}",
        type: ArbeidsgivernotifikasjonType = ArbeidsgivernotifikasjonType.Sak,
        status: ArbeidsgivernotifikasjonStatus = ArbeidsgivernotifikasjonStatus.SAK_MOTTATT,
        bedriftNr: String = "123456789",
        feilmelding: String? = "feilmelding",
        sendtTidspunkt: Instant? = Instant.parse("2026-01-01T10:15:30Z"),
        opprettetTidspunkt: Instant = Instant.parse("2026-01-01T09:00:00Z"),
        varslingsformål: Varslingsformål = Varslingsformål.REFUSJON_KLAR,
        avtaleId: String = "avtale-1",
        responseId: String? = "response-1",
        hardDeleteSkedulertTidspunkt: LocalDateTime? = LocalDateTime.of(2026, 1, 31, 12, 0),
    ) = ArbeidsgiverRefusjonNotifikasjon(
        id = id,
        arbeidsgivernotifikasjonJson = arbeidsgivernotifikasjonJson,
        type = type,
        status = status,
        bedriftNr = bedriftNr,
        feilmelding = feilmelding,
        sendtTidspunkt = sendtTidspunkt,
        opprettetTidspunkt = opprettetTidspunkt,
        varslingsformål = varslingsformål,
        avtaleId = avtaleId,
        responseId = responseId,
        hardDeleteSkedulertTidspunkt = hardDeleteSkedulertTidspunkt,
        refusjonId = "refusjon-1",
        kafkaOffset = 100L,
        kafkaKey = "DKLW53FKLDE-1",
    )
}
