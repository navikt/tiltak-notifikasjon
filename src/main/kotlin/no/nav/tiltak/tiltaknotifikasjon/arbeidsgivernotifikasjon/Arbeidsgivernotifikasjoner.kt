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


fun nySak(avtaleHendelseMelding: AvtaleHendelseMelding): NySak {
    val variabler = NySak.Variables(
        grupperingsid = avtaleHendelseMelding.grupperingsId(),
        merkelapp = avtaleHendelseMelding.tiltakstype.arbeidsgiverNotifikasjonMerkelapp,
        virksomhetsnummer = avtaleHendelseMelding.bedriftNr,
        mottakere = listOf(
            MottakerInput(
                altinnRessurs = AltinnRessursMottakerInput(
                    ressursId = avtaleHendelseMelding.tiltakstype.ressursId,
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

fun nySakRefusjoner(refusjonKontaktperson: RefusjonKontaktpersonEntitet, refusjonId: String): NySak {
    val variabler = NySak.Variables(
        grupperingsid = refusjonKontaktperson.grupperingsId(),
        merkelapp = refusjonKontaktperson.tiltakstype.arbeidsgiverNotifikasjonMerkelapp,
        virksomhetsnummer = refusjonKontaktperson.bedriftNr,
        mottakere = listOf(
            MottakerInput(
                altinnRessurs = AltinnRessursMottakerInput(
                    ressursId = refusjonKontaktperson.tiltakstype.ressursId,
                )
            )
        ),
        tittel = "Refusjoner for avtale om ${refusjonKontaktperson.tiltakstype.beskrivelse}", //TODO: Vurdere om vi skal få inn avtaleNr eller deltakernavn eller lignende her
        lenke = lagRefusjonLink(refusjonId),
        initiellStatus = SaksStatus.MOTTATT,
        tidspunkt = Instant.now().toString(),
    )
    val nySak = NySak(variabler)
    return nySak
}
fun nyBeskjedRefusjoner(refusjonKontaktperson: RefusjonKontaktpersonEntitet, refusjonId: String): NyBeskjed {
    val variabler = NyBeskjed.Variables(
        NyBeskjedInput(
            mottakere = listOf(
                MottakerInput(
                    altinnRessurs = AltinnRessursMottakerInput(
                        ressursId = refusjonKontaktperson.tiltakstype.ressursId,
                    )
                )
            ), notifikasjon = NotifikasjonInput(
                merkelapp = refusjonKontaktperson.tiltakstype.arbeidsgiverNotifikasjonMerkelapp,
                tekst = "Det har kommet en ny oppdatering på refusjonssøknaden din for avtale om ${refusjonKontaktperson.tiltakstype.beskrivelse}. Klikk på lenken for å se oppdateringen.",
                lenke = lagRefusjonLink(refusjonId)
            ), metadata = MetadataInput(
                virksomhetsnummer = refusjonKontaktperson.bedriftNr,
                eksternId = refusjonKontaktperson.eksternId(),
                opprettetTidspunkt = Instant.now().toString(),
                grupperingsid = refusjonKontaktperson.grupperingsId(),
                hardDelete = null
            ), eksterneVarsler = listOf(
                EksterntVarselInput(
                    sms = EksterntVarselSmsInput(
                        mottaker = SmsMottakerInput(
                            kontaktinfo = SmsKontaktInfoInput(tlf = refusjonKontaktperson.arbeidsgiverTlf)
                        ),
                        smsTekst = "Hei! Det har kommet en ny oppdatering på refusjonssøknaden din for avtale om ${refusjonKontaktperson.tiltakstype.beskrivelse}. Logg inn på Min side - arbeidsgiver hos NAV for å se hva det gjelder. Vennlig hilsen NAV",
                        sendetidspunkt = SendetidspunktInput(Sendevindu.DAGTID_IKKE_SOENDAG)
                    )
        )
    )))
    val nyBeskjed = NyBeskjed(variabler)
    return nyBeskjed
}

fun nyHentSakQuery(grupperingsid: String, merkelapp: String): HentSakMedGrupperingsid {
    val variables = HentSakMedGrupperingsid.Variables(
        grupperingsid = grupperingsid,
        merkelapp = merkelapp,
    )
    return HentSakMedGrupperingsid(variables)
}

fun nyOppgave(avtaleHendelseMelding: AvtaleHendelseMelding): NyOppgave {
    val oppgaveVariables = NyOppgave.Variables(
        NyOppgaveInput(
            mottakere = listOf(
                MottakerInput(
                    altinnRessurs = AltinnRessursMottakerInput(
                        ressursId = avtaleHendelseMelding.tiltakstype.ressursId,
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


fun nyBeskjed(avtaleHendelseMelding: AvtaleHendelseMelding): NyBeskjed {
    val beskjedVariables = NyBeskjed.Variables(
        NyBeskjedInput(
            mottakere = listOf(
                MottakerInput(
                    altinnRessurs = AltinnRessursMottakerInput(
                        ressursId = avtaleHendelseMelding.tiltakstype.ressursId,
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
fun lagRefusjonLink(refusjonId: String): String {
    return when (Cluster.current) {
        Cluster.PROD_GCP -> "https://tiltak-refusjon.nav.no/refusjon/${refusjonId}"
        Cluster.DEV_GCP -> "https://tiltak-refusjon.ekstern.dev.nav.no/refusjon/${refusjonId}"
        Cluster.LOKAL -> "https://tiltak-refusjon.ekstern.dev.nav.no/refusjon/${refusjonId}"
    }
}
