-- Fix orphaned mannschaften: set game from their linked gruppe
UPDATE mannschaft
SET game = (SELECT g.game FROM gruppe g WHERE g.id = mannschaft.gruppea_id)
WHERE (game IS NULL OR game = '')
  AND gruppea_id IS NOT NULL;

-- Delete any remaining mannschaften without a game (truly orphaned)
DELETE FROM mannschaft WHERE game IS NULL OR game = '';
