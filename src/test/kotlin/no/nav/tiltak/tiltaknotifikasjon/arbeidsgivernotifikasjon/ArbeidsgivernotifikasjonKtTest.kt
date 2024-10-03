package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.deleteAll
import no.nav.tiltak.tiltaknotifikasjon.jsonAvtaleForkortetMelding
import no.nav.tiltak.tiltaknotifikasjon.jsonAvtaleForlengetMelding
import no.nav.tiltak.tiltaknotifikasjon.jsonAvtaleOpprettetMelding
import no.nav.tiltak.tiltaknotifikasjon.kafka.TiltakNotifikasjonKvitteringProdusent
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers


@SpringBootTest
@ActiveProfiles("test-containers", "wiremock")
@Testcontainers
class ArbeidsgivernotifikasjonKtTest{

    @Autowired
    lateinit var arbeidsgiverNotifikasjonService: ArbeidsgiverNotifikasjonService
    @Autowired
    lateinit var arbeidsgivernotifikasjonRepository: ArbeidsgivernotifikasjonRepository
    @MockkBean(relaxed = true)
    lateinit var tiltakNotifikasjonKvitteringProdusent: TiltakNotifikasjonKvitteringProdusent
    @BeforeEach
    fun setup() {
        arbeidsgivernotifikasjonRepository.deleteAll()
    }

    @Test
    fun `nySak_skal_ikke_ha_sent_sms_true`(){
        val avtaleHendelseMelding: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleOpprettetMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMelding)

        val sak = arbeidsgivernotifikasjonRepository.findSakByAvtaleId(avtaleHendelseMelding.avtaleId.toString())
        assertFalse(sak!!.sendtSms())
    }

    @Test
    fun `avtale_forlenget_og_forkortet_skal_ha_sendt_sms_true`(){
        val avtaleHendelseMeldingForlenget: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleForlengetMelding)
        val avtaleHendelseForkortet: AvtaleHendelseMelding = jacksonMapper().readValue(jsonAvtaleForkortetMelding)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseMeldingForlenget)
        arbeidsgiverNotifikasjonService.behandleAvtaleHendelseMelding(avtaleHendelseForkortet)

        val alle = arbeidsgivernotifikasjonRepository.findAll()
        alle.filter { it.type == ArbeidsgivernotifikasjonType.Beskjed }.forEach {
            assertTrue(it.sendtSms())
        }
    }


















}