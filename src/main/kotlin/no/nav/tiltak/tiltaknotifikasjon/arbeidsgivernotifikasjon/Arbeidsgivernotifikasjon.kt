package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.*
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.SaksStatus
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.Sendevindu
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.inputs.*
import no.nav.tiltak.tiltaknotifikasjon.avtale.AvtaleHendelseMelding
import no.nav.tiltak.tiltaknotifikasjon.utils.Cluster
import java.time.Instant
import java.util.*


fun nySak(avtaleHendelseMelding: AvtaleHendelseMelding): NySak {
    val variabler = NySak.Variables(
        grupperingsid = avtaleHendelseMelding.avtaleId.toString(),
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
                tekst = NotifikasjonTekst.TILTAK_GODKJENNINGER_OPPHEVET_AV_VEILEDER.tekst(avtaleHendelseMelding.tiltakstype),
                lenke = lagLink(avtaleHendelseMelding.avtaleId.toString())
            ),
            metadata = MetadataInput(
                virksomhetsnummer = avtaleHendelseMelding.bedriftNr,
                eksternId = avtaleHendelseMelding.avtaleId.toString(), // TODO: Sjekke om dette blir riktig
                opprettetTidspunkt = Instant.now().toString(),
                grupperingsid = avtaleHendelseMelding.avtaleId.toString(), // Dette blir satt til avtaleId som også blir brukt på saken.
                hardDelete = null // TODO: Sjekke hva dette betyr
            ),
            eksterneVarsler = listOf(
                EksterntVarselInput(
                    sms = EksterntVarselSmsInput(
                        mottaker = SmsMottakerInput(kontaktinfo = SmsKontaktInfoInput(tlf = avtaleHendelseMelding.arbeidsgiverTlf!!)), // TODO: kan være null...
                        smsTekst = "Ny avtale til godkjenning!", // TODO: Bestemme tekst i sms.
                        sendetidspunkt = SendetidspunktInput(Sendevindu.DAGTID_IKKE_SOENDAG)
                    )
                )
            ),
        )
    )
    val oppgave = NyOppgave(oppgaveVariables)
    return oppgave
}

fun oppgaveUtført(id: String): OppgaveUtfoert {
    val oppgaveUtfoert = OppgaveUtfoert(OppgaveUtfoert.Variables(id))
    return oppgaveUtfoert
}


fun nyBeskjed(avtaleHendelseMelding: AvtaleHendelseMelding): NyBeskjed {
    val beskjedVariables = NyBeskjed.Variables(
        NyBeskjedInput(
            mottakere = listOf(MottakerInput(AltinnMottakerInput(avtaleHendelseMelding.tiltakstype.serviceCode, avtaleHendelseMelding.tiltakstype.serviceEdition))),
            notifikasjon = NotifikasjonInput(
                merkelapp = avtaleHendelseMelding.tiltakstype.arbeidsgiverNotifikasjonMerkelapp,
                tekst = "Ny avtale til godkjenning!", // TODO: mappe endringstyper til tekster
                lenke = lagLink(avtaleHendelseMelding.avtaleId.toString())
            ),
            metadata = MetadataInput(
                virksomhetsnummer = avtaleHendelseMelding.bedriftNr,
                eksternId = avtaleHendelseMelding.avtaleId.toString() + UUID.randomUUID().toString(), // TODO: Kunne f.eks. laget et løpenummer, som da sier noe om hvor mange notifikasjoner osm er sendt på hver avtale.
                opprettetTidspunkt = Instant.now().toString(),
                grupperingsid = avtaleHendelseMelding.avtaleId.toString(),
                hardDelete = null
            ),

            eksterneVarsler = listOf(
                EksterntVarselInput(
                    sms = EksterntVarselSmsInput(
                        mottaker = SmsMottakerInput(
                            kontaktinfo = SmsKontaktInfoInput(tlf = avtaleHendelseMelding.arbeidsgiverTlf!!)
                        ),
                        smsTekst = "Ny avtale til godkjenning!", // TODO: Bestemme tekst i sms.
                        sendetidspunkt = SendetidspunktInput(Sendevindu.DAGTID_IKKE_SOENDAG)
                    )
                )
            ),
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
        else -> {
            "https://tiltaksgjennomforing.ekstern.dev.nav.no/tiltaksgjennomforing/avtale/${avtaleId}?part=ARBEIDSGIVER"
        }
    }

}