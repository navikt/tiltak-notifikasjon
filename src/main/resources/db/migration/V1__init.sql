create table brukernotifikasjon
(
    id                  varchar primary key,
    avtale_melding_json varchar,
    min_side_json       varchar,
    type                varchar,
    status              varchar,
    feilmelding         varchar,
    deltaker_fnr        varchar,
    avtale_nr           varchar,
    avtale_id           varchar
);
