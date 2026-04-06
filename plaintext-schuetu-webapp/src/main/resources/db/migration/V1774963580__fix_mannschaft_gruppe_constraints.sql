-- Remove wrong unique constraints on mannschaft FK columns (only if table exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'mannschaft') THEN
        ALTER TABLE mannschaft DROP CONSTRAINT IF EXISTS uk4aou1u0r77y5djd8i76uierww;
        ALTER TABLE mannschaft DROP CONSTRAINT IF EXISTS uk1gtvmby69uohbnw831frob04k;
    END IF;
END $$;
