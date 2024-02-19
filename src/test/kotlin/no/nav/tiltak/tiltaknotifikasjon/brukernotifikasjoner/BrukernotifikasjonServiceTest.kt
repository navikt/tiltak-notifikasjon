package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import no.nav.tiltak.tiltaknotifikasjon.*
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
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
    fun `skal lage oppgave med formål godkjenning av avtale når arbeidsgiver godkjenner`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvArbeidsgiverMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
        assertThat(brukernotifikasjoner[0].varslingsformål).isEqualTo(Varslingsformål.GODKJENNING_AV_AVTALE)
        assertThat(brukernotifikasjoner[0].type).isEqualTo(BrukernotifikasjonType.Oppgave)
        assertThat(brukernotifikasjoner[0].status).isEqualTo(BrukernotifikasjonStatus.BEHANDLET)
    }

    @Test
    fun `skal lage kun 1 oppgave ved flere godkjent av arbeidsgiver status meldinger`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvArbeidsgiverMelding)

        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
    }

    @Test
    fun `skal inaktivere oppgave om godkjenning når deltaker har godkjent`() {
        // Godkjent av arbeidsgiver melding
        val avtaleHendelseMelding1: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvArbeidsgiverMelding)

        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding1)

        // Melding med godkjent av deltaker
        val avtaleHendelseMelding2: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvDeltaker)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding2)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        val inaktiverteNotifikasjoner = brukernotifikasjoner.filter { it.type == BrukernotifikasjonType.Inaktivering }
        assertThat(inaktiverteNotifikasjoner).hasSize(1)
        assertThat(brukernotifikasjoner).hasSize(2)
    }

    @Test
    fun `skal inaktivere oppgave om godkjenning når avtalen annulleres`() {
        // Godkjent av arbeidsgiver melding
        val avtaleHendelseMelding1: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvArbeidsgiverMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding1)

        // Melding med annullert avtale
        val avtaleHendelseMelding2: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding2)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()

        // Sjekk at det er en beskjed om annullering
        val beskjedOmAnnullering = brukernotifikasjoner.filter { it.type == BrukernotifikasjonType.Beskjed && it.varslingsformål == Varslingsformål.AVTALE_ANNULLERT }
        assertThat(beskjedOmAnnullering).hasSize(1)

        // Sjekker at den brukernotifikasjonen som kom først om godkjenning har bitt inaktivert
        val inaktiverteOppgaverOmGodkjenning = brukernotifikasjoner.filter { it.type == BrukernotifikasjonType.Oppgave && it.status == BrukernotifikasjonStatus.INAKTIVERT }
        assertThat(inaktiverteOppgaverOmGodkjenning).hasSize(1)

        // Sjekker at brukernotifikajsonen som ble opprettet ifbm med annullertmelding har type inaktivering
        val inaktiveringsMelding = brukernotifikasjoner.filter { it.type == BrukernotifikasjonType.Inaktivering && it.status == BrukernotifikasjonStatus.BEHANDLET }
        assertThat(inaktiveringsMelding).hasSize(1)

        // Skal være 3 brukernotifikasjoner totalt: 1 oppgave om godkjenning, 1 beskjed om annullering og 1 inaktivering
        assertThat(brukernotifikasjoner).hasSize(3)
    }

    @Test
    fun `skal kun sende 1 beskjed om annullering ved flere like meldinger` () {
        // En avtale kan kun annulleres 1 gang
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
    }

    @Test
    fun `skal lage beskjed ved inngåelse av avtale`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleInngåttMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
        val brukernotifikasjon = brukernotifikasjoner.first()
        assertThat(brukernotifikasjon.type).isEqualTo(BrukernotifikasjonType.Beskjed)
        assertThat(brukernotifikasjon.varslingsformål).isEqualTo(Varslingsformål.AVTALE_INNGÅTT)
    }

    @Test
    fun `skal lage beskjed ved annullert avtale`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
        val brukernotifikasjon = brukernotifikasjoner.first()
        assertThat(brukernotifikasjon.type).isEqualTo(BrukernotifikasjonType.Beskjed)
        assertThat(brukernotifikasjon.varslingsformål).isEqualTo(Varslingsformål.AVTALE_ANNULLERT)
    }

    @Test
    fun `skal lage beskjed ved forlenget avtale`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleForlengetMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
        val brukernotifikasjon = brukernotifikasjoner.first()
        assertThat(brukernotifikasjon.type).isEqualTo(BrukernotifikasjonType.Beskjed)
        assertThat(brukernotifikasjon.varslingsformål).isEqualTo(Varslingsformål.AVTALE_FORLENGET)
    }

    @Test
    fun `skal lage beskjed ved forkortet avtale`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleForkortetMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(1)
        val brukernotifikasjon = brukernotifikasjoner.first()
        assertThat(brukernotifikasjon.type).isEqualTo(BrukernotifikasjonType.Beskjed)
        assertThat(brukernotifikasjon.varslingsformål).isEqualTo(Varslingsformål.AVTALE_FORKORTET)
    }

    @Test
    fun `skal ikke sende annullert beskjed ved annullert grunn feilregistrering`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertFeilregistreringMelding)
        brukernotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val brukernotifikasjoner = brukernotifikasjonRepository.findAll()
        assertThat(brukernotifikasjoner).hasSize(0)
    }


}