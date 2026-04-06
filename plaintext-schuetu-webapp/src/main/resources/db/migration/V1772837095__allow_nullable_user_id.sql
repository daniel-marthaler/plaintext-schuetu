-- Allow user_id to be NULL for remote user sessions (only if table exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'discovery_user_session' AND column_name = 'user_id') THEN
        ALTER TABLE discovery_user_session ALTER COLUMN user_id DROP NOT NULL;
    END IF;
END $$;
