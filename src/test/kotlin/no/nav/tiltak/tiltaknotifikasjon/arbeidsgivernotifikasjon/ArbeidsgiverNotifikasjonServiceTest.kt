package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltak.tiltaknotifikasjon.*
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers

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
        assertThat(arbeidsgivernotifikasjoner[0].status).isEqualTo(ArbeidsgivernotifikasjonStatus.BEHANDLET)

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

        val avtaleHendelseMeldingGodkjent: AvtaleHendelseMelding = jacksonMapper().readValue(jsonGodkjentAvArbeidsgiverMelding) // TODO: Samme avtaleid på json
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
        notifikasjoner.forEach { assertThat(it.status).isEqualTo(ArbeidsgivernotifikasjonStatus.BEHANDLET) }

        val avtaleHendelseMeldingAnnullert: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleAnnullertFeilregistreringMelding) // TODO: Samme avtaleid på json
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingAnnullert)
        val alleIDB = arbeidsgivernotifikasjonRepository.findAll()
        val sak = alleIDB.first { it.type == ArbeidsgivernotifikasjonType.Sak }
        assertThat(sak.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SLETTET)
        // Hvis vi får satt sak til slettet, skal også alle andre notifikasjoner slettes på fager sin side. (cascalde)
        // dette bør vi da sette
        alleIDB.filter { it.type == ArbeidsgivernotifikasjonType.Sak || it.type == ArbeidsgivernotifikasjonType.Beskjed || it.type == ArbeidsgivernotifikasjonType.Oppgave } .forEach {
            assertThat(it.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SLETTET)
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
    }


    @Test
    fun `skal softDelete sak, oppgave og beskjeder ved annulering av avtale med årsak feilregistrering`() {

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
    fun `skal sette sak til ferdig når avtalen endrer status til avsluttet`() {

    }
    @Test
    fun `skal sette status på sak tilbake til mottatt når en avtale går fra avsluttet tilbake til aktiv`() {

    }

}