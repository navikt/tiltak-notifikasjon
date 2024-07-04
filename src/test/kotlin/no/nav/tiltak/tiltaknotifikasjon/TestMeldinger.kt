package no.nav.tiltak.tiltaknotifikasjon

val jsonManglerGodkjenningEndretAvtaleMelding = """
            {
              "hendelseType": "ENDRET",
              "avtaleStatus": "MANGLER_GODKJENNING",
              "deltakerFnr": "00000000000",
              "mentorFnr": null,
              "bedriftNr": "999999999",
              "veilederNavIdent": "Z123456",
              "tiltakstype": "MIDLERTIDIG_LONNSTILSKUDD",
              "opprettetTidspunkt": "2023-11-24T12:42:53.669444",
              "avtaleId": "1b36183e-8799-44ef-8bff-14fe39517c79",
              "avtaleNr": 11,
              "sistEndret": "2023-11-24T11:56:58.210186890Z",
              "annullertTidspunkt": null,
              "annullertGrunn": null,
              "slettemerket": false,
              "opprettetAvArbeidsgiver": false,
              "enhetGeografisk": "0313",
              "enhetsnavnGeografisk": "NAV St. Hanshaugen",
              "enhetOppfolging": "0906",
              "enhetsnavnOppfolging": "NAV Agder",
              "godkjentForEtterregistrering": false,
              "kvalifiseringsgruppe": "VARIG",
              "formidlingsgruppe": "ARBS",
              "tilskuddPeriode": [],
              "feilregistrert": false,
              "versjon": 1,
              "deltakerFornavn": "Lilly",
              "deltakerEtternavn": "Lønning",
              "deltakerTlf": "40000000",
              "bedriftNavn": "Pers butikk",
              "arbeidsgiverFornavn": "Per",
              "arbeidsgiverEtternavn": "Kremmer",
              "arbeidsgiverTlf": "99999999",
              "veilederFornavn": "Vera",
              "veilederEtternavn": "Veileder",
              "veilederTlf": "44444444",
              "oppfolging": "Telefon hver uke",
              "tilrettelegging": "Ingen",
              "startDato": "2023-11-24",
              "sluttDato": "2024-11-23",
              "stillingprosent": 50,
              "journalpostId": null,
              "arbeidsoppgaver": "Butikkarbeidfs",
              "stillingstittel": "Butikkbetjent",
              "stillingStyrk08": 5223,
              "stillingKonseptId": 112968,
              "antallDagerPerUke": 5,
              "refusjonKontaktperson": {
                "refusjonKontaktpersonFornavn": "Ola",
                "refusjonKontaktpersonEtternavn": "Olsen",
                "refusjonKontaktpersonTlf": "12345678",
                "ønskerVarslingOmRefusjon": true
              },
              "mentorFornavn": null,
              "mentorEtternavn": null,
              "mentorOppgaver": null,
              "mentorAntallTimer": null,
              "mentorTimelonn": null,
              "mentorTlf": null,
              "arbeidsgiverKontonummer": "22222222222",
              "lonnstilskuddProsent": 40,
              "manedslonn": 20000,
              "feriepengesats": 0.12,
              "arbeidsgiveravgift": 0.141,
              "harFamilietilknytning": true,
              "familietilknytningForklaring": "En middels god forklaring",
              "feriepengerBelop": 2400,
              "otpSats": 0.02,
              "otpBelop": 448,
              "arbeidsgiveravgiftBelop": 3222,
              "sumLonnsutgifter": 26070,
              "sumLonnstilskudd": 10428,
              "manedslonn100pst": 52140,
              "sumLønnstilskuddRedusert": 7821,
              "datoForRedusertProsent": "2024-05-24",
              "stillingstype": "FAST",
              "maal": [],
              "inkluderingstilskuddsutgift": [],
              "inkluderingstilskuddBegrunnelse": null,
              "inkluderingstilskuddTotalBeløp": 0,
              "godkjentAvDeltaker": null,
              "godkjentTaushetserklæringAvMentor": null,
              "godkjentAvArbeidsgiver": null,
              "godkjentAvVeileder": null,
              "godkjentAvBeslutter": null,
              "avtaleInngått": null,
              "ikrafttredelsestidspunkt": null,
              "godkjentAvNavIdent": null,
              "godkjentAvBeslutterNavIdent": null,
              "enhetKostnadssted": null,
              "enhetsnavnKostnadssted": null,
              "godkjentPaVegneGrunn": null,
              "godkjentPaVegneAv": false,
              "godkjentPaVegneAvArbeidsgiverGrunn": null,
              "godkjentPaVegneAvArbeidsgiver": false,
              "innholdType": "INNGÅ",
              "utførtAv": "Z123456",
              "utførtAvRolle": "VEILEDER"
            }
        """.trimIndent()

