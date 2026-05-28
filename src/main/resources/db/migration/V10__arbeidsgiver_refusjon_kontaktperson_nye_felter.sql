alter table arbeidsgiver_refusjon_kontaktperson
    add column arbeidsgiver_tlf varchar,
    add column tiltakstype      varchar;

alter table arbeidsgiver_refusjon_kontaktperson alter column refusjon_kontaktperson_tlf drop not null;
