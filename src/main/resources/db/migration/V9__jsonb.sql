-- Endre _json-kolonner fra varchar til jsonb.
-- Bruker add/drop/rename i stedet for ALTER COLUMN TYPE jsonb USING fordi
-- jOOQ 3.18 sin DDLDatabase-parser ikke støtter USING-klausulen.

-- brukernotifikasjon: avtale_melding_json varchar -> jsonb (NOT NULL)
ALTER TABLE brukernotifikasjon ADD COLUMN avtale_melding_jsonb jsonb;
UPDATE brukernotifikasjon SET avtale_melding_jsonb = avtale_melding_json::jsonb;
ALTER TABLE brukernotifikasjon DROP COLUMN avtale_melding_json;
ALTER TABLE brukernotifikasjon RENAME COLUMN avtale_melding_jsonb TO avtale_melding_json;
ALTER TABLE brukernotifikasjon ALTER COLUMN avtale_melding_json SET NOT NULL;

-- brukernotifikasjon: min_side_json varchar -> jsonb (nullable)
ALTER TABLE brukernotifikasjon ADD COLUMN min_side_jsonb jsonb;
UPDATE brukernotifikasjon SET min_side_jsonb = min_side_json::jsonb;
ALTER TABLE brukernotifikasjon DROP COLUMN min_side_json;
ALTER TABLE brukernotifikasjon RENAME COLUMN min_side_jsonb TO min_side_json;

-- arbeidsgivernotifikasjon: avtale_melding_json varchar -> jsonb (NOT NULL)
ALTER TABLE arbeidsgivernotifikasjon ADD COLUMN avtale_melding_jsonb jsonb;
UPDATE arbeidsgivernotifikasjon SET avtale_melding_jsonb = avtale_melding_json::jsonb;
ALTER TABLE arbeidsgivernotifikasjon DROP COLUMN avtale_melding_json;
ALTER TABLE arbeidsgivernotifikasjon RENAME COLUMN avtale_melding_jsonb TO avtale_melding_json;
ALTER TABLE arbeidsgivernotifikasjon ALTER COLUMN avtale_melding_json SET NOT NULL;

-- arbeidsgivernotifikasjon: arbeidsgivernotifikasjon_json varchar -> jsonb (nullable)
ALTER TABLE arbeidsgivernotifikasjon ADD COLUMN arbeidsgivernotifikasjon_jsonb jsonb;
UPDATE arbeidsgivernotifikasjon SET arbeidsgivernotifikasjon_jsonb = arbeidsgivernotifikasjon_json::jsonb;
ALTER TABLE arbeidsgivernotifikasjon DROP COLUMN arbeidsgivernotifikasjon_json;
ALTER TABLE arbeidsgivernotifikasjon RENAME COLUMN arbeidsgivernotifikasjon_jsonb TO arbeidsgivernotifikasjon_json;
