create table tiltak_notifikasjon_kvittering
(
    id                  varchar primary key,
    notifikasjonstype   varchar                  not null,
    payload             varchar                  not null,
    feilmelding         varchar,
    sendt_tidspunkt     timestamp with time zone,
    hendelse_type       varchar                  not null,
    mottaker            varchar                  not null,
    sendt_sms           boolean                  not null,
    avtale_id           uuid                     not null,
    opprettet_tidspunkt timestamp with time zone not null
);