val jsonGodkjentAvDeltaker = """
    {
      "hendelseType": "GODKJENT_AV_DELTAKER",
      "avtaleStatus": "MANGLER_GODKJENNING",
      "deltakerFnr": "00000000000",
      "mentorFnr": "23090170716",
      "bedriftNr": "999999999",
      "veilederNavIdent": "Z123456",
      "tiltakstype": "MENTOR",
      "opprettetTidspunkt": "2023-11-30T10:54:13.747032",
      "avtaleId": "1b36183e-8799-44ef-8bff-14fe39517c79",
      "avtaleNr": 3,
      "sistEndret": "2023-11-30T09:57:06.119788Z",
      "annullertTidspunkt": null,
      "annullertGrunn": null,
      "slettemerket": false,
      "opprettetAvArbeidsgiver": false,
      "enhetGeografisk": null,
      "enhetsnavnGeografisk": null,
      "enhetOppfolging": null,
      "enhetsnavnOppfolging": null,
      "godkjentForEtterregistrering": false,
      "kvalifiseringsgruppe": null,
      "formidlingsgruppe": null,
      "tilskuddPeriode": [],
      "feilregistrert": false,
      "versjon": 1,
      "deltakerFornavn": "Dagny",
      "deltakerEtternavn": "Deltaker",
      "deltakerTlf": "40000000",
      "bedriftNavn": "Pers butikk",
      "arbeidsgiverFornavn": "Per",
      "arbeidsgiverEtternavn": "Kremmer",
      "arbeidsgiverTlf": "99999999",
      "veilederFornavn": "Vera",
      "veilederEtternavn": "Veileder",
      "veilederTlf": "44444444",
      "oppfolging": "Telefon hver uke",
      "tilrettelegging": "Ingen",
      "startDato": "2023-11-30",
      "sluttDato": "2024-05-29",
      "stillingprosent": null,
      "journalpostId": null,
      "arbeidsoppgaver": null,
      "stillingstittel": null,
      "stillingStyrk08": null,
      "stillingKonseptId": null,
      "antallDagerPerUke": null,
      "refusjonKontaktperson": null,
      "mentorFornavn": "Mentor",
      "mentorEtternavn": "Mentorsen",
      "mentorOppgaver": "Mentoroppgaver",
      "mentorAntallTimer": 10.0,
      "mentorTimelonn": 1000,
      "mentorTlf": "44444444",
      "arbeidsgiverKontonummer": null,
      "lonnstilskuddProsent": null,
      "manedslonn": null,
      "feriepengesats": null,
      "arbeidsgiveravgift": null,
      "harFamilietilknytning": true,
      "familietilknytningForklaring": "En middels god forklaring",
      "feriepengerBelop": null,
      "otpSats": null,
      "otpBelop": null,
      "arbeidsgiveravgiftBelop": null,
      "sumLonnsutgifter": null,
      "sumLonnstilskudd": null,
      "manedslonn100pst": null,
      "sumLønnstilskuddRedusert": null,
      "datoForRedusertProsent": null,
      "stillingstype": null,
      "maal": [],
      "inkluderingstilskuddsutgift": [],
      "inkluderingstilskuddBegrunnelse": null,
      "inkluderingstilskuddTotalBeløp": 0,
      "godkjentAvDeltaker": "2023-11-30T10:57:06.119782",
      "godkjentTaushetserklæringAvMentor": "2023-11-30T10:54:13.747071",
      "godkjentAvArbeidsgiver": null,
      "godkjentAvVeileder": null,
      "godkjentAvBeslutter": null,
      "avtaleInngått": null,
      "ikrafttredelsestidspunkt": null,
      "godkjentAvNavIdent": null,
      "godkjentAvBeslutterNavIdent": null,
      "enhetKostnadssted": null,
      "enhetsnavnKostnadssted": null,
      "godkjentPaVegneGrunn": null,
      "godkjentPaVegneAv": false,
      "godkjentPaVegneAvArbeidsgiverGrunn": null,
      "godkjentPaVegneAvArbeidsgiver": false,
      "innholdType": "INNGÅ",
      "utførtAv": "00000000000",
      "utførtAvRolle": "DELTAKER"
    }
""".trimIndent()

val jsonGodkjentAvVeileder = """
           {
             "hendelseType": "GODKJENT_AV_VEILEDER",
             "avtaleStatus": "MANGLER_GODKJENNING",
             "deltakerFnr": "00000000000",
             "mentorFnr": null,
             "bedriftNr": "999999999",
             "veilederNavIdent": "X123456",
             "tiltakstype": "MIDLERTIDIG_LONNSTILSKUDD",
             "opprettetTidspunkt": "2023-11-14T13:31:49.800338",
             "avtaleId": "384e1299-a9f6-4de5-8699-a30cdec543e7",
             "avtaleNr": 26,
             "sistEndret": "2023-11-14T12:38:34.375684343Z",
             "annullertTidspunkt": null,
             "annullertGrunn": null,
             "slettemerket": false,
             "opprettetAvArbeidsgiver": false,
             "enhetGeografisk": "0313",
             "enhetsnavnGeografisk": "NAV St. Hanshaugen",
             "enhetOppfolging": "0906",
             "enhetsnavnOppfolging": "NAV Agder",
             "godkjentForEtterregistrering": false,
             "kvalifiseringsgruppe": "VARIG",
             "formidlingsgruppe": "ARBS",
             "tilskuddPeriode": [],
             "feilregistrert": false,
             "versjon": 1,
             "deltakerFornavn": "Donald",
             "deltakerEtternavn": "Duck",
             "deltakerTlf": "12121212",
             "bedriftNavn": "Saltrød og Høneby",
             "arbeidsgiverFornavn": "For",
             "arbeidsgiverEtternavn": "Navn",
             "arbeidsgiverTlf": "12121212",
             "veilederFornavn": "Nav",
             "veilederEtternavn": "Etternavn",
             "veilederTlf": "12121212",
             "oppfolging": "Dette er oppfølgingen",
             "tilrettelegging": "Og dette er tilretteleggingen",
             "startDato": "2023-11-07",
             "sluttDato": "2024-06-30",
             "stillingprosent": 100,
             "journalpostId": null,
             "arbeidsoppgaver": "Oppgaver for å arbeide",
             "stillingstittel": "Garver",
             "stillingStyrk08": 7535,
             "stillingKonseptId": 69158,
             "antallDagerPerUke": 5,
             "refusjonKontaktperson": null,
             "mentorFornavn": null,
             "mentorEtternavn": null,
             "mentorOppgaver": null,
             "mentorAntallTimer": null,
             "mentorTimelonn": null,
             "mentorTlf": null,
             "arbeidsgiverKontonummer": "10000008162",
             "lonnstilskuddProsent": 60,
             "manedslonn": 40000,
             "feriepengesats": 0.12,
             "arbeidsgiveravgift": 0.141,
             "harFamilietilknytning": true,
             "familietilknytningForklaring": "Jeg er slekt med personen på tiltak",
             "feriepengerBelop": 4800,
             "otpSats": 0.06,
             "otpBelop": 2688,
             "arbeidsgiveravgiftBelop": 6696,
             "sumLonnsutgifter": 54184,
             "sumLonnstilskudd": 32510,
             "manedslonn100pst": 54184,
             "sumLønnstilskuddRedusert": null,
             "datoForRedusertProsent": null,
             "stillingstype": "FAST",
             "maal": [],
             "inkluderingstilskuddsutgift": [],
             "inkluderingstilskuddBegrunnelse": null,
             "inkluderingstilskuddTotalBeløp": 0,
             "godkjentAvDeltaker": "2023-11-14T13:38:17.209559",
             "godkjentTaushetserklæringAvMentor": null,
             "godkjentAvArbeidsgiver": "2023-11-14T13:35:54.158615",
             "godkjentAvVeileder": "2023-11-14T13:38:34.375653979",
             "godkjentAvBeslutter": null,
             "avtaleInngått": null,
             "ikrafttredelsestidspunkt": "2023-11-14T13:38:34.375653979",
             "godkjentAvNavIdent": "X123456",
             "godkjentAvBeslutterNavIdent": null,
             "enhetKostnadssted": null,
             "enhetsnavnKostnadssted": null,
             "godkjentPaVegneGrunn": null,
             "godkjentPaVegneAv": false,
             "godkjentPaVegneAvArbeidsgiverGrunn": null,
             "godkjentPaVegneAvArbeidsgiver": false,
             "innholdType": "INNGÅ",
             "utførtAv": "X123456",
             "utførtAvRolle": "VEILEDER"
           }
        """.trimIndent()



