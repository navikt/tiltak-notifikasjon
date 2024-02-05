package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import no.nav.tiltak.tiltaknotifikasjon.*
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.kafka.MinSideProdusent
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import no.nav.tiltak.tiltaknotifikasjon.utils.ulid
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
    fun `skal lage oppgave med formål godkjenning av avtale når arbeidsgiver godkjenner`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvArbeidsgiverMelding)
        val brukernotifikasjonFraAvtaleHendelse = opprettBrukernotifikasjonFraAvtaleHendelse(jsonGodkjentAvArbeidsgiverMelding)

        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding, brukernotifikasjonFraAvtaleHendelse)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
        assertThat(brukernotifikasjoner[0].varslingsformål).isEqualTo(Varslingsformål.GODKJENNING_AV_AVTALE)
        assertThat(brukernotifikasjoner[0].type).isEqualTo(BrukernotifikasjonType.Oppgave)
        assertThat(brukernotifikasjoner[0].status).isEqualTo(BrukernotifikasjonStatus.BEHANDLET)
    }

    @Test
    fun `skal lage kun 1 oppgave ved flere godkjent av arbeidsgiver status meldinger`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvArbeidsgiverMelding)
        val brukernotifikasjonFraAvtaleHendelse = opprettBrukernotifikasjonFraAvtaleHendelse(jsonGodkjentAvArbeidsgiverMelding)

        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding, brukernotifikasjonFraAvtaleHendelse)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding, brukernotifikasjonFraAvtaleHendelse)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
    }

    @Test
    fun `skal inaktivere oppgave om godkjenning når deltaker har godkjent`() {
        // Godkjent av arbeidsgiver melding
        val avtaleHendelseMelding1: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvArbeidsgiverMelding)
        val brukernotifikasjonFraAvtaleHendelse = opprettBrukernotifikasjonFraAvtaleHendelse(jsonGodkjentAvArbeidsgiverMelding)

        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding1, brukernotifikasjonFraAvtaleHendelse)

        // Melding med godkjent av deltaker
        val avtaleHendelseMelding2: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvDeltaker)
        val brukernotifikasjonFraAvtaleHendelse2 = opprettBrukernotifikasjonFraAvtaleHendelse(jsonGodkjentAvDeltaker)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding2, brukernotifikasjonFraAvtaleHendelse2)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        val inaktiverteNotifikasjoner = brukernotifikasjoner.filter { it.type == BrukernotifikasjonType.Inaktivering }
        assertThat(inaktiverteNotifikasjoner).hasSize(1)
        assertThat(brukernotifikasjoner).hasSize(2)
    }

    @Test
    fun `skal inaktivere oppgave om godkjenning når avtalen annulleres`() {
        // Godkjent av arbeidsgiver melding
        val avtaleHendelseMelding1: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvArbeidsgiverMelding)
        val brukernotifikasjonFraAvtaleHendelse = opprettBrukernotifikasjonFraAvtaleHendelse(jsonGodkjentAvArbeidsgiverMelding)

        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding1, brukernotifikasjonFraAvtaleHendelse)

        // Melding med annullert avtale
        val avtaleHendelseMelding2: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertMelding)
        val brukernotifikasjonFraAvtaleHendelse2 = opprettBrukernotifikasjonFraAvtaleHendelse(jsonAvtaleAnnullertMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding2, brukernotifikasjonFraAvtaleHendelse2)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        val inaktiverteNotifikasjoner = brukernotifikasjoner.filter { it.type == BrukernotifikasjonType.Inaktivering }
        assertThat(inaktiverteNotifikasjoner).hasSize(1)
        assertThat(brukernotifikasjoner).hasSize(2)

        // Sjekker at brukernotifikajsonen som ble opprettet ifbm med annullertmelding har type inaktivering
        assertThat(brukernotifikasjoner.find { it.id == brukernotifikasjonFraAvtaleHendelse2.id }?.type).isEqualTo(BrukernotifikasjonType.Inaktivering)
        // Sjekker at den brukernotifikasjonen som kom først om godkjenning har bitt inaktivert
        assertThat(brukernotifikasjoner.find { it.id == brukernotifikasjonFraAvtaleHendelse.id }?.status).isEqualTo(BrukernotifikasjonStatus.INAKTIVERT)
    }

    @Test
    fun `skal lage beskjed ved inngåelse av avtale`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleInngåttMelding)
        val brukernotifikasjonFraAvtaleHendelse = opprettBrukernotifikasjonFraAvtaleHendelse(jsonAvtaleInngåttMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding, brukernotifikasjonFraAvtaleHendelse)

        val brukernotifikasjon = brukernotifikasjonRepository.findAll().find { it.id == brukernotifikasjonFraAvtaleHendelse.id }
        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
        assertThat(brukernotifikasjon?.type).isEqualTo(BrukernotifikasjonType.Beskjed)
        assertThat(brukernotifikasjon?.varslingsformål).isEqualTo(Varslingsformål.AVTALE_INNGÅTT)
    }

    @Test
    fun `skal lage beskjed ved annullert avtale`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertMelding)
        val brukernotifikasjonFraAvtaleHendelse = opprettBrukernotifikasjonFraAvtaleHendelse(jsonAvtaleAnnullertMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding, brukernotifikasjonFraAvtaleHendelse)

        val brukernotifikasjon = brukernotifikasjonRepository.findAll().find { it.id == brukernotifikasjonFraAvtaleHendelse.id }
        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
        assertThat(brukernotifikasjon?.type).isEqualTo(BrukernotifikasjonType.Beskjed)
        assertThat(brukernotifikasjon?.varslingsformål).isEqualTo(Varslingsformål.AVTALE_ANNULLERT)
    }

    @Test
    fun `skal lage beskjed ved forlenget avtale`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleForlengetMelding)
        val brukernotifikasjonFraAvtaleHendelse = opprettBrukernotifikasjonFraAvtaleHendelse(jsonAvtaleForlengetMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding, brukernotifikasjonFraAvtaleHendelse)

        val brukernotifikasjon = brukernotifikasjonRepository.findAll().find { it.id == brukernotifikasjonFraAvtaleHendelse.id }
        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
        assertThat(brukernotifikasjon?.type).isEqualTo(BrukernotifikasjonType.Beskjed)
        assertThat(brukernotifikasjon?.varslingsformål).isEqualTo(Varslingsformål.AVTALE_FORLENGET)
    }

    @Test
    fun `skal lage beskjed ved forkortet avtale`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleForkortetMelding)
        val brukernotifikasjonFraAvtaleHendelse = opprettBrukernotifikasjonFraAvtaleHendelse(jsonAvtaleForkortetMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding, brukernotifikasjonFraAvtaleHendelse)

        val brukernotifikasjon = brukernotifikasjonRepository.findAll().find { it.id == brukernotifikasjonFraAvtaleHendelse.id }
        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
        assertThat(brukernotifikasjon?.type).isEqualTo(BrukernotifikasjonType.Beskjed)
        assertThat(brukernotifikasjon?.varslingsformål).isEqualTo(Varslingsformål.AVTALE_FORKORTET)
    }


    fun opprettBrukernotifikasjonFraAvtaleHendelse(avtaleHendelseMelding: String) =
        Brukernotifikasjon(
            id = ulid(),
            avtaleMeldingJson = avtaleHendelseMelding,
            status = BrukernotifikasjonStatus.MOTTATT,
        )
}