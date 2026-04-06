-- Fix orphaned mannschaften: only relevant after HSQLDB import, not on fresh DB.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'mannschaft' AND column_name = 'game') THEN
        UPDATE mannschaft
        SET game = (SELECT g.game FROM gruppe g WHERE g.id = mannschaft.gruppea_id)
        WHERE (game IS NULL OR game = '')
          AND gruppea_id IS NOT NULL;

        DELETE FROM mannschaft WHERE game IS NULL OR game = '';
    END IF;
END $$;
