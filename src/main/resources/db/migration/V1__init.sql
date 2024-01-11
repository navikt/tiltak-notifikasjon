create table brukernotifikasjon
(
    id                   varchar primary key,
    varsel_id            varchar,
    avtale_melding_json  varchar,
    min_side_json        varchar,
    type                 varchar,
    status               varchar,
    feilmelding          varchar,
    deltaker_fnr         varchar,
    avtale_nr            integer,
    avtale_id            varchar,
    avtale_hendelse_type varchar,
    sendt                timestamp with time zone,
    opprettet            timestamp with time zone
);
