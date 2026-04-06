-- Add "Postkonto | Mein Konto" to mandates (only if table exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'mandate_hidden_menus') THEN
        INSERT INTO mandate_hidden_menus (config_id, menu_title)
        SELECT c.id, 'Postkonto | Mein Konto'
        FROM mandate_menu_config c
        WHERE c.is_whitelist_mode = TRUE
          AND EXISTS (
            SELECT 1 FROM mandate_hidden_menus m
            WHERE m.config_id = c.id AND m.menu_title = 'Postkonto'
          )
          AND NOT EXISTS (
            SELECT 1 FROM mandate_hidden_menus m
            WHERE m.config_id = c.id AND m.menu_title = 'Postkonto | Mein Konto'
          );
    END IF;
END $$;
