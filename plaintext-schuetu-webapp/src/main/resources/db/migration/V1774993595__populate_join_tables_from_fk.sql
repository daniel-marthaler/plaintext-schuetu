-- Populate gruppe_mannschaften join table from mannschaft.gruppea_id FK
-- The HSQLDB import set FKs on mannschaft but didn't populate the join table
DELETE FROM gruppe_mannschaften;
INSERT INTO gruppe_mannschaften (gruppe_id, mannschaften_id)
SELECT gruppea_id, id FROM mannschaft WHERE gruppea_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- Populate gruppe_spiele join table from spiel data
-- Spiele are linked to Gruppen via mannschaftA -> gruppe
DELETE FROM gruppe_spiele;
INSERT INTO gruppe_spiele (gruppe_id, spiele_id)
SELECT DISTINCT m.gruppea_id, s.id
FROM spiel s
JOIN mannschaft m ON m.id = s.mannschaft_a_id
WHERE m.gruppea_id IS NOT NULL AND s.typ = 0
ON CONFLICT DO NOTHING;
