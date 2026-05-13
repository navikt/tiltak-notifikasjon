create table arbeidsgiver_refusjon_kontaktperson
(
    id                                varchar primary key,
    refusjon_kontaktperson_tlf        varchar                  not null,
    arbeidsgiver_onsker_ogsa_varsling boolean,
    avtale_id                         uuid                     not null,
    avtale_innhold_versjon            integer                  not null,
    avtale_hendelse_type              varchar                  not null,
    avtale_hendelse_sist_endret       timestamp with time zone not null,
    topic_offset                      bigint                   not null,
    innlest_tidspunkt                 timestamp with time zone not null
);
