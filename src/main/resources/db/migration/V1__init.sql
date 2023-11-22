create table brukernotifikasjon
(
    id                  uuid primary key,
    avtale_melding_json varchar,
    min_side_json       varchar,
    type                varchar,
    status              varchar,
    feilmelding         varchar
);
