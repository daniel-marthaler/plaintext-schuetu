-- Fix wrong OneToOne unique constraints (only if tables exist)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'spiel') THEN
        ALTER TABLE spiel DROP CONSTRAINT IF EXISTS ukm5k0cumc1n75c4wwkbypr1pin;
        ALTER TABLE spiel DROP CONSTRAINT IF EXISTS uk673c6lg1kopr23e4bncs7dbv;
        ALTER TABLE spiel DROP CONSTRAINT IF EXISTS uk4ja8vhln1p9o60h64ob5yi0wd;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'gruppe') THEN
        ALTER TABLE gruppe DROP CONSTRAINT IF EXISTS ukke2ywveqgb3kqlpnebtmlauqp;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'penalty') THEN
        ALTER TABLE penalty DROP CONSTRAINT IF EXISTS ukpm8ve2l8fwgttccejydr2v5lj;
    END IF;
END $$;
