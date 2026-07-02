package no.nav.tiltak.tiltaknotifikasjon.kafka

import com.ninjasquad.springmockk.MockkBean
import io.getunleash.Unleash
import io.mockk.every
import io.mockk.mockk
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgiverRefusjonKontaktpersonRepository
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgiverRefusjonNotifikasjonRepository
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgivernotifikasjonProperties
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgivernotifikasjonStatus
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.ArbeidsgivernotifikasjonType
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.RefusjonKontaktpersonEntitet
import no.nav.tiltak.tiltaknotifikasjon.avtale.HendelseType
import no.nav.tiltak.tiltaknotifikasjon.avtale.Tiltakstype
import no.nav.tiltak.tiltaknotifikasjon.utils.jacksonMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Unit-tester for [RefusjonVarselConsumer]. Bruker ekte Postgres via testcontainers (repositories)
 * og wiremock som stub for GraphQL-kallene mot arbeidsgivernotifikasjon (sak/beskjed), på samme måte
 * som f.eks. ArbeidsgiverNotifikasjonServiceTest.
 *
 * RefusjonVarselConsumer og Unleash er kun registrert som Spring-beans i profilene prod-gcp/dev-gcp/dockercompose,
 * så vi kan ikke @Autowired disse under test-containers-profilen. Vi instansierer derfor consumeren manuelt
 * med de autowirede (ekte) repositoriene og en mocket Unleash.
 */
@SpringBootTest
@ActiveProfiles("test-containers", "wiremock")
@Testcontainers
class RefusjonVarselConsumerTest {

    @Autowired
    lateinit var kontaktpersonRepository: ArbeidsgiverRefusjonKontaktpersonRepository

    @Autowired
    lateinit var notifikasjonRepository: ArbeidsgiverRefusjonNotifikasjonRepository

    @Autowired
    lateinit var arbeidsgivernotifikasjonProperties: ArbeidsgivernotifikasjonProperties

    // Ikke brukt direkte i disse testene, men nødvendig for at Spring skal kunne gjenbruke samme cachede
    // ApplicationContext (og dermed samme wiremock-instans/port) som de andre testene i "wiremock"-profilen.
    @MockkBean(relaxed = true)
    lateinit var tiltakNotifikasjonKvitteringProdusent: TiltakNotifikasjonKvitteringProdusent

    private val unleash = mockk<Unleash>()
    private lateinit var consumer: RefusjonVarselConsumer

    /** Fast avtaleId som matcher wiremock-stubben for "sak finnes fra før" (se arbeidsgivernotifikasjon-graphql.json) */
    private val avtaleIdMedEksisterendeSak = UUID.fromString("d29ee14b-27b8-4b1e-9e10-000000000001")

    @BeforeEach
    fun setup() {
        every { unleash.isEnabled(any()) } returns true
        consumer = RefusjonVarselConsumer(
            kontaktpersonRepository,
            notifikasjonRepository,
            arbeidsgivernotifikasjonProperties,
            WebClient.builder(),
            unleash,
        )
    }

    private fun kontaktperson(avtaleId: UUID) = RefusjonKontaktpersonEntitet(
        avtaleId = avtaleId,
        bedriftNr = "999999999",
        refusjonKontaktpersonTlf = "99999999",
        arbeidsgiverOnskerOgsaVarsling = false,
        arbeidsgiverTlf = "88888888",
        tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
        avtaleInnholdVersjon = 1,
        avtaleHendelseType = HendelseType.AVTALE_INNGÅTT,
        avtaleHendelseSistEndret = Instant.now(),
        topicOffset = 1,
        innlestTidspunkt = Instant.now(),
        deltakerFornavn = "Donald",
        deltakerEtternavn = "Duck",
    )

    private fun melding(
        avtaleId: UUID = UUID.randomUUID(),
        refusjonId: String = UUID.randomUUID().toString(),
        refusjonVarselType: RefusjonVarselType = RefusjonVarselType.KLAR,
    ) = RefusjonVarselMelding(
        avtaleId = avtaleId,
        refusjonId = refusjonId,
        tilskuddsperiodeId = UUID.randomUUID(),
        refusjonVarselType = refusjonVarselType,
        fristForGodkjenning = LocalDate.now().plusDays(14),
        avtaleNr = 1,
        løpenummer = 1,
        tilskuddFom = LocalDate.now(),
        tilskuddTom = LocalDate.now().plusMonths(1),
        refusjonsnummer = "1#1",
    )