val jsonGodkjentAvArbeidsgiverMelding = """
    {
      "hendelseType": "GODKJENT_AV_ARBEIDSGIVER",
      "avtaleStatus": "MANGLER_GODKJENNING",
      "deltakerFnr": "00000000000",
      "mentorFnr": null,
      "bedriftNr": "999999999",
      "veilederNavIdent": "Z123456",
      "tiltakstype": "VARIG_LONNSTILSKUDD",
      "opprettetTidspunkt": "2024-01-18T14:21:10.317687",
      "avtaleId": "1b36183e-8799-44ef-8bff-14fe39517c79",
      "avtaleNr": null,
      "sistEndret": "2024-01-18T13:21:10.354605Z",
      "annullertTidspunkt": null,
      "annullertGrunn": null,
      "slettemerket": false,
      "opprettetAvArbeidsgiver": false,
      "enhetGeografisk": null,
      "enhetsnavnGeografisk": null,
      "enhetOppfolging": "0906",
      "enhetsnavnOppfolging": "Oslo gamlebyen",
      "godkjentForEtterregistrering": false,
      "kvalifiseringsgruppe": "VARIG",
      "formidlingsgruppe": null,
      "tilskuddPeriode": [],
      "feilregistrert": false,
      "versjon": 1,
      "deltakerFornavn": "Lilly",
      "deltakerEtternavn": "Lønning",
      "deltakerTlf": "40000000",
      "bedriftNavn": "Pers butikk",
      "arbeidsgiverFornavn": "Per",
      "arbeidsgiverEtternavn": "Kremmer",
      "arbeidsgiverTlf": "99999999",
      "veilederFornavn": "Vera",
      "veilederEtternavn": "Veileder",
      "veilederTlf": "44444444",
      "oppfolging": "Telefon hver uke",
      "tilrettelegging": "Ingen",
      "startDato": "2023-01-18",
      "sluttDato": "2025-01-18",
      "stillingprosent": 50,
      "journalpostId": null,
      "arbeidsoppgaver": "Butikkarbeid",
      "stillingstittel": "Butikkbetjent",
      "stillingStyrk08": 5223,
      "stillingKonseptId": 112968,
      "antallDagerPerUke": 5,
      "refusjonKontaktperson": {
        "refusjonKontaktpersonFornavn": "Ola",
        "refusjonKontaktpersonEtternavn": "Olsen",
        "refusjonKontaktpersonTlf": "12345678",
        "ønskerVarslingOmRefusjon": true
      },
      "mentorFornavn": null,
      "mentorEtternavn": null,
      "mentorOppgaver": null,
      "mentorAntallTimer": null,
      "mentorTimelonn": null,
      "mentorTlf": null,
      "arbeidsgiverKontonummer": "22222222222",
      "lonnstilskuddProsent": 60,
      "manedslonn": 20000,
      "feriepengesats": 0.12,
      "arbeidsgiveravgift": 0.141,
      "harFamilietilknytning": true,
      "familietilknytningForklaring": "En middels god forklaring",
      "feriepengerBelop": 10000,
      "otpSats": 0.02,
      "otpBelop": 400,
      "arbeidsgiveravgiftBelop": 20400,
      "sumLonnsutgifter": 40800,
      "sumLonnstilskudd": 24480,
      "manedslonn100pst": 81600,
      "sumLønnstilskuddRedusert": null,
      "datoForRedusertProsent": null,
      "stillingstype": "FAST",
      "maal": [],
      "inkluderingstilskuddsutgift": [],
      "inkluderingstilskuddBegrunnelse": null,
      "inkluderingstilskuddTotalBeløp": 0,
      "godkjentAvDeltaker": null,
      "godkjentTaushetserklæringAvMentor": null,
      "godkjentAvArbeidsgiver": "2024-01-18T14:21:10.321283",
      "godkjentAvVeileder": "2024-01-18T14:21:10.354583",
      "godkjentAvBeslutter": null,
      "avtaleInngått": null,
      "ikrafttredelsestidspunkt": "2024-01-18T14:21:10.354583",
      "godkjentAvNavIdent": "Z123456",
      "godkjentAvBeslutterNavIdent": null,
      "enhetKostnadssted": null,
      "enhetsnavnKostnadssted": null,
      "godkjentPaVegneGrunn": {
        "ikkeBankId": true,
        "reservert": false,
        "digitalKompetanse": false,
        "arenaMigreringDeltaker": false
      },
      "godkjentPaVegneAv": true,
      "godkjentPaVegneAvArbeidsgiverGrunn": null,
      "godkjentPaVegneAvArbeidsgiver": false,
      "innholdType": "INNGÅ",
      "utførtAv": "00000000000",
      "utførtAvRolle": "ARBEIDSGIVER"
    }
""".trimIndent()

