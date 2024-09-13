package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltak.tiltaknotifikasjon.*
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.NyStatusSak
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleStatus
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.util.*

@SpringBootTest
@ActiveProfiles("test-containers", "wiremock")
@Testcontainers
class ArbeidsgiverNotifikasjonServiceTest {

    @Autowired
    lateinit var arbeidsgiverNotifikasjonService: ArbeidsgiverNotifikasjonService
    @Autowired
    lateinit var arbeidsgivernotifikasjonRepository: ArbeidsgivernotifikasjonRepository
    @BeforeEach
    fun setup() {
        arbeidsgivernotifikasjonRepository.deleteAll()
    }


    @Test
    fun `skal kunne komme flere forlengelse meldinger men like blir ikke prosessert`() {
        val avtaleHendelseMelding1: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleForlengetMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding1)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding1)

        val arbeidsgivernotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        assertThat(arbeidsgivernotifikasjoner).hasSize(1)
    }

    @Test
    fun `skal lage sak og oppgave ved opprettet avtale`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val arbeidsgivernotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        assertThat(arbeidsgivernotifikasjoner).hasSize(2)

        assertThat(arbeidsgivernotifikasjoner[0].varslingsformål).isEqualTo(Varslingsformål.GODKJENNING_AV_AVTALE)
        assertThat(arbeidsgivernotifikasjoner[0].type).isEqualTo(ArbeidsgivernotifikasjonType.Sak)
        assertThat(arbeidsgivernotifikasjoner[0].status).isEqualTo(ArbeidsgivernotifikasjonStatus.SAK_MOTTATT)

        assertThat(arbeidsgivernotifikasjoner[1].varslingsformål).isEqualTo(Varslingsformål.GODKJENNING_AV_AVTALE)
        assertThat(arbeidsgivernotifikasjoner[1].type).isEqualTo(ArbeidsgivernotifikasjonType.Oppgave)
        assertThat(arbeidsgivernotifikasjoner[1].status).isEqualTo(ArbeidsgivernotifikasjonStatus.BEHANDLET)
    }

    @Test
    fun `skal fullføre oppgave ved godkjenning av avtale og lage ny ferdigstill request`() {
        val avtaleHendelseMeldingOpprettet: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingOpprettet)
        val notifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        val oppgave = notifikasjoner.first { it.type == ArbeidsgivernotifikasjonType.Oppgave }
        assertThat(oppgave).isNotNull()
        assertThat(oppgave.status).isEqualTo(ArbeidsgivernotifikasjonStatus.BEHANDLET)

        val avtaleHendelseMeldingGodkjent: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvArbeidsgiverMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingGodkjent)

        val oppgaveEtterGodkjenning = arbeidsgivernotifikasjonRepository.findNotifikasjonByResponseId(oppgave.responseId!!)
        assertThat(oppgaveEtterGodkjenning).isNotNull()
        assertThat(oppgaveEtterGodkjenning!!.status).isEqualTo(ArbeidsgivernotifikasjonStatus.OPPGAVE_FERDIGSTILT)


    }

    @Test
    fun `skal slette alle notifikasjoner ved annullering av avtale og feilregistrering`(){
        val avtaleHendelseMeldingOpprettet: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingOpprettet)
        val notifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        assertThat(notifikasjoner).hasSize(2)
        notifikasjoner.forEach {
            if (it.type == ArbeidsgivernotifikasjonType.Sak) {
                assertThat(it.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SAK_MOTTATT)
            } else {
                assertThat(it.status).isEqualTo(ArbeidsgivernotifikasjonStatus.BEHANDLET)
            }
        }

        val avtaleHendelseMeldingAnnullert: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertFeilregistreringMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingAnnullert)
        val alleIDB = arbeidsgivernotifikasjonRepository.findAll()
        val sak = alleIDB.first { it.type == ArbeidsgivernotifikasjonType.Sak }
        assertThat(sak.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SAK_ANNULLERT)
        // Hvis vi får satt sak til slettet, skal også alle andre notifikasjoner slettes på fager sin side. (cascalde)
        // dette bør vi da sette
        alleIDB.filter { it.type == ArbeidsgivernotifikasjonType.Sak || it.type == ArbeidsgivernotifikasjonType.Beskjed || it.type == ArbeidsgivernotifikasjonType.Oppgave }
            .forEach {
                if (it.type == ArbeidsgivernotifikasjonType.Sak) {
                    assertThat(it.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SAK_ANNULLERT)
                } else {
                    assertThat(it.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SLETTET)
                }
            }
    }

    @Test
    fun `skal lage beskjed ved inngåelse av avtale`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleInngåttMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val arbeidsgivernotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        assertThat(arbeidsgivernotifikasjoner).hasSize(1)
        val arbeidsgivernotifikasjon = arbeidsgivernotifikasjoner.first()
        assertThat(arbeidsgivernotifikasjon.type).isEqualTo(ArbeidsgivernotifikasjonType.Beskjed)
        assertThat(arbeidsgivernotifikasjon.varslingsformål).isEqualTo(Varslingsformål.AVTALE_INNGÅTT)
    }

    @Test
    fun `skal lage beskjed ved annullering av avtale og ikke årsak feilregistrering`() {
        val avtaleHendelseMeldingOpprettet: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)
        val avtaleHendelseMeldingAnnullert: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingOpprettet) // Skal generere 1 sak og 1 oppgave
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingAnnullert) // Skal generere 1 annullerSak, 1 slettOppgaver og 1 beskjed

        val arbeidsgivernotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        assertThat(arbeidsgivernotifikasjoner).hasSize(5)
        val annullertNotifikasjon =
            arbeidsgivernotifikasjoner.first { it.type == ArbeidsgivernotifikasjonType.Beskjed && it.varslingsformål == Varslingsformål.AVTALE_ANNULLERT }
        assertThat(annullertNotifikasjon).isNotNull()
    }

    @Test
    fun `skal sette sak til annullert ved annullering av avtale og ikke årsak feilregistrering`() {
        val avtaleHendelseMeldingOpprettet: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)
        val avtaleHendelseMeldingAnnullert: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingOpprettet) // Skal generere 1 sak og 1 oppgave
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingAnnullert) // Skal generere 1 nyStatusSak, 1 slettOppgaver og 1 beskjed

        val arbeidsgivernotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()

        assertThat(arbeidsgivernotifikasjoner).hasSize(5)
        val sak = arbeidsgivernotifikasjoner.firstOrNull { it.type == ArbeidsgivernotifikasjonType.Sak }
        assertThat(sak).isNotNull()
        assertThat(sak?.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SAK_ANNULLERT)
    }
    @Test
    fun `skal sette sak til annullert ved annullering av avtale og årsak feilregistrering`() {
        val avtaleHendelseMeldingOpprettet: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)
        val avtaleHendelseMeldingAnnullert: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertFeilregistreringMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingOpprettet) // Skal generere 1 sak og 1 oppgave
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingAnnullert) // Skal generere 1 softDeleteSak

        val arbeidsgivernotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()

        assertThat(arbeidsgivernotifikasjoner).hasSize(3)
        val sak = arbeidsgivernotifikasjoner.firstOrNull { it.type == ArbeidsgivernotifikasjonType.Sak }
        assertThat(sak).isNotNull()
        assertThat(sak?.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SAK_ANNULLERT)
    }

    @Test
    fun `skal ikke lage beskjed ved annullering av avtale og årsak feilregistrering`() {
        val avtaleHendelseMeldingOpprettet: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)
        val avtaleHendelseMeldingAnnullert: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertFeilregistreringMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingOpprettet) // Skal generere 1 sak og 1 oppgave
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingAnnullert) // Skal generere 1 slettSak

        val arbeidsgivernotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        assertThat(arbeidsgivernotifikasjoner).hasSize(3)
        val softDeleteSak =
            arbeidsgivernotifikasjoner.first { it.type == ArbeidsgivernotifikasjonType.SoftDeleteSak && it.varslingsformål == Varslingsformål.AVTALE_ANNULLERT }
        assertThat(softDeleteSak).isNotNull()

        val harIkkeBeskjed = arbeidsgivernotifikasjoner.none { it.type == ArbeidsgivernotifikasjonType.Beskjed && it.varslingsformål == Varslingsformål.AVTALE_ANNULLERT }
        assertTrue(harIkkeBeskjed)
    }


    @Test
    fun `skal softDelete sak, sette oppgaver og beskjeder til slettet ved annullert - feilregistrert`() {

        val avtaleHendelseMeldingOpprettet: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)
        val avtaleHendelseMeldingAnnullert: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertFeilregistreringMelding)
        val avtaleHendelseMeldingForlenget: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleForlengetMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingOpprettet) // Opprettet: genererer 1 sak og 1 oppgave
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingForlenget) // Forlenget: generere 1 beskjed
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingAnnullert) // Annullert: generere 1 slettSak

        val arbeidsgivernotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        assertThat(arbeidsgivernotifikasjoner).hasSize(4)

        val oppgave = arbeidsgivernotifikasjoner.first { it.type == ArbeidsgivernotifikasjonType.Oppgave }
        val beskjed = arbeidsgivernotifikasjoner.first { it.type == ArbeidsgivernotifikasjonType.Beskjed }
        assertThat(oppgave.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SLETTET)
        assertThat(beskjed.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SLETTET)
        val softDeleteOppgaverOgBeskjeder = arbeidsgivernotifikasjoner.filter { it.type == ArbeidsgivernotifikasjonType.SoftDeleteNotifikasjon }
        assertThat(softDeleteOppgaverOgBeskjeder).hasSize(0) // Softdelete går automatisk når vi softDeleter sak via cascade hos fager.
    }

    @Test
    fun `Avtale annullert, finnes ingen sak, skal softdelete eventuelle oppgaver og beskjeder`() {
        // Dette er casen hvor det er opprettet oppgaver og beskjeder før denne appen går live, så kommer det en annulleringsmelding.
        val avtaleHendelseMeldingAnnullert: AvtaleHendelseMelding = jacksonMapper().readValue<AvtaleHendelseMelding>(jsonAvtaleAnnullertFeilregistreringMelding).copy(
            avtaleId = UUID.randomUUID()
        )
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingAnnullert)

        val arbeidsgivernotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        assertThat(arbeidsgivernotifikasjoner).hasSize(2)
        arbeidsgivernotifikasjoner.forEach { assertThat(it.type == ArbeidsgivernotifikasjonType.SoftDeleteNotifikasjon) }
    }

    @Test
    fun `skal sette sak til ferdig når en avtale endrer status til AVSLUTTET`() {
        val opprettetMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)

        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleForlengetMelding)
        val avtaleHendelsesMeldingStatusendringFerdig = avtaleHendelseMelding.copy(
            startDato = LocalDate.now().minusDays(10),
            sluttDato = LocalDate.now().minusDays(1),
            avtaleStatus = AvtaleStatus.AVSLUTTET,
            hendelseType = HendelseType.STATUSENDRING
        )
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(opprettetMelding) // Generer sak og oppgave
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelsesMeldingStatusendringFerdig) // Generer sakstatusEndret
        val alleIDb = arbeidsgivernotifikasjonRepository.findAll()
        assertThat(alleIDb).hasSize(3)
    }

    @Test
    fun `skal sette status på sak tilbake til mottatt når en avtale går fra avsluttet tilbake til aktiv`() {
        // En avsluttet avtale kan forlenges og bli "aktiv" igjen. Hard delete bør fjernes også da fra saken.
        val opprettetMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)
        val forkortetMelding: AvtaleHendelseMelding = jacksonMapper().readValue<AvtaleHendelseMelding>(jsonAvtaleForkortetMelding).copy(
            startDato = LocalDate.now().minusDays(30),
            sluttDato = LocalDate.now().minusDays(1),
            avtaleStatus = AvtaleStatus.AVSLUTTET
        )
        val forlengetMelding: AvtaleHendelseMelding = jacksonMapper().readValue<AvtaleHendelseMelding>(jsonAvtaleForlengetMelding).copy(
            startDato = forkortetMelding.startDato,
            sluttDato = LocalDate.now().plusDays(10),
            avtaleStatus = AvtaleStatus.GJENNOMFØRES,
        )

        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(opprettetMelding) // Generer sak og oppgave
        val alleNotifikasjoner1 = arbeidsgivernotifikasjonRepository.findAll()
        alleNotifikasjoner1.find { it.type == ArbeidsgivernotifikasjonType.Sak }?.let {
            assertThat(it.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SAK_MOTTATT)
        }

        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(forkortetMelding) // Generer sakstatusEndret
        val alleNotifikasjoner2 = arbeidsgivernotifikasjonRepository.findAll()
        alleNotifikasjoner2.find { it.type == ArbeidsgivernotifikasjonType.Sak }?.let {
            assertThat(it.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SAK_FERDIG)
        }

        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(forlengetMelding) // Generer sakstatusEndret
        val alleNotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        alleNotifikasjoner.find { it.type == ArbeidsgivernotifikasjonType.Sak }?.let {
            assertThat(it.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SAK_MOTTATT)
        }

    }

    @Test
    fun `skal lage beskjed ved forlenget avtale`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleForlengetMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val arbeidsgivernotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        assertThat(arbeidsgivernotifikasjoner).hasSize(1)
        val arbeidsgivernotifikasjon = arbeidsgivernotifikasjoner.first()
        assertThat(arbeidsgivernotifikasjon.type).isEqualTo(ArbeidsgivernotifikasjonType.Beskjed)
        assertThat(arbeidsgivernotifikasjon.varslingsformål).isEqualTo(Varslingsformål.AVTALE_FORLENGET)
    }

    @Test
    fun `skal lage beskjed ved forkortet avtale`() {
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleForkortetMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val arbeidsgivernotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        assertThat(arbeidsgivernotifikasjoner).hasSize(1)
        val arbeidsgivernotifikasjon = arbeidsgivernotifikasjoner.first()
        assertThat(arbeidsgivernotifikasjon.type).isEqualTo(ArbeidsgivernotifikasjonType.Beskjed)
        assertThat(arbeidsgivernotifikasjon.varslingsformål).isEqualTo(Varslingsformål.AVTALE_FORKORTET)
    }

    @Test
    fun `skal sette hardDelete på nySakStatusQuery og NySakStatus Entitet ved statusendring til ferdig`() {
        val opprettetMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)
        val statusEndringMelding: AvtaleHendelseMelding = jacksonMapper().readValue<AvtaleHendelseMelding>(jsonAvtaleForlengetMelding).copy(
            startDato = LocalDate.now().minusDays(10),
            sluttDato = LocalDate.now().minusDays(1),
            avtaleStatus = AvtaleStatus.AVSLUTTET,
            hendelseType = HendelseType.STATUSENDRING
        )
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(opprettetMelding) // Generer sak og oppgave
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(statusEndringMelding) // Generer sakstatusEndret

        val arbeidsgivernotifikasjoner = arbeidsgivernotifikasjonRepository.findAll()
        assertThat(arbeidsgivernotifikasjoner).hasSize(3) // sak, oppgave, beskjed
        val nySakStatus = arbeidsgivernotifikasjoner.find { it.type == ArbeidsgivernotifikasjonType.NySakStatus}
        val sak = arbeidsgivernotifikasjoner.find { it.type == ArbeidsgivernotifikasjonType.Sak}

        val query = jacksonMapper().readValue<NyStatusSak>(nySakStatus!!.arbeidsgivernotifikasjonJson!!) // Query
        assertThat(query.variables.hardDelete).isNotNull()
        assertThat(sak?.hardDeleteSkedulertTidspunkt).isNotNull() // entitet
    }
    @Test
    fun `skal utvide hardDelete på sak når den går fra ferdig til mottatt`() {
        val opprettetMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)
        val statusEndringMelding: AvtaleHendelseMelding = jacksonMapper().readValue<AvtaleHendelseMelding>(jsonAvtaleForlengetMelding).copy(
            startDato = LocalDate.now().minusDays(10),
            sluttDato = LocalDate.now().minusDays(1),
            avtaleStatus = AvtaleStatus.AVSLUTTET,
            hendelseType = HendelseType.STATUSENDRING
        )
        val forlengetMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleForlengetMelding)

        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(opprettetMelding) // Generer sak og oppgave
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(statusEndringMelding) // Generer sakstatusEndret
        val sak = arbeidsgivernotifikasjonRepository.findSakByAvtaleId(opprettetMelding.avtaleId.toString())
        assertThat(sak?.hardDeleteSkedulertTidspunkt).isNotNull()
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(forlengetMelding) // Generer sakstatusEndret
        val sak2 = arbeidsgivernotifikasjonRepository.findSakByAvtaleId(opprettetMelding.avtaleId.toString())
        assertThat(sak2?.hardDeleteSkedulertTidspunkt).isNotNull()

        assertThat(sak!!.hardDeleteSkedulertTidspunkt!!.isBefore(sak2!!.hardDeleteSkedulertTidspunkt)).isTrue()


    }


}