    @Suppress("UNCHECKED_CAST")
    private fun consumerRecord(refusjonVarselMelding: RefusjonVarselMelding, offset: Long = 42L, key: String? = "kafka-key") =
        ConsumerRecord<String, String>(Topics.TILTAK_VARSEL, 0, offset, uncheckedCast(key), jacksonMapper().writeValueAsString(refusjonVarselMelding))

    /** Kafka tillater null som key selv om ConsumerRecord sin generiske type ikke er nullable. Denne casten omgår Kotlins null-sjekk (erasure) slik at vi kan simulere en melding uten key. */
    @Suppress("UNCHECKED_CAST")
    private fun <T> uncheckedCast(value: Any?): T = value as T

    @Test
    fun `skal hoppe over konsumering når feature-toggle er avslått`() {
        every { unleash.isEnabled(any()) } returns false
        val avtaleId = UUID.randomUUID()

        consumer.konsumer(consumerRecord(melding(avtaleId = avtaleId)))

        assertThat(kontaktpersonRepository.findByAvtaleId(avtaleId)).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = RefusjonVarselType::class, names = ["REVARSEL", "FRIST_FORLENGET", "KORRIGERT"])
    fun `skal filtrere bort varseltyper som ikke er KLAR`(varselType: RefusjonVarselType) {
        val avtaleId = UUID.randomUUID()
        kontaktpersonRepository.save(kontaktperson(avtaleId))

        consumer.konsumer(consumerRecord(melding(avtaleId = avtaleId, refusjonVarselType = varselType)))

        assertThat(notifikasjonRepository.findAll().none { it.avtaleId == avtaleId.toString() }).isTrue()
    }

    @Test
    fun `skal skippe melding uten feil når kontaktperson ikke finnes for avtalen`() {
        val avtaleIdUtenKontaktperson = UUID.randomUUID()

        consumer.konsumer(consumerRecord(melding(avtaleId = avtaleIdUtenKontaktperson)))

        assertThat(notifikasjonRepository.findAll().none { it.avtaleId == avtaleIdUtenKontaktperson.toString() }).isTrue()
    }

    @Test
    fun `skal ikke opprette ny sak når sak allerede finnes (idempotens)`() {
        kontaktpersonRepository.save(kontaktperson(avtaleIdMedEksisterendeSak))
        val refusjonId = UUID.randomUUID().toString()

        consumer.konsumer(consumerRecord(melding(avtaleId = avtaleIdMedEksisterendeSak, refusjonId = refusjonId)))

        val notifikasjoner = notifikasjonRepository.findAllByRefusjonId(refusjonId)
        assertThat(notifikasjoner.none { it.type == ArbeidsgivernotifikasjonType.Sak }).isTrue()
        val beskjed = notifikasjoner.find { it.type == ArbeidsgivernotifikasjonType.Beskjed }
        assertThat(beskjed).isNotNull()
        assertThat(beskjed!!.status).isEqualTo(ArbeidsgivernotifikasjonStatus.BEHANDLET)
        assertThat(beskjed.responseId).isNotNull()
        assertThat(beskjed.sendtTidspunkt).isNotNull()
    }

    @Test
    fun `skal opprette ny sak når sak ikke finnes fra før`() {
        val avtaleId = UUID.randomUUID()
        kontaktpersonRepository.save(kontaktperson(avtaleId))
        val refusjonId = UUID.randomUUID().toString()

        consumer.konsumer(consumerRecord(melding(avtaleId = avtaleId, refusjonId = refusjonId)))

        val notifikasjoner = notifikasjonRepository.findAllByRefusjonId(refusjonId)
        val sak = notifikasjoner.find { it.type == ArbeidsgivernotifikasjonType.Sak }
        assertThat(sak).isNotNull()
        assertThat(sak!!.status).isEqualTo(ArbeidsgivernotifikasjonStatus.SAK_MOTTATT)
        assertThat(sak.responseId).isNotNull()
        assertThat(sak.sendtTidspunkt).isNotNull()
        val beskjed = notifikasjoner.find { it.type == ArbeidsgivernotifikasjonType.Beskjed }
        assertThat(beskjed).isNotNull()
        assertThat(beskjed!!.responseId).isNotNull()
        assertThat(beskjed.sendtTidspunkt).isNotNull()
    }

