-- Populate join tables from mannschaft FK data (only if tables exist from prior HSQLDB import)
-- On a fresh DB these tables may not yet exist (JPA creates them later), so skip gracefully.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'gruppe_mannschaften')
       AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'mannschaft') THEN
        EXECUTE 'DELETE FROM gruppe_mannschaften';
        EXECUTE 'INSERT INTO gruppe_mannschaften (gruppe_id, mannschaften_id)
                 SELECT gruppea_id, id FROM mannschaft WHERE gruppea_id IS NOT NULL
                 ON CONFLICT DO NOTHING';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'gruppe_spiele')
       AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'mannschaft')
       AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'spiel') THEN
        EXECUTE 'DELETE FROM gruppe_spiele';
        EXECUTE 'INSERT INTO gruppe_spiele (gruppe_id, spiele_id)
                 SELECT DISTINCT m.gruppea_id, s.id
                 FROM spiel s
                 JOIN mannschaft m ON m.id = s.mannschafta_id
                 WHERE m.gruppea_id IS NOT NULL AND s.typ = 0
                 ON CONFLICT DO NOTHING';
    END IF;
END $$;
