# 🔔 Arbeidsgivernotifikasjoner 🔔

Det sendes ut notifikasjoner til arbeidsgivere når det oppstår en hendelse som er relevante for dem.
Hendelsene kommer fra tiltaksgjennomforing-api.
Notifikasjonene sendes ut via denne appen, med GrapQL integrasjon mot `min-side-arbeidsgiver`.

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





[^1]: SoftDelete er en operasjon hos fager som sletter en notifikasjon, uten å wipe den fra databasen.