-- Tømmer tabellen siden dataene ikke er i bruk ennå, og backfill kjøres på nytt med nye felter.
truncate table arbeidsgiver_refusjon_kontaktperson;

alter table arbeidsgiver_refusjon_kontaktperson
    add column arbeidsgiver_tlf varchar,
    add column tiltakstype      varchar not null default 'UKJENT'; -- default trengs visst siden det teoretisk kan ligge rader der uten tiltakstype.

alter table arbeidsgiver_refusjon_kontaktperson alter column refusjon_kontaktperson_tlf drop not null;
