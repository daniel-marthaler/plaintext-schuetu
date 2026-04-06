-- Join tables are managed by JPA. Only create if not present (e.g. older DB without JPA-created tables).
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'gruppe_mannschaften') THEN
        CREATE TABLE gruppe_mannschaften (
            gruppe_id BIGINT NOT NULL,
            mannschaften_id BIGINT NOT NULL,
            CONSTRAINT uk_gruppe_mannschaft UNIQUE (mannschaften_id)
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'gruppe_spiele') THEN
        CREATE TABLE gruppe_spiele (
            gruppe_id BIGINT NOT NULL,
            spiele_id BIGINT NOT NULL,
            CONSTRAINT uk_gruppe_spiel UNIQUE (spiele_id)
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'penalty_final_list') THEN
        CREATE TABLE penalty_final_list (
            penalty_id BIGINT NOT NULL,
            finallist_id BIGINT NOT NULL,
            CONSTRAINT uk_penalty_mannschaft UNIQUE (finallist_id)
        );
    END IF;
END $$;
