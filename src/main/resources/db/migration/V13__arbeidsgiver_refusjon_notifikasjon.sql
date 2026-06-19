create table arbeidsgiver_refusjon_notifikasjon
(
    id                              varchar primary key,
    refusjon_id                     varchar,
    arbeidsgivernotifikasjon_json   varchar                  not null,
    type                            varchar                  not null,
    status                          varchar                  not null,
    bedrift_nr                      varchar,
    feilmelding                     varchar,
    sendt_tidspunkt                 timestamp with time zone,
    opprettet_tidspunkt             timestamp with time zone not null,
    varslingsformål                 varchar                  not null,
    avtale_id                       varchar,
    response_id                     varchar,
    hard_delete_skedulert_tidspunkt timestamp without time zone,
    kafka_offset                    bigint not null,
    kafka_key                       varchar not null
);

create index if not exists idx_arbeidsgiver_refusjon_notifikasjon_avtale_id
    on arbeidsgiver_refusjon_notifikasjon (avtale_id);

create index if not exists idx_arbeidsgiver_refusjon_notifikasjon_response_id
    on arbeidsgiver_refusjon_notifikasjon (response_id);

create index if not exists idx_arbeidsgiver_refusjon_notifikasjon_refusjon_id
    on arbeidsgiver_refusjon_notifikasjon (refusjon_id);
