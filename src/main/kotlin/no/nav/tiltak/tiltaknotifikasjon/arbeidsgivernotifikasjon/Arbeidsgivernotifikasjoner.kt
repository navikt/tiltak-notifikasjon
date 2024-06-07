package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.*
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.SaksStatus
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.Sendevindu
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.inputs.*
import no.nav.tiltak.tiltaknotifikasjon.avtale.*
import no.nav.tiltak.tiltaknotifikasjon.utils.Cluster
import java.time.Instant
import java.util.*


fun nySak(avtaleHendelseMelding: AvtaleHendelseMelding): NySak {
    val variabler = NySak.Variables(
        grupperingsid = avtaleHendelseMelding.grupperingsId(),
        merkelapp = avtaleHendelseMelding.tiltakstype.arbeidsgiverNotifikasjonMerkelapp,
        virksomhetsnummer = avtaleHendelseMelding.bedriftNr,
        mottakere = listOf(MottakerInput(AltinnMottakerInput(avtaleHendelseMelding.tiltakstype.serviceCode, avtaleHendelseMelding.tiltakstype.serviceEdition))),
        tittel = NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET_SAK.tekst(avtaleHendelseMelding.tiltakstype),
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
            mottakere = listOf(MottakerInput(AltinnMottakerInput(avtaleHendelseMelding.tiltakstype.serviceCode, avtaleHendelseMelding.tiltakstype.serviceEdition))),
            notifikasjon = NotifikasjonInput(
                merkelapp = avtaleHendelseMelding.tiltakstype.arbeidsgiverNotifikasjonMerkelapp,
                tekst =  avtaleHendelseMelding.lagArbeidsgivernotifikasjonTekst(false),
                lenke = lagLink(avtaleHendelseMelding.avtaleId.toString())
            ),
            metadata = MetadataInput(
                virksomhetsnummer = avtaleHendelseMelding.bedriftNr,
                eksternId = avtaleHendelseMelding.eksternId(),
                opprettetTidspunkt = Instant.now().toString(),
                grupperingsid = avtaleHendelseMelding.grupperingsId(),
                hardDelete = null
            ),
            eksterneVarsler = if (avtaleHendelseMelding.hendelseType.skalSendeSmsTilArbeidsgiver()) listOf(
                EksterntVarselInput(
                    sms = EksterntVarselSmsInput(
                        mottaker = SmsMottakerInput(
                            kontaktinfo = SmsKontaktInfoInput(tlf = avtaleHendelseMelding.arbeidsgiverTlf!!)
                        ),
                        smsTekst = "Hei! Du har fått en ny oppgave om tiltak. Logg inn på Min side - arbeidsgiver hos NAV for å se hva det gjelder. Vennlig hilsen NAV", // TODO: Finne ut av hva som skal stå i sms
                        sendetidspunkt = SendetidspunktInput(Sendevindu.DAGTID_IKKE_SOENDAG)
                    )
                )
            ) else emptyList()
        )
    )
    val oppgave = NyOppgave(oppgaveVariables)
    return oppgave
}

// TODO: Sjekke om man kan lukke en sak og dermed alle oppgaver på saken.
fun oppgaveUtført(id: String): OppgaveUtfoert = OppgaveUtfoert(OppgaveUtfoert.Variables(id))


fun nyBeskjed(avtaleHendelseMelding: AvtaleHendelseMelding): NyBeskjed {
    val beskjedVariables = NyBeskjed.Variables(
        NyBeskjedInput(
            mottakere = listOf(MottakerInput(AltinnMottakerInput(avtaleHendelseMelding.tiltakstype.serviceCode, avtaleHendelseMelding.tiltakstype.serviceEdition))),
            notifikasjon = NotifikasjonInput(
                merkelapp = avtaleHendelseMelding.tiltakstype.arbeidsgiverNotifikasjonMerkelapp,
                tekst = avtaleHendelseMelding.lagArbeidsgivernotifikasjonTekst(false),
                lenke = lagLink(avtaleHendelseMelding.avtaleId.toString())
            ),
            metadata = MetadataInput(
                virksomhetsnummer = avtaleHendelseMelding.bedriftNr,
                eksternId = avtaleHendelseMelding.eksternId(),
                opprettetTidspunkt = Instant.now().toString(),
                grupperingsid = avtaleHendelseMelding.grupperingsId(),
                hardDelete = null
            ),

            eksterneVarsler = if (avtaleHendelseMelding.hendelseType.skalSendeSmsTilArbeidsgiver()) listOf(
                EksterntVarselInput(
                    sms = EksterntVarselSmsInput( // TODO: Hva skal varsles på i sms.
                        mottaker = SmsMottakerInput(
                            kontaktinfo = SmsKontaktInfoInput(tlf = avtaleHendelseMelding.arbeidsgiverTlf!!)
                        ),
                        smsTekst = "Hei! Du har fått en ny beskjed om tiltak. Logg inn på Min side - arbeidsgiver hos NAV for å se hva det gjelder. Vennlig hilsen NAV", // TODO: Bestemme tekst i sms.
                        sendetidspunkt = SendetidspunktInput(Sendevindu.DAGTID_IKKE_SOENDAG)
                    )
                )
            ) else emptyList()
        )
    )
    val beskjed = NyBeskjed(beskjedVariables)
    return beskjed

}

fun nySakStatusFerdig(sakId: String): NyStatusSak {
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
        Cluster.LOKAL -> "https://tiltaksgjennomforing.ekstern.dev.nav.no/tiltaksgjennomforing/avtale/${avtaleId}?part=ARBEIDSGIVER"
    }

}