package no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon

import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.*
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.NyTidStrategi
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.SaksStatus
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.enums.Sendevindu
import no.nav.tiltak.tiltaknotifikasjon.arbeidsgivernotifikasjon.graphql.generated.inputs.*
import no.nav.tiltak.tiltaknotifikasjon.avtale.*
import no.nav.tiltak.tiltaknotifikasjon.utils.Cluster
import java.time.Instant
import java.time.LocalDateTime


fun nySak(avtaleHendelseMelding: AvtaleHendelseMelding, altinnProperties: AltinnProperties): NySak {
    val variabler = NySak.Variables(
        grupperingsid = avtaleHendelseMelding.grupperingsId(),
        merkelapp = avtaleHendelseMelding.tiltakstype.arbeidsgiverNotifikasjonMerkelapp,
        virksomhetsnummer = avtaleHendelseMelding.bedriftNr,
        mottakere = listOf(
            MottakerInput(
                AltinnMottakerInput(
                    avtaleHendelseMelding.tiltakstype.serviceCode(altinnProperties),
                    avtaleHendelseMelding.tiltakstype.serviceEdition(altinnProperties)
                )
            )
        ),
        tittel = avtaleHendelseMelding.lagArbeidsgivernotifikasjonTekst(true),
        lenke = lagLink(avtaleHendelseMelding.avtaleId.toString()),
        initiellStatus = SaksStatus.MOTTATT,
        tidspunkt = Instant.now().toString(),
    )
    val nySak = NySak(variabler)
    return nySak
}

fun nyOppgave(avtaleHendelseMelding: AvtaleHendelseMelding, altinnProperties: AltinnProperties): NyOppgave {
    val oppgaveVariables = NyOppgave.Variables(
        NyOppgaveInput(
            mottakere = listOf(
                MottakerInput(
                    AltinnMottakerInput(
                        avtaleHendelseMelding.tiltakstype.serviceCode(altinnProperties),
                        avtaleHendelseMelding.tiltakstype.serviceEdition(altinnProperties)
                    )
                )
            ), notifikasjon = NotifikasjonInput(
                merkelapp = avtaleHendelseMelding.tiltakstype.arbeidsgiverNotifikasjonMerkelapp,
                tekst = avtaleHendelseMelding.lagArbeidsgivernotifikasjonTekst(false),
                lenke = lagLink(avtaleHendelseMelding.avtaleId.toString())
            ), metadata = MetadataInput(
                virksomhetsnummer = avtaleHendelseMelding.bedriftNr,
                eksternId = avtaleHendelseMelding.eksternId(),
                opprettetTidspunkt = Instant.now().toString(),
                grupperingsid = avtaleHendelseMelding.grupperingsId(),
                hardDelete = null
            ), eksterneVarsler = if (avtaleHendelseMelding.hendelseType.skalSendeSmsTilArbeidsgiver() && avtaleHendelseMelding.erArbeidsgiversTlfGyldigNorskMobilnr()) listOf(
                EksterntVarselInput(
                    sms = EksterntVarselSmsInput(
                        mottaker = SmsMottakerInput(
                            kontaktinfo = SmsKontaktInfoInput(tlf = avtaleHendelseMelding.arbeidsgiverTlf!!)
                        ),
                        smsTekst = "Hei! Du har fått en ny oppgave om tiltak. Logg inn på Min side - arbeidsgiver hos NAV for å se hva det gjelder. Vennlig hilsen NAV",
                        sendetidspunkt = SendetidspunktInput(Sendevindu.DAGTID_IKKE_SOENDAG)
                    )
                )
            ) else emptyList()
        )
    )
    val oppgave = NyOppgave(oppgaveVariables)
    return oppgave
}

fun oppgaveUtført(id: String): OppgaveUtfoert = OppgaveUtfoert(OppgaveUtfoert.Variables(id))


fun nyBeskjed(avtaleHendelseMelding: AvtaleHendelseMelding, altinnProperties: AltinnProperties): NyBeskjed {
    val beskjedVariables = NyBeskjed.Variables(
        NyBeskjedInput(
            mottakere = listOf(
                MottakerInput(
                    AltinnMottakerInput(
                        avtaleHendelseMelding.tiltakstype.serviceCode(altinnProperties),
                        avtaleHendelseMelding.tiltakstype.serviceEdition(altinnProperties)
                    )
                )
            ), notifikasjon = NotifikasjonInput(
                merkelapp = avtaleHendelseMelding.tiltakstype.arbeidsgiverNotifikasjonMerkelapp,
                tekst = avtaleHendelseMelding.lagArbeidsgivernotifikasjonTekst(false),
                lenke = lagLink(avtaleHendelseMelding.avtaleId.toString())
            ), metadata = MetadataInput(
                virksomhetsnummer = avtaleHendelseMelding.bedriftNr,
                eksternId = avtaleHendelseMelding.eksternId(),
                opprettetTidspunkt = Instant.now().toString(),
                grupperingsid = avtaleHendelseMelding.grupperingsId(),
                hardDelete = null
            ),

            eksterneVarsler = if (avtaleHendelseMelding.hendelseType.skalSendeSmsTilArbeidsgiver() && avtaleHendelseMelding.erArbeidsgiversTlfGyldigNorskMobilnr()) listOf(
                EksterntVarselInput(
                    sms = EksterntVarselSmsInput(
                        mottaker = SmsMottakerInput(
                            kontaktinfo = SmsKontaktInfoInput(tlf = avtaleHendelseMelding.arbeidsgiverTlf!!)
                        ),
                        smsTekst = "Hei! Du har fått en ny beskjed om tiltak. Logg inn på Min side - arbeidsgiver hos NAV for å se hva det gjelder. Vennlig hilsen NAV",
                        sendetidspunkt = SendetidspunktInput(Sendevindu.DAGTID_IKKE_SOENDAG)
                    )
                )
            ) else emptyList()
        )
    )
    val beskjed = NyBeskjed(beskjedVariables)
    return beskjed

}

