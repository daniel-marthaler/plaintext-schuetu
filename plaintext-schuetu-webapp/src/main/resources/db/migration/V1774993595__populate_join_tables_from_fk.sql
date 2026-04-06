-- Populate join tables from mannschaft FK data (only if tables exist from prior HSQLDB import)
-- On a fresh DB these tables may not yet exist (JPA creates them later), so skip gracefully.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'gruppe_mannschaften') THEN
        DELETE FROM gruppe_mannschaften;
        INSERT INTO gruppe_mannschaften (gruppe_id, mannschaften_id)
        SELECT gruppea_id, id FROM mannschaft WHERE gruppea_id IS NOT NULL
        ON CONFLICT DO NOTHING;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'gruppe_spiele') THEN
        DELETE FROM gruppe_spiele;
        INSERT INTO gruppe_spiele (gruppe_id, spiele_id)
        SELECT DISTINCT m.gruppea_id, s.id
        FROM spiel s
        JOIN mannschaft m ON m.id = s.mannschafta_id
        WHERE m.gruppea_id IS NOT NULL AND s.typ = 0
        ON CONFLICT DO NOTHING;
    END IF;
END $$;
