create table tiltak_notifikasjon_kvittering
(
    id                   varchar primary key,
    notifikasjonstype    varchar                  not null,
    payload              varchar                  not null,
    feilmelding          varchar,
    sendt_tidspunkt      timestamp with time zone,
    avtale_hendelse_type varchar                  not null,
    mottaker             varchar                  not null,
    mottaker_tlf         varchar,
    sendt_sms            boolean                  not null,
    avtale_id            uuid                     not null,
    opprettet_tidspunkt  timestamp with time zone not null,
    notifikasjon_id      varchar                  not null
);

create index idx_tiltak_notifikasjon_kvittering_notifikasjon_id on tiltak_notifikasjon_kvittering (notifikasjon_id);