    @Test
    fun `skal ikke opprette ny beskjed når notifikasjon for refusjonId og varseltype allerede finnes (idempotens)`() {
        kontaktpersonRepository.save(kontaktperson(avtaleIdMedEksisterendeSak))
        val refusjonId = UUID.randomUUID().toString()
        val meldingVerdi = melding(avtaleId = avtaleIdMedEksisterendeSak, refusjonId = refusjonId)

        // Første melding oppretter beskjed (sak finnes allerede fra før via wiremock-stub)
        consumer.konsumer(consumerRecord(meldingVerdi))
        val antallEtterFørsteMelding = notifikasjonRepository.findAllByRefusjonId(refusjonId).size

        // Andre melding med samme refusjonId og varseltype skal ikke opprette ny beskjed
        consumer.konsumer(consumerRecord(meldingVerdi, offset = 43L))
        val notifikasjonerEtterAndreMelding = notifikasjonRepository.findAllByRefusjonId(refusjonId)

        assertThat(notifikasjonerEtterAndreMelding).hasSize(antallEtterFørsteMelding)
        assertThat(notifikasjonerEtterAndreMelding.filter { it.type == ArbeidsgivernotifikasjonType.Beskjed }).hasSize(1)
    }

    @Test
    fun `skal lagre feilet notifikasjon når konsumering kaster exception`() {
        // Ugyldig json fører til at deserialisering feiler og treffer catch-blokken i konsumer().
        val ugyldigJson = "dette er ikke gyldig json"
        val record = ConsumerRecord(Topics.TILTAK_VARSEL, 0, 123L, "min-kafka-key", ugyldigJson)

        consumer.konsumer(record)

        val feiledeNotifikasjoner = notifikasjonRepository.findAll().filter {
            it.status == ArbeidsgivernotifikasjonStatus.FEILET_VED_BEHANDLING && it.kafkaOffset == 123L
        }
        assertThat(feiledeNotifikasjoner).hasSize(1)
        val feiletNotifikasjon = feiledeNotifikasjoner.first()
        assertThat(feiletNotifikasjon.type).isEqualTo(ArbeidsgivernotifikasjonType.Ukjent)
        assertThat(feiletNotifikasjon.kafkaKey).isEqualTo("min-kafka-key")
        assertThat(feiletNotifikasjon.feilmelding).isNotBlank()
        assertThat(feiletNotifikasjon.arbeidsgivernotifikasjonJson).isEqualTo(ugyldigJson)
    }

    @Test
    fun `skal lagre feilet notifikasjon med tomt kafka-key når key mangler`() {
        val record = consumerRecord(melding(), offset = 321L, key = null)
        every { unleash.isEnabled(any()) } throws RuntimeException("Unleash er nede")

        consumer.konsumer(record)

        val feiletNotifikasjon = notifikasjonRepository.findAll().first { it.kafkaOffset == 321L }
        assertThat(feiletNotifikasjon.kafkaKey).isEqualTo("<null>")
        assertThat(feiletNotifikasjon.status).isEqualTo(ArbeidsgivernotifikasjonStatus.FEILET_VED_BEHANDLING)
    }

    @Test
    fun `finnesNotifikasjon skal returnere true når type og varselformål matcher eksisterende notifikasjon`() {
        val avtaleId = UUID.randomUUID()
        kontaktpersonRepository.save(kontaktperson(avtaleId))
        val refusjonId = UUID.randomUUID().toString()
        consumer.konsumer(consumerRecord(melding(avtaleId = avtaleId, refusjonId = refusjonId)))

        assertThat(consumer.finnesNotifikasjon(refusjonId, RefusjonVarselType.KLAR, ArbeidsgivernotifikasjonType.Beskjed)).isTrue()
    }

    @Test
    fun `finnesNotifikasjon skal returnere false når det ikke finnes notifikasjon med matchende type og varselformål`() {
        assertThat(consumer.finnesNotifikasjon(UUID.randomUUID().toString(), RefusjonVarselType.KLAR, ArbeidsgivernotifikasjonType.Beskjed)).isFalse()
    }

    @Test
    fun `finnesNotifikasjon skal returnere false når kun typen ikke matcher (Sak vs Beskjed)`() {
        val avtaleId = UUID.randomUUID()
        kontaktpersonRepository.save(kontaktperson(avtaleId))
        val refusjonId = UUID.randomUUID().toString()
        // Første melding oppretter både Sak og Beskjed for avtalen
        consumer.konsumer(consumerRecord(melding(avtaleId = avtaleId, refusjonId = refusjonId)))

        // Det finnes en Sak med samme varslingsformål, men vi spør spesifikt etter en Sak (som allerede finnes)
        assertThat(consumer.finnesNotifikasjon(refusjonId, RefusjonVarselType.KLAR, ArbeidsgivernotifikasjonType.Sak)).isTrue()
        // Og en Oppgave, som ikke finnes, skal gi false selv om varseltype matcher
        assertThat(consumer.finnesNotifikasjon(refusjonId, RefusjonVarselType.KLAR, ArbeidsgivernotifikasjonType.Oppgave)).isFalse()
    }
}
