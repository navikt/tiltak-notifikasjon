# 🔔 Arbeidsgivernotifikasjoner 🔔

Det sendes ut notifikasjoner til arbeidsgivere når det oppstår en hendelse som er relevante for dem.
Hendelsene kommer fra tiltaksgjennomforing-api.
Notifikasjonene sendes ut via denne appen, med GrapQL integrasjon mot `min-side-arbeidsgiver`.




## Avtale opprettet:
- `Sak` ("_Avtale om [tiltakstype] for [deltakernavn]_")
- `Oppgave` ("_Ny avtale om [tiltakstype] opprettet. Åpne avtalen og fyll ut innholdet_.")


## Godkjent av arbeidsgiver:
- Henter alle `oppgaver` på avtalen (grupperingsIden) og setter disse til utført.

## Arbeidsgivers godkjenning opphevet av veileder:
- `Oppgave` ("_Avtalen må godkjennes på nytt._")
- `SMS` ("_Hei! Du har fått en ny oppgave om tiltak. Logg inn på Min side - arbeidsgiver hos NAV for å se hva det gjelder. Vennlig hilsen NAV_")

## Avtale Annullert:

### Avtale annullert med årsak feilregistrering:
Vi fjerner alt av notifikasjoner.
Vi forsøker først å softDelete[^1] Saken (med grupperingsId = avtaleId). Hvis det går bra, så antar vi at
fager sletter tilhørende oppgaver og beskejder. De kaller dette for "cascade".
Hvis det ikke går bra, så henter vi alt av oppgaver og beskjeder på avtalen og softDeleter disse.

### Avtale annullert, men ikke årsak feilregistrering:
Vi sjekker om det eksisterer en Sak på avtaleIden. 
Hvis det gjør det, sletter vi oppgaver med softDelete og setter hardDelete om 12 uker på saken.
Hvis det ikke eksisterer noen sak, sletter vi både oppgaver og beskjeder med softDelete.


## Statusendring på avtalen:
- Sjekker om avtalen har endret status til avsluttet. Hvis den har det, forsøker vi å hente saken og endre status på denne til FERDIG.


## Hendelser som generer beskjed:
- Avtale inngått (+ `SMS`)
- Avtale forlenget (+ `SMS`)
- Avtale forkortet (+ `SMS`)
- Mål endret
- Inkluderingstilskudd endret
- Om mentor endret
- Stillingsbeskrivelser endret
- Oppfølging og tilrettelegging endret
- Tilskuddsberegning endret
- Kontaktinformasjon endret



[^1]: SoftDelete er en operasjon hos fager som sletter en notifikasjon, uten å wipe den fra databasen.