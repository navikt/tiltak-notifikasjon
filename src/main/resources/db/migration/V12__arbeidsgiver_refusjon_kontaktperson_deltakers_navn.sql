-- Tømmer tabellen siden dataene ikke er i bruk ennå, og backfill kjøres på nytt med nye felter.
truncate table arbeidsgiver_refusjon_kontaktperson;

alter table arbeidsgiver_refusjon_kontaktperson add column deltaker_fornavn varchar;
alter table arbeidsgiver_refusjon_kontaktperson add column deltaker_etternavn varchar;
