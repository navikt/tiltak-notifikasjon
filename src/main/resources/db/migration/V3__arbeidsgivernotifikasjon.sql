create table arbeidsgivernotifikasjon
(
    id                            varchar primary key,
    varsel_id                     varchar,
    avtale_melding_json           varchar                  not null,
    arbeidsgivernotifikasjon_json varchar,
    type                          varchar,
    status                        varchar                  not null,
    feilmelding                   varchar,
    bedrift_nr                    varchar,
    avtale_nr                     integer,
    avtale_id                     varchar,
    avtale_hendelse_type          varchar,
    sendt                         timestamp with time zone,
    opprettet                     timestamp with time zone not null,
    varslingsformål               varchar,
    response_id                   varchar
);