val jsonAvtaleAnnullertMelding = """
    {
      "hendelseType": "ANNULLERT",
      "avtaleStatus": "ANNULLERT",
      "deltakerFnr": "00000000000",
      "mentorFnr": null,
      "bedriftNr": "999999999",
      "veilederNavIdent": "Z123456",
      "tiltakstype": "MIDLERTIDIG_LONNSTILSKUDD",
      "opprettetTidspunkt": "2024-02-01T10:08:50.007154",
      "avtaleId": "1b36183e-8799-44ef-8bff-14fe39517c79",
      "avtaleNr": 11,
      "sistEndret": "2024-02-01T09:09:41.195095Z",
      "annullertTidspunkt": "2024-02-01T09:09:41.195072Z",
      "annullertGrunn": "Begynt i arbeid",
      "slettemerket": false,
      "opprettetAvArbeidsgiver": false,
      "enhetGeografisk": null,
      "enhetsnavnGeografisk": null,
      "enhetOppfolging": "0906",
      "enhetsnavnOppfolging": "Oslo gamlebyen",
      "godkjentForEtterregistrering": false,
      "kvalifiseringsgruppe": "BFORM",
      "formidlingsgruppe": null,
      "tilskuddPeriode": [],
      "feilregistrert": false,
      "versjon": 1,
      "deltakerFornavn": "Lilly",
      "deltakerEtternavn": "Lønning",
      "deltakerTlf": "40000000",
      "bedriftNavn": "Pers butikk",
      "arbeidsgiverFornavn": "Per",
      "arbeidsgiverEtternavn": "Kremmer",
      "arbeidsgiverTlf": "99999999",
      "veilederFornavn": "Vera",
      "veilederEtternavn": "Veileder",
      "veilederTlf": "44444444",
      "oppfolging": "Telefon hver uke",
      "tilrettelegging": "Ingen",
      "startDato": "2024-02-01",
      "sluttDato": "2025-01-31",
      "stillingprosent": 50,
      "journalpostId": null,
      "arbeidsoppgaver": "Butikkarbeid",
      "stillingstittel": "Butikkbetjent",
      "stillingStyrk08": 5223,
      "stillingKonseptId": 112968,
      "antallDagerPerUke": 5,
      "refusjonKontaktperson": {
        "refusjonKontaktpersonFornavn": "Ola",
        "refusjonKontaktpersonEtternavn": "Olsen",
        "refusjonKontaktpersonTlf": "12345678",
        "ønskerVarslingOmRefusjon": true
      },
      "mentorFornavn": null,
      "mentorEtternavn": null,
      "mentorOppgaver": null,
      "mentorAntallTimer": null,
      "mentorTimelonn": null,
      "mentorTlf": null,
      "arbeidsgiverKontonummer": "22222222222",
      "lonnstilskuddProsent": 40,
      "manedslonn": 20000,
      "feriepengesats": 0.12,
      "arbeidsgiveravgift": 0.141,
      "harFamilietilknytning": true,
      "familietilknytningForklaring": "En middels god forklaring",
      "feriepengerBelop": 10000,
      "otpSats": 0.02,
      "otpBelop": 400,
      "arbeidsgiveravgiftBelop": 20400,
      "sumLonnsutgifter": 40800,
      "sumLonnstilskudd": 16320,
      "manedslonn100pst": 81600,
      "sumLønnstilskuddRedusert": 12240,
      "datoForRedusertProsent": "2024-08-01",
      "stillingstype": "FAST",
      "maal": [],
      "inkluderingstilskuddsutgift": [],
      "inkluderingstilskuddBegrunnelse": null,
      "inkluderingstilskuddTotalBeløp": 0,
      "godkjentAvDeltaker": null,
      "godkjentTaushetserklæringAvMentor": null,
      "godkjentAvArbeidsgiver": "2024-02-01T10:08:50.007475",
      "godkjentAvVeileder": null,
      "godkjentAvBeslutter": null,
      "avtaleInngått": null,
      "ikrafttredelsestidspunkt": null,
      "godkjentAvNavIdent": null,
      "godkjentAvBeslutterNavIdent": null,
      "enhetKostnadssted": null,
      "enhetsnavnKostnadssted": null,
      "godkjentPaVegneGrunn": null,
      "godkjentPaVegneAv": false,
      "godkjentPaVegneAvArbeidsgiverGrunn": null,
      "godkjentPaVegneAvArbeidsgiver": false,
      "innholdType": "INNGÅ",
      "utførtAv": "Z123456",
      "utførtAvRolle": "VEILEDER"
    }
""".trimIndent()

