CREATE OR REPLACE FUNCTION set_verzija_broj()
    RETURNS TRIGGER AS $$
BEGIN
    SELECT COALESCE(MAX(verzija_broj), 0) + 1
    INTO NEW.verzija_broj
    FROM dokument_verzija
    WHERE dokument_id = NEW.dokument_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_set_verzija_broj ON dokument_verzija;
CREATE TRIGGER trg_set_verzija_broj
    BEFORE INSERT ON dokument_verzija
    FOR EACH ROW
EXECUTE FUNCTION set_verzija_broj();

CREATE OR REPLACE FUNCTION set_dokument_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_set_dokument_updated_at ON dokumenti;
CREATE TRIGGER trg_set_dokument_updated_at
    BEFORE UPDATE ON dokumenti
    FOR EACH ROW
EXECUTE FUNCTION set_dokument_updated_at();
