alter table arbeidsgivernotifikasjon drop column varsel_id; -- Aldri vært i bruk
alter table arbeidsgivernotifikasjon rename column opprettet to opprettet_tidspunkt;
alter table arbeidsgivernotifikasjon rename column sendt to sendt_tidspunkt;