val jsonAvtaleAnnullertFeilregistreringMelding = """
    {
      "hendelseType": "ANNULLERT",
      "avtaleStatus": "ANNULLERT",
      "deltakerFnr": "00000000000",
      "mentorFnr": null,
      "bedriftNr": "999999999",
      "veilederNavIdent": "Z123456",
      "tiltakstype": "MIDLERTIDIG_LONNSTILSKUDD",
      "opprettetTidspunkt": "2024-02-16T10:26:18.906216",
      "avtaleId": "1b36183e-8799-44ef-8bff-14fe39517c79",
      "avtaleNr": 11,
      "sistEndret": "2024-02-16T14:39:25.454746Z",
      "annullertTidspunkt": "2024-02-16T14:39:25.454722Z",
      "annullertGrunn": "Feilregistrering",
      "slettemerket": false,
      "opprettetAvArbeidsgiver": false,
      "enhetGeografisk": null,
      "enhetsnavnGeografisk": null,
      "enhetOppfolging": "0906",
      "enhetsnavnOppfolging": "Oslo gamlebyen",
      "godkjentForEtterregistrering": false,
      "kvalifiseringsgruppe": "BFORM",
      "formidlingsgruppe": null,
      "tilskuddPeriode": [],
      "feilregistrert": true,
      "versjon": 1,
      "deltakerFornavn": "Lilly",
      "deltakerEtternavn": "Lønning",
      "deltakerTlf": "40000000",
      "bedriftNavn": "Pers butikk",
      "arbeidsgiverFornavn": "Per",
      "arbeidsgiverEtternavn": "Kremmer",
      "arbeidsgiverTlf": "99999999",
      "veilederFornavn": "Vera",
      "veilederEtternavn": "Veileder",
      "veilederTlf": "44444444",
      "oppfolging": "Telefon hver uke",
      "tilrettelegging": "Ingen",
      "startDato": "2024-02-16",
      "sluttDato": "2025-02-15",
      "stillingprosent": 50,
      "journalpostId": null,
      "arbeidsoppgaver": "Butikkarbeid",
      "stillingstittel": "Butikkbetjent",
      "stillingStyrk08": 5223,
      "stillingKonseptId": 112968,
      "antallDagerPerUke": 5,
      "refusjonKontaktperson": {
        "refusjonKontaktpersonFornavn": "Ola",
        "refusjonKontaktpersonEtternavn": "Olsen",
        "refusjonKontaktpersonTlf": "12345678",
        "ønskerVarslingOmRefusjon": true
      },
      "mentorFornavn": null,
      "mentorEtternavn": null,
      "mentorOppgaver": null,
      "mentorAntallTimer": null,
      "mentorTimelonn": null,
      "mentorTlf": null,
      "arbeidsgiverKontonummer": "22222222222",
      "lonnstilskuddProsent": 40,
      "manedslonn": 20000,
      "feriepengesats": 0.12,
      "arbeidsgiveravgift": 0.141,
      "harFamilietilknytning": true,
      "familietilknytningForklaring": "En middels god forklaring",
      "feriepengerBelop": 10000,
      "otpSats": 0.02,
      "otpBelop": 400,
      "arbeidsgiveravgiftBelop": 20400,
      "sumLonnsutgifter": 40800,
      "sumLonnstilskudd": 16320,
      "manedslonn100pst": 81600,
      "sumLønnstilskuddRedusert": 12240,
      "datoForRedusertProsent": "2024-08-16",
      "stillingstype": "FAST",
      "maal": [],
      "inkluderingstilskuddsutgift": [],
      "inkluderingstilskuddBegrunnelse": null,
      "inkluderingstilskuddTotalBeløp": 0,
      "godkjentAvDeltaker": null,
      "godkjentTaushetserklæringAvMentor": null,
      "godkjentAvArbeidsgiver": "2024-02-16T10:26:18.906467",
      "godkjentAvVeileder": null,
      "godkjentAvBeslutter": null,
      "avtaleInngått": null,
      "ikrafttredelsestidspunkt": null,
      "godkjentAvNavIdent": null,
      "godkjentAvBeslutterNavIdent": null,
      "enhetKostnadssted": null,
      "enhetsnavnKostnadssted": null,
      "godkjentPaVegneGrunn": null,
      "godkjentPaVegneAv": false,
      "godkjentPaVegneAvArbeidsgiverGrunn": null,
      "godkjentPaVegneAvArbeidsgiver": false,
      "innholdType": "INNGÅ",
      "utførtAv": "Z123456",
      "utførtAvRolle": "VEILEDER"
    }
""".trimIndent()

