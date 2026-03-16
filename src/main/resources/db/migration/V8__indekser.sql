-- Indekser på ofte brukte kolonner i spørringer
CREATE INDEX IF NOT EXISTS idx_brukernotifikasjon_avtale_id ON brukernotifikasjon (avtale_id);
CREATE INDEX IF NOT EXISTS idx_brukernotifikasjon_varsel_id ON brukernotifikasjon (varsel_id);
CREATE INDEX IF NOT EXISTS idx_arbeidsgivernotifikasjon_avtale_id ON arbeidsgivernotifikasjon (avtale_id);
CREATE INDEX IF NOT EXISTS idx_arbeidsgivernotifikasjon_response_id ON arbeidsgivernotifikasjon (response_id);
