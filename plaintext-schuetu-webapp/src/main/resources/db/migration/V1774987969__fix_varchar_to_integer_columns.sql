-- Fix columns that were created as VARCHAR by old schema but should be INTEGER/SMALLINT.
-- On fresh DB (JPA-created schema) these are already the correct type, so skip.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'spiel' AND column_name = 'typ' AND data_type = 'character varying') THEN
        EXECUTE 'ALTER TABLE spiel ALTER COLUMN typ TYPE SMALLINT USING typ::SMALLINT';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'spiel' AND column_name = 'platz' AND data_type = 'character varying') THEN
        EXECUTE 'ALTER TABLE spiel ALTER COLUMN platz TYPE SMALLINT USING platz::SMALLINT';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'mannschaft' AND column_name = 'geschlecht' AND data_type = 'character varying') THEN
        EXECUTE 'ALTER TABLE mannschaft ALTER COLUMN geschlecht TYPE SMALLINT USING geschlecht::SMALLINT';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'mannschaft' AND column_name = 'klasse' AND data_type = 'character varying') THEN
        EXECUTE 'ALTER TABLE mannschaft ALTER COLUMN klasse TYPE INTEGER USING klasse::INTEGER';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'spiel_zeile' AND column_name = 'phase' AND data_type = 'character varying') THEN
        EXECUTE 'ALTER TABLE spiel_zeile ALTER COLUMN phase TYPE SMALLINT USING phase::SMALLINT';
    END IF;
END $$;
