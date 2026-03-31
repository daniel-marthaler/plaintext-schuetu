-- Fix columns that were created as VARCHAR but should be INTEGER/SMALLINT
-- The HSQLDB import stored enum ordinals and integers as strings

-- spiel table: typ and platz are enums stored as ordinals
ALTER TABLE spiel ALTER COLUMN typ TYPE SMALLINT USING typ::SMALLINT;
ALTER TABLE spiel ALTER COLUMN platz TYPE SMALLINT USING platz::SMALLINT;

-- mannschaft table: geschlecht is enum, klasse is integer
ALTER TABLE mannschaft ALTER COLUMN geschlecht TYPE SMALLINT USING geschlecht::SMALLINT;
ALTER TABLE mannschaft ALTER COLUMN klasse TYPE INTEGER USING klasse::INTEGER;

-- spiel_zeile table: phase is enum
ALTER TABLE spiel_zeile ALTER COLUMN phase TYPE SMALLINT USING phase::SMALLINT;
