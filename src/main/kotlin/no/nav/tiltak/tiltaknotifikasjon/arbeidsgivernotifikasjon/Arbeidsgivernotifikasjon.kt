package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.*
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.SaksStatus
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.Sendevindu
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.inputs.*
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.nysak.DuplikatGrupperingsid
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.utils.Cluster
import java.time.Instant

val client = GraphQLWebClient("https://notifikasjon-fake-produsent-api.ekstern.dev.nav.no")

fun nySak(avtaleHendelseMelding: AvtaleHendelseMelding): NySak {
    val variabler = NySak.Variables(
        grupperingsid = avtaleHendelseMelding.avtaleId.toString(),
        merkelapp = avtaleHendelseMelding.tiltakstype.arbeidsgiverNotifikasjonMerkelapp,
        virksomhetsnummer = avtaleHendelseMelding.bedriftNr,
        mottakere = listOf(MottakerInput(AltinnMottakerInput(avtaleHendelseMelding.tiltakstype.serviceCode, avtaleHendelseMelding.tiltakstype.serviceEdition))),
        tittel = NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET.tekst(avtaleHendelseMelding.tiltakstype),
        lenke = lagLink(avtaleHendelseMelding.avtaleId.toString()),
        initiellStatus = SaksStatus.MOTTATT,
        tidspunkt = Instant.now().toString(),
    )
    val nySak = NySak(variabler)
    return nySak
}

fun nyOppgave(avtaleHendelseMelding: AvtaleHendelseMelding): NyOppgave {
    val oppgaveVariables = NyOppgave.Variables(
        NyOppgaveInput(
            mottaker = MottakerInput(AltinnMottakerInput("1234", "1")),
            mottakere = listOf(MottakerInput(AltinnMottakerInput("1234", "1"))),
            notifikasjon = NotifikasjonInput(
                merkelapp = "Lonnstilskudd",
                tekst = "Ny avtale til godkjenning!",
                lenke = "nav.no"
            ),
            metadata = MetadataInput(
                virksomhetsnummer = "123456789",
                eksternId = "randomUUID",
                opprettetTidspunkt = Instant.now().toString(),
                grupperingsid = "123",
                hardDelete = null
            ),
            eksterneVarsler = listOf(
                EksterntVarselInput(
                    sms = EksterntVarselSmsInput(
                        mottaker = SmsMottakerInput(kontaktinfo = SmsKontaktInfoInput(tlf = "12345678")),
                        smsTekst = "Ny avtale til godkjenning!",
                        sendetidspunkt = SendetidspunktInput(Sendevindu.DAGTID_IKKE_SOENDAG)
                    )
                )
            ),
        )
    )
    val oppgave = NyOppgave(oppgaveVariables)
    return oppgave
}

fun oppgaveUtført(): OppgaveUtfoert {
    val oppgaveUtfoert = OppgaveUtfoert(OppgaveUtfoert.Variables(id = "123"))
    return oppgaveUtfoert
}


fun nyBeskjed(): NyBeskjed {
    val beskjedVariables = NyBeskjed.Variables(
        NyBeskjedInput(
            mottaker = MottakerInput(AltinnMottakerInput("1234", "1")),
            mottakere = listOf(),
            notifikasjon = NotifikasjonInput(
                merkelapp = "Lonnstilskudd",
                tekst = "Ny avtale til godkjenning!",
                lenke = "nav.no"
            ),
            metadata = MetadataInput(
                virksomhetsnummer = "123456789",
                eksternId = "randomUUID",
                opprettetTidspunkt = Instant.now().toString(),
                grupperingsid = "123",
                hardDelete = null
            ),
            eksterneVarsler = listOf(
                EksterntVarselInput(
                    sms = EksterntVarselSmsInput(
                        mottaker = SmsMottakerInput(
                            kontaktinfo = SmsKontaktInfoInput(tlf = "12345678")
                        ),
                        smsTekst = "Ny avtale til godkjenning!",
                        sendetidspunkt = SendetidspunktInput(Sendevindu.DAGTID_IKKE_SOENDAG)
                    )
                )
            ),
        )
    )
    val beskjed = NyBeskjed(beskjedVariables)
    return beskjed

}

fun nySakStatus(sakId: String): NyStatusSak {
    val nySakStatusVariables =
        NyStatusSak.Variables(id = sakId, nyStatus = SaksStatus.FERDIG, tidspunkt = Instant.now().toString())
    val nySakStatus = NyStatusSak(nySakStatusVariables)
    return nySakStatus
}

fun mineNotifikasjoner(merkelapp: String, grupperingsid: String): MineNotifikasjoner {
    val variables = MineNotifikasjoner.Variables(merkelapp = merkelapp, grupperingsid = grupperingsid)
    val mineNotifikasjoner = MineNotifikasjoner(variables)
    return mineNotifikasjoner
}


private fun lagLink(avtaleId: String): String {
    return when (Cluster.current) {
        Cluster.PROD_GCP -> "https://arbeidsgiver.nav.no/tiltaksgjennomforing/avtale/${avtaleId}?part=ARBEIDSGIVER"
        Cluster.DEV_GCP -> "https://tiltaksgjennomforing.ekstern.dev.nav.no/tiltaksgjennomforing/avtale/${avtaleId}?part=ARBEIDSGIVER"
        else -> {
            "https://tiltaksgjennomforing.ekstern.dev.nav.no/tiltaksgjennomforing/avtale/${avtaleId}?part=ARBEIDSGIVER"
        }
    }

}


suspend fun main() {
    val mineNotifikasjoner = mineNotifikasjoner("Lonnstilskudd", "123")
    val result = client.execute(mineNotifikasjoner)
    println(result)
}