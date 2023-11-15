package no.nav.tiltak.tiltaknotifikasjon.brukernotifikasjoner

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tms.varsel.action.Varseltype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BeskjedOppgaveTest {
    private val mapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Test
    fun `Avtalemelding Godkjent av veileder - skal lage oppgave i brukernotifikajson`() {
        val jsonGodkjentAvVeileder = """
            {"hendelseType":"GODKJENT_AV_VEILEDER","avtaleStatus":"MANGLER_GODKJENNING","deltakerFnr":"00000000000","mentorFnr":null,"bedriftNr":"999999999","veilederNavIdent":"X123456","tiltakstype":"MIDLERTIDIG_LONNSTILSKUDD","opprettetTidspunkt":"2023-11-14T13:31:49.800338","avtaleId":"384e1299-a9f6-4de5-8699-a30cdec543e7","avtaleNr":26,"sistEndret":"2023-11-14T12:38:34.375684343Z","annullertTidspunkt":null,"annullertGrunn":null,"slettemerket":false,"opprettetAvArbeidsgiver":false,"enhetGeografisk":"0313","enhetsnavnGeografisk":"NAV St. Hanshaugen","enhetOppfolging":"0906","enhetsnavnOppfolging":"NAV Agder","godkjentForEtterregistrering":false,"kvalifiseringsgruppe":"VARIG","formidlingsgruppe":"ARBS","tilskuddPeriode":[],"feilregistrert":false,"versjon":1,"deltakerFornavn":"Donald","deltakerEtternavn":"Duck","deltakerTlf":"12121212","bedriftNavn":"Saltrød og Høneby","arbeidsgiverFornavn":"For","arbeidsgiverEtternavn":"Navn","arbeidsgiverTlf":"12121212","veilederFornavn":"Nav","veilederEtternavn":"Etternavn","veilederTlf":"12121212","oppfolging":"Dette er oppfølgingen","tilrettelegging":"Og dette er tilretteleggingen","startDato":"2023-11-07","sluttDato":"2024-06-30","stillingprosent":100,"journalpostId":null,"arbeidsoppgaver":"Oppgaver for å arbeide","stillingstittel":"Garver","stillingStyrk08":7535,"stillingKonseptId":69158,"antallDagerPerUke":5,"refusjonKontaktperson":null,"mentorFornavn":null,"mentorEtternavn":null,"mentorOppgaver":null,"mentorAntallTimer":null,"mentorTimelonn":null,"mentorTlf":null,"arbeidsgiverKontonummer":"10000008162","lonnstilskuddProsent":60,"manedslonn":40000,"feriepengesats":0.12,"arbeidsgiveravgift":0.141,"harFamilietilknytning":true,"familietilknytningForklaring":"Jeg er slekt med personen på tiltak","feriepengerBelop":4800,"otpSats":0.06,"otpBelop":2688,"arbeidsgiveravgiftBelop":6696,"sumLonnsutgifter":54184,"sumLonnstilskudd":32510,"manedslonn100pst":54184,"sumLønnstilskuddRedusert":null,"datoForRedusertProsent":null,"stillingstype":"FAST","maal":[],"inkluderingstilskuddsutgift":[],"inkluderingstilskuddBegrunnelse":null,"inkluderingstilskuddTotalBeløp":0,"godkjentAvDeltaker":"2023-11-14T13:38:17.209559","godkjentTaushetserklæringAvMentor":null,"godkjentAvArbeidsgiver":"2023-11-14T13:35:54.158615","godkjentAvVeileder":"2023-11-14T13:38:34.375653979","godkjentAvBeslutter":null,"avtaleInngått":null,"ikrafttredelsestidspunkt":"2023-11-14T13:38:34.375653979","godkjentAvNavIdent":"X123456","godkjentAvBeslutterNavIdent":null,"enhetKostnadssted":null,"enhetsnavnKostnadssted":null,"godkjentPaVegneGrunn":null,"godkjentPaVegneAv":false,"godkjentPaVegneAvArbeidsgiverGrunn":null,"godkjentPaVegneAvArbeidsgiver":false,"innholdType":"INNGÅ","utførtAv":"X123456","utførtAvRolle":"VEILEDER"}
        """.trimIndent()
        val melding: AvtaleHendelseMelding = mapper.readValue(jsonGodkjentAvVeileder)

        // Burde kanskje vært litt static
        val oppgaveTilBrukerNotifikasjon = lagOppgave(melding.deltakerFnr, melding.avtaleId.toString())

        assertThat(oppgaveTilBrukerNotifikasjon).isNotNull()
        assertThat(oppgaveTilBrukerNotifikasjon.type).isEqualTo(Varseltype.Oppgave)
        println(oppgaveTilBrukerNotifikasjon.json)
        assertThat(oppgaveTilBrukerNotifikasjon.json).isNotEmpty()
    }
}