val jsonAvtaleInngåttMelding = """
    {
      "hendelseType": "AVTALE_INNGÅTT",
      "avtaleStatus": "GJENNOMFØRES",
      "deltakerFnr": "00000000000",
      "mentorFnr": null,
      "bedriftNr": "999999999",
      "veilederNavIdent": "Z123456",
      "tiltakstype": "MIDLERTIDIG_LONNSTILSKUDD",
      "opprettetTidspunkt": "2024-02-02T12:56:21.533626",
      "avtaleId": "1b36183e-8799-44ef-8bff-14fe39517c79",
      "avtaleNr": 11,
      "sistEndret": "2024-02-02T11:57:12.221268Z",
      "annullertTidspunkt": null,
      "annullertGrunn": null,
      "slettemerket": false,
      "opprettetAvArbeidsgiver": false,
      "enhetGeografisk": null,
      "enhetsnavnGeografisk": null,
      "enhetOppfolging": "0906",
      "enhetsnavnOppfolging": "Oslo gamlebyen",
      "godkjentForEtterregistrering": false,
      "kvalifiseringsgruppe": "BFORM",
      "formidlingsgruppe": null,
      "tilskuddPeriode": [],
      "feilregistrert": false,
      "versjon": 1,
      "deltakerFornavn": "Lilly",
      "deltakerEtternavn": "Lønning",
      "deltakerTlf": "40000000",
      "bedriftNavn": "Pers butikk",
      "arbeidsgiverFornavn": "Per",
      "arbeidsgiverEtternavn": "Kremmer",
      "arbeidsgiverTlf": "99999999",
      "veilederFornavn": "Vera",
      "veilederEtternavn": "Veileder",
      "veilederTlf": "44444444",
      "oppfolging": "Telefon hver uke",
      "tilrettelegging": "Ingen",
      "startDato": "2024-02-02",
      "sluttDato": "2025-02-01",
      "stillingprosent": 50,
      "journalpostId": null,
      "arbeidsoppgaver": "Butikkarbeid",
      "stillingstittel": "Butikkbetjent",
      "stillingStyrk08": 5223,
      "stillingKonseptId": 112968,
      "antallDagerPerUke": 5,
      "refusjonKontaktperson": {
        "refusjonKontaktpersonFornavn": "Ola",
        "refusjonKontaktpersonEtternavn": "Olsen",
        "refusjonKontaktpersonTlf": "12345678",
        "ønskerVarslingOmRefusjon": true
      },
      "mentorFornavn": null,
      "mentorEtternavn": null,
      "mentorOppgaver": null,
      "mentorAntallTimer": null,
      "mentorTimelonn": null,
      "mentorTlf": null,
      "arbeidsgiverKontonummer": "22222222222",
      "lonnstilskuddProsent": 40,
      "manedslonn": 20000,
      "feriepengesats": 0.12,
      "arbeidsgiveravgift": 0.141,
      "harFamilietilknytning": true,
      "familietilknytningForklaring": "En middels god forklaring",
      "feriepengerBelop": 10000,
      "otpSats": 0.02,
      "otpBelop": 400,
      "arbeidsgiveravgiftBelop": 20400,
      "sumLonnsutgifter": 40800,
      "sumLonnstilskudd": 16320,
      "manedslonn100pst": 81600,
      "sumLønnstilskuddRedusert": 12240,
      "datoForRedusertProsent": "2024-08-02",
      "stillingstype": "FAST",
      "maal": [],
      "inkluderingstilskuddsutgift": [],
      "inkluderingstilskuddBegrunnelse": null,
      "inkluderingstilskuddTotalBeløp": 0,
      "godkjentAvDeltaker": "2024-02-02T12:56:41.537045",
      "godkjentTaushetserklæringAvMentor": null,
      "godkjentAvArbeidsgiver": "2024-02-02T12:56:21.533879",
      "godkjentAvVeileder": "2024-02-02T12:56:41.537045",
      "godkjentAvBeslutter": "2024-02-02T12:57:12.221099",
      "avtaleInngått": "2024-02-02T12:57:12.221099",
      "ikrafttredelsestidspunkt": "2024-02-02T12:56:41.537045",
      "godkjentAvNavIdent": "Z123456",
      "godkjentAvBeslutterNavIdent": "X123456",
      "enhetKostnadssted": null,
      "enhetsnavnKostnadssted": null,
      "godkjentPaVegneGrunn": {
        "ikkeBankId": true,
        "reservert": false,
        "digitalKompetanse": false,
        "arenaMigreringDeltaker": false
      },
      "godkjentPaVegneAv": true,
      "godkjentPaVegneAvArbeidsgiverGrunn": null,
      "godkjentPaVegneAvArbeidsgiver": false,
      "innholdType": "INNGÅ",
      "utførtAv": "X123456",
      "utførtAvRolle": "BESLUTTER"
    }
""".trimIndent()

