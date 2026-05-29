-- Tømmer tabellen siden dataene ikke er i bruk ennå, og backfill kjøres på nytt med nye felter slik vi kan ha not null på tiltakstype som nå blir lagt til.
truncate table arbeidsgiver_refusjon_kontaktperson;

alter table arbeidsgiver_refusjon_kontaktperson add column bedriftNr varchar not null;