fun nySakStatusAnnullertQuery(sakId: String): NyStatusSak {
    val om12Uker = LocalDateTime.now().plusWeeks(12).toString()
    val nySakStatusVariables = NyStatusSak.Variables(
        id = sakId,
        nyStatus = SaksStatus.FERDIG,
        overstyrStatustekstMed = "Avlyst",
        tidspunkt = Instant.now().toString(),
        hardDelete = HardDeleteUpdateInput(nyTid = FutureTemporalInput(den = om12Uker), strategi = NyTidStrategi.OVERSKRIV)
    )
    val nySakStatus = NyStatusSak(nySakStatusVariables)
    return nySakStatus
}

fun nySakStatusFerdigQuery(sakId: String): NyStatusSak {
    val om12Uker = LocalDateTime.now().plusWeeks(12).toString()
    val nySakStatusVariables = NyStatusSak.Variables(
        id = sakId,
        nyStatus = SaksStatus.FERDIG,
        tidspunkt = Instant.now().toString(),
        hardDelete = HardDeleteUpdateInput(nyTid = FutureTemporalInput(den = om12Uker), strategi = NyTidStrategi.OVERSKRIV)
    )
    val nySakStatus = NyStatusSak(nySakStatusVariables)
    return nySakStatus
}

fun nySakStatusMottattQuery(sakId: String, avtaleHendelse: AvtaleHendelseMelding): NyStatusSak {
    val sletteDato = avtaleHendelse.sluttDato?.plusWeeks(12)?.atStartOfDay().toString()
    // Samme status som saker opprettes med. Brukes i de tilfeller en avsluttet avtale forlenges og går til gjennomføres igjen.
    val nySakStatusVariables = NyStatusSak.Variables(
        id = sakId,
        nyStatus = SaksStatus.MOTTATT,
        tidspunkt = Instant.now().toString(),
        hardDelete = HardDeleteUpdateInput(nyTid = FutureTemporalInput(den = sletteDato), NyTidStrategi.OVERSKRIV) // Fager støttet ikke å fjerne harDDelete. Overskiriver bare med avtalens sluttdato pluss 12 uker.
    )
    val nySakStatus = NyStatusSak(nySakStatusVariables)
    return nySakStatus
}


fun mineNotifikasjoner(tiltakstype: Tiltakstype, grupperingsid: String): MineNotifikasjoner {
    val variables = MineNotifikasjoner.Variables(merkelapp = tiltakstype.arbeidsgiverNotifikasjonMerkelapp, grupperingsid = grupperingsid)
    val mineNotifikasjoner = MineNotifikasjoner(variables)
    return mineNotifikasjoner
}

fun nySoftDeleteSakQuery(merkelapp: String, grupperingsid: String): SoftDeleteSakByGrupperingsid {
    val variables = SoftDeleteSakByGrupperingsid.Variables(merkelapp = merkelapp, grupperingsid = grupperingsid)
    val softDeleteSak = SoftDeleteSakByGrupperingsid(variables)
    return softDeleteSak
}

fun nySoftDeleteNotifikasjonQuery(notifikasjonId: String): SoftDeleteNotifikasjon {
    val softDeleteNotifikasjon = SoftDeleteNotifikasjon(SoftDeleteNotifikasjon.Variables(notifikasjonId))
    return softDeleteNotifikasjon
}


private fun lagLink(avtaleId: String): String {
    return when (Cluster.current) {
        Cluster.PROD_GCP -> "https://arbeidsgiver.nav.no/tiltaksgjennomforing/avtale/${avtaleId}?part=ARBEIDSGIVER"
        Cluster.DEV_GCP -> "https://tiltaksgjennomforing.ekstern.dev.nav.no/tiltaksgjennomforing/avtale/${avtaleId}?part=ARBEIDSGIVER"
        Cluster.LOKAL -> "https://tiltaksgjennomforing.ekstern.dev.nav.no/tiltaksgjennomforing/avtale/${avtaleId}?part=ARBEIDSGIVER"
    }

}