val jsonAvtaleForlengetMelding = """
    {
      "hendelseType": "AVTALE_FORLENGET",
      "avtaleStatus": "GJENNOMFØRES",
      "deltakerFnr": "00000000000",
      "mentorFnr": null,
      "bedriftNr": "999999999",
      "veilederNavIdent": "Z123456",
      "tiltakstype": "MIDLERTIDIG_LONNSTILSKUDD",
      "opprettetTidspunkt": "2024-02-05T14:07:06.04628",
      "avtaleId": "1b36183e-8799-44ef-8bff-14fe39517c79",
      "avtaleNr": 17,
      "sistEndret": "2024-02-05T13:07:42.746197Z",
      "annullertTidspunkt": null,
      "annullertGrunn": null,
      "slettemerket": false,
      "opprettetAvArbeidsgiver": false,
      "enhetGeografisk": null,
      "enhetsnavnGeografisk": null,
      "enhetOppfolging": "0906",
      "enhetsnavnOppfolging": "Oslo gamlebyen",
      "godkjentForEtterregistrering": false,
      "kvalifiseringsgruppe": "VARIG",
      "formidlingsgruppe": "ARBS",
      "tilskuddPeriode": [],
      "feilregistrert": false,
      "versjon": 2,
      "deltakerFornavn": "Lilly",
      "deltakerEtternavn": "Lønning",
      "deltakerTlf": "40000000",
      "bedriftNavn": "Pers butikk",
      "arbeidsgiverFornavn": "Per",
      "arbeidsgiverEtternavn": "Kremmer",
      "arbeidsgiverTlf": "99999999",
      "veilederFornavn": "Vera",
      "veilederEtternavn": "Veileder",
      "veilederTlf": "44444444",
      "oppfolging": "Telefon hver uke",
      "tilrettelegging": "Ingen",
      "startDato": "2024-02-05",
      "sluttDato": "2025-02-28",
      "stillingprosent": 50,
      "journalpostId": null,
      "arbeidsoppgaver": "Butikkarbeid",
      "stillingstittel": "Butikkbetjent",
      "stillingStyrk08": 5223,
      "stillingKonseptId": 112968,
      "antallDagerPerUke": 5,
      "refusjonKontaktperson": {
        "refusjonKontaktpersonFornavn": "Ola",
        "refusjonKontaktpersonEtternavn": "Olsen",
        "refusjonKontaktpersonTlf": "12345678",
        "ønskerVarslingOmRefusjon": true
      },
      "mentorFornavn": null,
      "mentorEtternavn": null,
      "mentorOppgaver": null,
      "mentorAntallTimer": null,
      "mentorTimelonn": null,
      "mentorTlf": null,
      "arbeidsgiverKontonummer": "22222222222",
      "lonnstilskuddProsent": 60,
      "manedslonn": 20000,
      "feriepengesats": 0.12,
      "arbeidsgiveravgift": 0.141,
      "harFamilietilknytning": true,
      "familietilknytningForklaring": "En middels god forklaring",
      "feriepengerBelop": 2400,
      "otpSats": 0.02,
      "otpBelop": 448,
      "arbeidsgiveravgiftBelop": 3222,
      "sumLonnsutgifter": 26070,
      "sumLonnstilskudd": 15642,
      "manedslonn100pst": 52140,
      "sumLønnstilskuddRedusert": 13035,
      "datoForRedusertProsent": "2025-02-05",
      "stillingstype": "FAST",
      "maal": [],
      "inkluderingstilskuddsutgift": [],
      "inkluderingstilskuddBegrunnelse": null,
      "inkluderingstilskuddTotalBeløp": 0,
      "godkjentAvDeltaker": "2024-02-05T14:07:06.046437",
      "godkjentTaushetserklæringAvMentor": null,
      "godkjentAvArbeidsgiver": "2024-02-05T14:07:06.046436",
      "godkjentAvVeileder": "2024-02-05T14:07:06.046438",
      "godkjentAvBeslutter": null,
      "avtaleInngått": "2024-02-05T14:07:06.046438",
      "ikrafttredelsestidspunkt": "2024-02-05T14:07:42.743858",
      "godkjentAvNavIdent": "Q987654",
      "godkjentAvBeslutterNavIdent": null,
      "enhetKostnadssted": null,
      "enhetsnavnKostnadssted": null,
      "godkjentPaVegneGrunn": null,
      "godkjentPaVegneAv": false,
      "godkjentPaVegneAvArbeidsgiverGrunn": null,
      "godkjentPaVegneAvArbeidsgiver": false,
      "innholdType": "FORLENGE",
      "utførtAv": "Z123456",
      "utførtAvRolle": "VEILEDER"
    }
""".trimIndent()

val jsonAvtaleForkortetMelding = """
    {
      "hendelseType": "AVTALE_FORKORTET",
      "avtaleStatus": "GJENNOMFØRES",
      "deltakerFnr": "00000000000",
      "mentorFnr": null,
      "bedriftNr": "999999999",
      "veilederNavIdent": "Z123456",
      "tiltakstype": "MIDLERTIDIG_LONNSTILSKUDD",
      "opprettetTidspunkt": "2024-02-05T14:07:06.04628",
      "avtaleId": "1b36183e-8799-44ef-8bff-14fe39517c79",
      "avtaleNr": 17,
      "sistEndret": "2024-02-05T13:08:08.026203Z",
      "annullertTidspunkt": null,
      "annullertGrunn": null,
      "slettemerket": false,
      "opprettetAvArbeidsgiver": false,
      "enhetGeografisk": null,
      "enhetsnavnGeografisk": null,
      "enhetOppfolging": "0906",
      "enhetsnavnOppfolging": "Oslo gamlebyen",
      "godkjentForEtterregistrering": false,
      "kvalifiseringsgruppe": "VARIG",
      "formidlingsgruppe": "ARBS",
      "tilskuddPeriode": [],
      "feilregistrert": false,
      "versjon": 3,
      "deltakerFornavn": "Lilly",
      "deltakerEtternavn": "Lønning",
      "deltakerTlf": "40000000",
      "bedriftNavn": "Pers butikk",
      "arbeidsgiverFornavn": "Per",
      "arbeidsgiverEtternavn": "Kremmer",
      "arbeidsgiverTlf": "99999999",
      "veilederFornavn": "Vera",
      "veilederEtternavn": "Veileder",
      "veilederTlf": "44444444",
      "oppfolging": "Telefon hver uke",
      "tilrettelegging": "Ingen",
      "startDato": "2024-02-05",
      "sluttDato": "2025-02-20",
      "stillingprosent": 50,
      "journalpostId": null,
      "arbeidsoppgaver": "Butikkarbeid",
      "stillingstittel": "Butikkbetjent",
      "stillingStyrk08": 5223,
      "stillingKonseptId": 112968,
      "antallDagerPerUke": 5,
      "refusjonKontaktperson": {
        "refusjonKontaktpersonFornavn": "Ola",
        "refusjonKontaktpersonEtternavn": "Olsen",
        "refusjonKontaktpersonTlf": "12345678",
        "ønskerVarslingOmRefusjon": true
      },
      "mentorFornavn": null,
      "mentorEtternavn": null,
      "mentorOppgaver": null,
      "mentorAntallTimer": null,
      "mentorTimelonn": null,
      "mentorTlf": null,
      "arbeidsgiverKontonummer": "22222222222",
      "lonnstilskuddProsent": 60,
      "manedslonn": 20000,
      "feriepengesats": 0.12,
      "arbeidsgiveravgift": 0.141,
      "harFamilietilknytning": true,
      "familietilknytningForklaring": "En middels god forklaring",
      "feriepengerBelop": 2400,
      "otpSats": 0.02,
      "otpBelop": 448,
      "arbeidsgiveravgiftBelop": 3222,
      "sumLonnsutgifter": 26070,
      "sumLonnstilskudd": 15642,
      "manedslonn100pst": 52140,
      "sumLønnstilskuddRedusert": 13035,
      "datoForRedusertProsent": "2025-02-05",
      "stillingstype": "FAST",
      "maal": [],
      "inkluderingstilskuddsutgift": [],
      "inkluderingstilskuddBegrunnelse": null,
      "inkluderingstilskuddTotalBeløp": 0,
      "godkjentAvDeltaker": "2024-02-05T14:07:06.046437",
      "godkjentTaushetserklæringAvMentor": null,
      "godkjentAvArbeidsgiver": "2024-02-05T14:07:06.046436",
      "godkjentAvVeileder": "2024-02-05T14:07:06.046438",
      "godkjentAvBeslutter": null,
      "avtaleInngått": "2024-02-05T14:07:06.046438",
      "ikrafttredelsestidspunkt": "2024-02-05T14:08:08.026116",
      "godkjentAvNavIdent": "Q987654",
      "godkjentAvBeslutterNavIdent": null,
      "enhetKostnadssted": null,
      "enhetsnavnKostnadssted": null,
      "godkjentPaVegneGrunn": null,
      "godkjentPaVegneAv": false,
      "godkjentPaVegneAvArbeidsgiverGrunn": null,
      "godkjentPaVegneAvArbeidsgiver": false,
      "innholdType": "FORKORTE",
      "utførtAv": "Z123456",
      "utførtAvRolle": "VEILEDER"
    }
""".trimIndent()


val jsonAvtaleOpprettetMelding = """
    {
      "hendelseType": "OPPRETTET",
      "avtaleStatus": "PÅBEGYNT",
      "deltakerFnr": "00000000000",
      "mentorFnr": null,
      "bedriftNr": "999999999",
      "veilederNavIdent": "Z123456",
      "tiltakstype": "MIDLERTIDIG_LONNSTILSKUDD",
      "opprettetTidspunkt": "2024-06-23T13:24:42.139858",
      "avtaleId": "1b36183e-8799-44ef-8bff-14fe39517c79",
      "avtaleNr": null,
      "sistEndret": "2024-06-23T11:24:42.139869Z",
      "annullertTidspunkt": null,
      "annullertGrunn": null,
      "slettemerket": false,
      "opprettetAvArbeidsgiver": false,
      "enhetGeografisk": "0313",
      "enhetsnavnGeografisk": "NAV St. Hanshaugen",
      "enhetOppfolging": "0906",
      "enhetsnavnOppfolging": "NAV Agder",
      "godkjentForEtterregistrering": false,
      "kvalifiseringsgruppe": "VARIG",
      "formidlingsgruppe": "ARBS",
      "tilskuddPeriode": [],
      "feilregistrert": false,
      "versjon": 1,
      "deltakerFornavn": "Donald",
      "deltakerEtternavn": "Duck",
      "deltakerTlf": null,
      "bedriftNavn": "Saltrød og Høneby",
      "arbeidsgiverFornavn": null,
      "arbeidsgiverEtternavn": null,
      "arbeidsgiverTlf": null,
      "veilederFornavn": null,
      "veilederEtternavn": null,
      "veilederTlf": null,
      "oppfolging": null,
      "tilrettelegging": null,
      "startDato": null,
      "sluttDato": null,
      "stillingprosent": null,
      "journalpostId": null,
      "arbeidsoppgaver": null,
      "stillingstittel": null,
      "stillingStyrk08": null,
      "stillingKonseptId": null,
      "antallDagerPerUke": null,
      "refusjonKontaktperson": null,
      "mentorFornavn": null,
      "mentorEtternavn": null,
      "mentorOppgaver": null,
      "mentorAntallTimer": null,
      "mentorTimelonn": null,
      "mentorTlf": null,
      "arbeidsgiverKontonummer": null,
      "lonnstilskuddProsent": 60,
      "manedslonn": null,
      "feriepengesats": null,
      "arbeidsgiveravgift": null,
      "harFamilietilknytning": null,
      "familietilknytningForklaring": null,
      "feriepengerBelop": null,
      "otpSats": null,
      "otpBelop": null,
      "arbeidsgiveravgiftBelop": null,
      "sumLonnsutgifter": null,
      "sumLonnstilskudd": null,
      "manedslonn100pst": null,
      "sumLønnstilskuddRedusert": null,
      "datoForRedusertProsent": null,
      "stillingstype": null,
      "maal": [],
      "inkluderingstilskuddsutgift": [],
      "inkluderingstilskuddBegrunnelse": null,
      "inkluderingstilskuddTotalBeløp": 0,
      "godkjentAvDeltaker": null,
      "godkjentTaushetserklæringAvMentor": null,
      "godkjentAvArbeidsgiver": null,
      "godkjentAvVeileder": null,
      "godkjentAvBeslutter": null,
      "avtaleInngått": null,
      "ikrafttredelsestidspunkt": null,
      "godkjentAvNavIdent": null,
      "godkjentAvBeslutterNavIdent": null,
      "enhetKostnadssted": null,
      "enhetsnavnKostnadssted": null,
      "godkjentPaVegneGrunn": null,
      "godkjentPaVegneAv": false,
      "godkjentPaVegneAvArbeidsgiverGrunn": null,
      "godkjentPaVegneAvArbeidsgiver": false,
      "innholdType": "INNGÅ",
      "utførtAv": "Z123456",
      "utførtAvRolle": "VEILEDER"
    }
""".trimIndent()